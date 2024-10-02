package usc.etse.grei.ense.p3.project.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import usc.etse.grei.ense.p3.project.model.User;
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

	@Autowired
	public UserService(UserRepository users, MongoTemplate mongo, PatchUtil patchUtil) {
		this.users = users;
		this.mongo = mongo;
		this.patchUtil = patchUtil;
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

		Optional<User> user = users.findById(email);

		if (!user.isPresent()) {
			return Optional.empty();
		}

		try {

			operations.removeIf(op -> op.containsKey("path") && (op.get("path").equals("/email") || op.get("path").equals("/birthday")));

			User filteredUser = patchUtil.patch(user.get(), operations);
			User updatedUser = users.save(filteredUser);
			return Optional.of(updatedUser);

		} catch (Exception e) {

			return Optional.empty();

		}

	}

	public Optional<User> delete(String email) {
		User user = users.findById(email).orElse(null);

		if (user == null) {
			return Optional.empty();
		}

		users.delete(user);
		return Optional.of(user);

	}

	public Optional<User> addFriend(String email, User friend) {

		User user = users.findById(email).orElse(null);

		if (user == null) {
			return Optional.empty();
		}

		User bdFriend = users.findById(friend.getEmail()).orElse(null);

		if (bdFriend == null || !friend.getName().equals(bdFriend.getName())) {
			return Optional.empty();
		}

		List<User> friends = user.getFriends();

		if (friends == null) {
			friends = new ArrayList<>();
		}

		friends.add(friend);

		user.setFriends(friends);

		return Optional.of(user);

	}

	public Optional<User> deleteFriend(String email, String friendEmail) {

		User user = users.findById(email).orElse(null);

		if (user == null) {
			return Optional.empty();
		}

		User friend = users.findById(friendEmail).orElse(null);

		if (friend == null) {
			return Optional.empty();
		}

		if (friend.getFriends() == null) {
			return Optional.empty();
		}

		user.getFriends().remove(friend);

		return Optional.of(user);

	}

}