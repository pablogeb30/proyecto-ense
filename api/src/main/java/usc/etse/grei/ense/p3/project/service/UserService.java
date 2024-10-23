package usc.etse.grei.ense.p3.project.service;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import usc.etse.grei.ense.p3.project.model.Date;
import usc.etse.grei.ense.p3.project.model.*;
import usc.etse.grei.ense.p3.project.repository.AssessmentRepository;
import usc.etse.grei.ense.p3.project.repository.UserRepository;
import usc.etse.grei.ense.p3.project.util.PatchUtil;

import java.time.LocalDate;
import java.util.*;

/**
 * Servicio que implementa la lógica de negocio para usuarios
 */
@Service
public class UserService {

	private final UserRepository users;
	private final MongoTemplate mongo;
	private final PatchUtil patchUtil;
	private final AssessmentRepository assessments;
	private final Validator validator;
	private final PasswordEncoder encoder;

	@Autowired
	public UserService(UserRepository users, MongoTemplate mongo, PatchUtil patchUtil, AssessmentRepository assessments, Validator validator, PasswordEncoder encoder) {
		this.users = users;
		this.mongo = mongo;
		this.patchUtil = patchUtil;
		this.assessments = assessments;
		this.validator = validator;
		this.encoder = encoder;
	}

	/**
	 * Metodo que obtiene una lista de usuarios utilizando filtrado y ordenación
	 *
	 * @param page   número de página
	 * @param size   número de usuarios por página
	 * @param sort   criterio de ordenación
	 * @param filter criterio de filtrado por nombre o dirección de correo
	 * @return resultado de la búsqueda
	 */
	public Result<List<User>> get(int page, int size, Sort sort, Example<User> filter) {

		Pageable request = PageRequest.of(page, size, sort);

		Criteria criteria = Criteria.byExample(filter);

		Query query = Query.query(criteria).with(request);
		query.fields().include("name", "country", "birthday", "picture");

		List<User> result = mongo.find(query, User.class);

		return new Result<>(result, false, "Users data", 0, Result.Code.OK);

	}

	/**
	 * Metodo que obtiene un usuario a partir de su email
	 *
	 * @param email correo electrónico del usuario
	 * @return resultado de la búsqueda
	 */
	public Result<User> get(String email) {

		User result = users.findById(email).orElse(null);

		if (result == null) {
			return new Result<>(null, false, "No user", 0, Result.Code.NOT_FOUND);
		}

		return new Result<>(result, false, "User data", 0, Result.Code.OK);

	}

	/**
	 * Metodo que añade un usuario a la base de datos
	 *
	 * @param user usuario añadido
	 * @return resultado de la inserción
	 */
	public Result<User> create(User user) {

		try {

			User existingUser = users.findById(user.getEmail()).orElse(null);

			if (existingUser != null) {
				return new Result<>(null, false, "User already exists", 0, Result.Code.CONFLICT);
			}

			Date date = user.getBirthday();

			LocalDate birthday = LocalDate.of(date.getYear(), date.getMonth(), date.getDay());

			if (birthday.isAfter(LocalDate.now())) {
				return new Result<>(null, true, "Invalid birthday", 0, Result.Code.BAD_REQUEST);
			}

			user.setPassword(encoder.encode(user.getPassword()));

			ArrayList<String> roles = new ArrayList<>();
			roles.add("ROLE_USER");
			user.setRoles(roles);

			User newUser = users.insert(user);
			return new Result<>(newUser, false, "User created", 0, Result.Code.CREATED);

		} catch (Exception e) {

			return new Result<>(null, true, e.getLocalizedMessage(), 0, Result.Code.BAD_REQUEST);

		}

	}

	/**
	 * Metodo que modifica la información de un usuario existente
	 *
	 * @param email      correo electrónico del usuario
	 * @param operations lista de operaciones de modificación
	 * @return resultado de la modificación
	 */
	public Result<User> update(String email, List<Map<String, Object>> operations) {

		try {

			User originalUser = users.findById(email).orElse(null);

			if (originalUser == null) {
				return new Result<>(null, false, "No user", 0, Result.Code.NOT_FOUND);
			}

			operations.removeIf(op -> op.containsKey("path") && (op.get("path").equals("/email") || op.get("path").equals("/birthday") || ((String) op.get("path")).startsWith("/friends") || ((String) op.get("path")).startsWith("/roles")));

			User filteredUser = patchUtil.patch(originalUser, operations);
			List<User> friendsCopy = filteredUser.getFriends();
			filteredUser.setFriends(null);

			Set<ConstraintViolation<User>> violations = validator.validate(filteredUser, OnUpdate.class);

			if (!violations.isEmpty()) {
				return new Result<>(null, true, "Not valid due to violations", 0, Result.Code.BAD_REQUEST);
			}

			filteredUser.setFriends(friendsCopy);

			if (!filteredUser.getPassword().equals(originalUser.getPassword())) {
				filteredUser.setPassword(encoder.encode(filteredUser.getPassword()));
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

	/**
	 * Metodo que elimina un usuario de la base de datos
	 *
	 * @param email correo electrónico del usuario
	 * @return resultado de la eliminación
	 */
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

	/**
	 * Metodo que añade un usuario a la lista de amigos de otro usuario
	 *
	 * @param email  correo electrónico del usuario actual
	 * @param friend usuario añadido a la lista de amigos del usuario actual
	 * @param redo   booleano que indica si la inserción se realiza en ambas direcciones
	 * @return resultado de la inserción
	 */
	public Result<User> createFriend(String email, User friend, boolean redo) {

		User user = users.findById(email).orElse(null);

		if (user == null) {
			return new Result<>(null, false, "No user", 0, Result.Code.NOT_FOUND);
		}

		User bdFriend = users.findById(friend.getEmail()).orElse(null);

		if (bdFriend == null || !bdFriend.getName().equals(friend.getName())) {
			return new Result<>(null, false, "No friend", 0, Result.Code.NOT_FOUND);
		}

		if (user.equals(bdFriend)) {
			return new Result<>(null, false, "Recursive adition", 0, Result.Code.BAD_REQUEST);
		}

		List<User> friends = user.getFriends();

		if (friends == null) {
			friends = new ArrayList<>();
		}

		if (friends.stream().anyMatch(f -> f.getEmail().equals(bdFriend.getEmail()))) {
			return new Result<>(null, false, "Friend already added", 0, Result.Code.BAD_REQUEST);
		}

		friends.add(friend);

		user.setFriends(friends);

		users.save(user);

		if (redo) {
			User parseUser = new User().setEmail(user.getEmail()).setName(user.getName());
			createFriend(bdFriend.getEmail(), parseUser, false);
		}

		return new Result<>(user, false, "Friend added", 0, Result.Code.CREATED);

	}

	/**
	 * Metodo que elimina a un usuario de la lista de amigos de otro usuario
	 *
	 * @param email       correo electrónico del usuario actual
	 * @param friendEmail correo electrónico del usuario eliminado de la lista de amigos
	 * @param redo        booleano que indica si la eliminación se realiza en ambas direcciones
	 * @return resultado de la eliminación
	 */
	public Result<User> deleteFriend(String email, String friendEmail, boolean redo) {

		User user = users.findById(email).orElse(null);

		if (user == null) {
			return new Result<>(null, false, "No user", 0, Result.Code.NOT_FOUND);
		}

		List<User> friends = user.getFriends();

		if (friends == null) {
			return new Result<>(null, false, "No friends", 0, Result.Code.NOT_FOUND);
		}

		User friend = users.findById(friendEmail).orElse(null);

		if (friend == null) {
			return new Result<>(null, false, "No friend", 0, Result.Code.NOT_FOUND);
		}

		if (friends.stream().noneMatch(f -> f.getEmail().equals(friendEmail))) {
			return new Result<>(null, false, "No friend", 0, Result.Code.NOT_FOUND);
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