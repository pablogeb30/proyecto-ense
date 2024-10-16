package usc.etse.grei.ense.p3.project.service;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import usc.etse.grei.ense.p3.project.model.Date;
import usc.etse.grei.ense.p3.project.model.*;
import usc.etse.grei.ense.p3.project.repository.AssessmentRepository;
import usc.etse.grei.ense.p3.project.repository.UserRepository;
import usc.etse.grei.ense.p3.project.util.PatchUtil;

import java.time.LocalDate;
import java.util.*;

@Service
public class UserService {

	private final UserRepository users;
	private final MongoTemplate mongo;
	private final PatchUtil patchUtil;
	private final AssessmentRepository assessments;
	private final Validator validator;

	@Autowired
	public UserService(UserRepository users, MongoTemplate mongo, PatchUtil patchUtil, AssessmentRepository assessments, Validator validator) {
		this.users = users;
		this.mongo = mongo;
		this.patchUtil = patchUtil;
		this.assessments = assessments;
		this.validator = validator;
	}

	public Result<List<User>> get(int page, int size, Sort sort, Example<User> filter) {

		Pageable request = PageRequest.of(page, size, sort);

		Criteria criteria = Criteria.byExample(filter);

		Query query = Query.query(criteria).with(request);
		query.fields().include("name", "country", "birthday", "picture");
		query.fields().exclude("_id");

		List<User> result = mongo.find(query, User.class);

		if (result.isEmpty()) {
			return new Result<>(null, false, "No users", 0, Result.Code.NOT_FOUND);
		}

		return new Result<>(result, false, "Users data", 0, Result.Code.OK);

	}

	public Result<User> get(String email) {

		Optional<User> result = users.findById(email);

		if (result.isPresent()) {

			User user = result.get();
			return new Result<>(user, false, "User data", 0, Result.Code.OK);

		} else {

			return new Result<>(null, false, "No user", 0, Result.Code.NOT_FOUND);

		}

	}

	public Result<User> create(User user) {

		try {

			Date date = user.getBirthday();

			LocalDate birthday = LocalDate.of(date.getYear(), date.getMonth(), date.getDay());

			if (birthday.isAfter(LocalDate.now())) {
				return new Result<>(null, true, "Invalid birthday", 0, Result.Code.BAD_REQUEST);
			}

			User newUser = users.insert(user);
			return new Result<>(newUser, false, "User created", 0, Result.Code.CREATED);

		} catch (Exception e) {

			return new Result<>(null, true, e.getLocalizedMessage(), 0, Result.Code.BAD_REQUEST);

		}

	}

	public Result<User> update(String email, List<Map<String, Object>> operations) {

		try {

			User originalUser = users.findById(email).orElse(null);

			if (originalUser == null) {
				return new Result<>(null, false, "No user", 0, Result.Code.NOT_FOUND);
			}

			operations.removeIf(op -> op.containsKey("path") && (op.get("path").equals("/email") || op.get("path").equals("/birthday") || op.get("path").equals("/friends")));

			User filteredUser = patchUtil.patch(originalUser, operations);

			Set<ConstraintViolation<User>> violations = validator.validate(filteredUser, OnUpdate.class);

			if (!violations.isEmpty()) {
				return new Result<>(null, true, "Not valid due to violations", 0, Result.Code.BAD_REQUEST);
			}

			User updatedUser = users.save(filteredUser);

			ExampleMatcher matcher = ExampleMatcher.matching();
			Example<Assessment> filter = Example.of(
					new Assessment().setUser(new User().setEmail(updatedUser.getEmail())),
					matcher
			);

			if (!originalUser.getName().equals(updatedUser.getName())) {

				assessments.findAll(filter).forEach(assessment -> {

					User user = assessment.getUser();
					user.setName(updatedUser.getName());

					assessment.setUser(user);

					assessments.save(assessment);

				});

				if (updatedUser.getFriends() != null) {

					User parseUser = new User().setEmail(updatedUser.getEmail()).setName(updatedUser.getName());

					for (User friend : updatedUser.getFriends()) {

						Optional<User> resultFriend = users.findById(friend.getEmail());
						User friendUser = resultFriend.get();

						List<User> friends = friendUser.getFriends();

						friends.removeIf(f -> f.getEmail().equals(updatedUser.getEmail()));
						friends.add(parseUser);

						friendUser.setFriends(friends);

						users.save(friendUser);

					}

				}

			}

			return new Result<>(updatedUser, false, "User updated", 0, Result.Code.OK);

		} catch (Exception e) {

			return new Result<>(null, true, e.getLocalizedMessage(), 0, Result.Code.BAD_REQUEST);

		}

	}

	public Result<User> delete(String email) {

		Optional<User> result = users.findById(email);

		if (result.isEmpty()) {
			return new Result<>(null, false, "No user", 0, Result.Code.NOT_FOUND);
		}

		User user = result.get();

		ExampleMatcher matcher = ExampleMatcher.matching();
		Example<Assessment> filter = Example.of(
				new Assessment().setUser(new User().setEmail(user.getEmail())),
				matcher
		);

		assessments.deleteAll(assessments.findAll(filter));

		if (user.getFriends() != null) {
			for (User friend : user.getFriends()) {
				deleteFriend(user.getEmail(), friend.getEmail(), true);
			}
		}

		users.delete(user);

		return new Result<>(user, false, "User deleted", 0, Result.Code.OK);

	}

	public Result<User> addFriend(String email, User friend, boolean redo) {

		User user = users.findById(email).orElse(null);

		if (user == null) {
			return new Result<>(null, false, "No user", 0, Result.Code.NOT_FOUND);
		}

		User bdFriend = users.findById(friend.getEmail()).orElse(null);

		if (user.equals(bdFriend)) {
			return new Result<>(null, false, "Recursive aditions not allowed", 0, Result.Code.UNAUTHORIZED);
		}

		if (bdFriend == null || !bdFriend.getName().equals(friend.getName())) {
			return new Result<>(null, false, "No friend", 0, Result.Code.NOT_FOUND);
		}

		List<User> friends = user.getFriends();

		if (friends == null) {
			friends = new ArrayList<>();
		}

		friends.add(friend);

		user.setFriends(friends);

		users.save(user);

		if (redo) {
			User parseUser = new User().setEmail(user.getEmail()).setName(user.getName());
			addFriend(bdFriend.getEmail(), parseUser, false);
		}

		return new Result<>(user, false, "Friend added", 0, Result.Code.CREATED);

	}

	public Result<User> deleteFriend(String email, String friendEmail, boolean redo) {

		User user = users.findById(email).orElse(null);

		if (user == null) {
			return new Result<>(null, false, "No user", 0, Result.Code.NOT_FOUND);
		}

		User friend = users.findById(friendEmail).orElse(null);

		if (friend == null) {
			return new Result<>(null, false, "No friend", 0, Result.Code.NOT_FOUND);
		}

		List<User> friends = user.getFriends();

		if (friends == null) {
			return new Result<>(null, false, "No friends", 0, Result.Code.NOT_FOUND);
		}

		friends.removeIf(f -> f.getEmail().equals(friendEmail));

		user.setFriends(friends);

		users.save(user);

		if (redo) {
			deleteFriend(friendEmail, email, false);
		}

		return new Result<>(user, false, "Friend deleted", 0, Result.Code.OK);

	}

}