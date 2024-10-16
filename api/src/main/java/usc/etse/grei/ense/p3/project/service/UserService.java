package usc.etse.grei.ense.p3.project.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import usc.etse.grei.ense.p3.project.model.Assessment;
import usc.etse.grei.ense.p3.project.model.User;
import usc.etse.grei.ense.p3.project.repository.AssessmentRepository;
import usc.etse.grei.ense.p3.project.repository.UserRepository;
import usc.etse.grei.ense.p3.project.util.PatchUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class UserService {

	private final UserRepository users;
	private final MongoTemplate mongo;
	private final PatchUtil patchUtil;
	private final AssessmentRepository assessments;

	@Autowired
	public UserService(UserRepository users, MongoTemplate mongo, PatchUtil patchUtil, AssessmentRepository assessments) {
		this.users = users;
		this.mongo = mongo;
		this.patchUtil = patchUtil;
		this.assessments = assessments;
	}

	public Optional<Page<User>> get(int page, int size, Sort sort, Example<User> filter) {

		Pageable request = PageRequest.of(page, size, sort);

		Criteria criteria = Criteria.byExample(filter);

		Query query = Query.query(criteria).with(request);
		query.fields().include("name", "country", "birthday", "picture");
		query.fields().exclude("_id");

		List<User> result = mongo.find(query, User.class);

		return Optional.of(new PageImpl<>(result, request, result.size()));

	}

	public Optional<User> get(String email) {
		return users.findById(email);
	}

	public Optional<User> create(User user) {

		try {

			users.insert(user);
			return Optional.of(user);

		} catch (Exception e) {

			return Optional.empty();

		}

	}

	public Optional<User> update(String email, List<Map<String, Object>> operations) {

		try {

			Optional<User> result = users.findById(email);

			if (result.isEmpty()) {
				return Optional.empty();
			}

			operations.removeIf(op -> op.containsKey("path") && (op.get("path").equals("/email") || op.get("path").equals("/birthday") || op.get("path").equals("/friends")));

			User originalUser = result.get();
			User filteredUser = patchUtil.patch(result.get(), operations);
			User updatedUser = users.save(filteredUser);

			ExampleMatcher matcher = ExampleMatcher.matching();
			Example<Assessment> filter = Example.of(
					new Assessment().setUser(new User().setEmail(updatedUser.getEmail())),
					matcher
			);

			if (!originalUser.getName().equals(updatedUser.getName())) {
				assessments.findAll(filter).forEach(assessment -> {
					assessment.getUser().setName(updatedUser.getName());
					assessments.save(assessment);
				});
			}

			return Optional.of(updatedUser);

		} catch (Exception e) {

			return Optional.empty();

		}

	}

	public Optional<User> delete(String email) {

		Optional<User> result = users.findById(email);

		if (result.isEmpty()) {
			return Optional.empty();
		}

		User user = result.get();

		ExampleMatcher matcher = ExampleMatcher.matching();
		Example<Assessment> filter = Example.of(
				new Assessment().setUser(new User().setEmail(user.getEmail())),
				matcher
		);

		assessments.findAll(filter).forEach(assessment -> {
			assessments.delete(assessment);
		});

		if (user.getFriends() != null) {
			for (User friend : user.getFriends()) {
				deleteFriend(user.getEmail(), friend.getEmail());
			}
		}

		users.delete(user);

		return Optional.of(user);

	}

	public Optional<User> addFriend(String email, User friend) {

		Optional<User> result = users.findById(email);

		if (result.isEmpty()) {
			return Optional.empty();
		}

		User bdFriend = users.findById(friend.getEmail()).orElse(null);

		if (bdFriend == null || !friend.getName().equals(bdFriend.getName())) {
			return Optional.empty();
		}

		User user = result.get();

		List<User> friends = user.getFriends();

		if (friends == null) {
			friends = new ArrayList<>();
		}

		friends.add(friend);

		user.setFriends(friends);

		users.save(user);

		addFriend(bdFriend.getEmail(), user);

		return Optional.of(user);

	}

	public Optional<User> deleteFriend(String email, String friendEmail) {

		Optional<User> resultUser = users.findById(email);

		if (resultUser.isEmpty()) {
			return Optional.empty();
		}

		Optional<User> resultFriend = users.findById(friendEmail);

		if (resultFriend.isEmpty()) {
			return Optional.empty();
		}

		User user = resultFriend.get();
		User friend = resultFriend.get();

		if (user.getFriends() == null) {
			return Optional.empty();
		}

		user.getFriends().remove(friend);
		user.setFriends(user.getFriends());

		users.save(user);

		deleteFriend(friendEmail, email);

		return Optional.of(user);

	}

}