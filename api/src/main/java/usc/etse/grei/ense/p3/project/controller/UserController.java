package usc.etse.grei.ense.p3.project.controller;

import jakarta.validation.constraints.Email;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import usc.etse.grei.ense.p3.project.handler.ResponseHandler;
import usc.etse.grei.ense.p3.project.model.OnCreate;
import usc.etse.grei.ense.p3.project.model.OnUpdate;
import usc.etse.grei.ense.p3.project.model.User;
import usc.etse.grei.ense.p3.project.service.UserService;
import usc.etse.grei.ense.p3.project.util.SortUtil;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("users")
public class UserController {

	private final UserService users;

	@Autowired
	public UserController(UserService users) {
		this.users = users;
	}

	private EntityModel<User> getEntityModel() {

		User user = new User();
		EntityModel<User> entityModel = EntityModel.of(user);

		entityModel.add(linkTo(methodOn(UserController.class).getUser("email@email.com")).withSelfRel());
		entityModel.add(linkTo(methodOn(UserController.class).getUsers(0, 0, null, null)).withRel("users"));

		return entityModel;

	}

	@GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	ResponseEntity<Object> getUsers(
			@RequestParam(name = "page", required = false, defaultValue = "0") int page,
			@RequestParam(name = "size", required = false, defaultValue = "20") int size,
			@RequestParam(name = "sort", required = false, defaultValue = "") List<String> sort,
			@RequestParam(name = "search", required = false, defaultValue = "") String search
	) {

		List<Sort.Order> criteria = SortUtil.getCriteria(sort);

		ExampleMatcher matcher = ExampleMatcher
				.matchingAny()
				.withIgnoreCase()
				.withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);

		Example<User> filter = Example.of(
				new User().setEmail(search).setName(search),
				matcher
		);

		Optional<Page<User>> dbUsers = users.get(page, size, Sort.by(criteria), filter);
		List<User> userList = dbUsers.get().getContent();

		if (dbUsers.isPresent()) {
			return ResponseHandler.generateResponse(false, "ok", 0, userList, getEntityModel(), HttpStatus.ACCEPTED);
		} else {
			return ResponseHandler.generateResponse(true, "ok", 0, userList, getEntityModel(), HttpStatus.NOT_FOUND);
		}

	}

	@GetMapping(path = "{email}", produces = MediaType.APPLICATION_JSON_VALUE)
	ResponseEntity<EntityModel<User>> getUser(@PathVariable("email") @Email String email) {

		Optional<User> user = users.get(email);

		if (user.isPresent()) {

			return ResponseEntity.ok(getEntityModel());

		} else {

			return ResponseEntity.notFound().build();

		}

	}

	@PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	ResponseEntity<User> createUser(@Validated(OnCreate.class) @RequestBody User user) {

		Optional<User> result = users.create(user);

		if (result.isPresent()) {

			return ResponseEntity.of(result);

		} else {

			return ResponseEntity.status(409).build();

		}

	}

	@PatchMapping(path = "{email}", produces = MediaType.APPLICATION_JSON_VALUE)
	ResponseEntity<EntityModel<User>> updateUser(@PathVariable("email") @Email String email, @RequestBody List<Map<String, Object>> updates) {

		Optional<User> user = users.update(email, updates);

		if (user.isPresent()) {

			return ResponseEntity.ok(getEntityModel());

		} else {

			return ResponseEntity.notFound().build();

		}

	}

	@DeleteMapping(path = "{email}")
	ResponseEntity<Void> deleteUser(@PathVariable("email") @Email String email) {

		Optional<User> result = users.delete(email);

		if (result.isPresent()) {

			return ResponseEntity.noContent().build();

		} else {

			return ResponseEntity.notFound().build();

		}

	}

	@PostMapping(path = "{email}/friends", produces = MediaType.APPLICATION_JSON_VALUE)
	ResponseEntity<EntityModel<User>> updateFriends(@PathVariable("email") @Email String email, @Validated(OnUpdate.class) @RequestBody User friend) {

		Optional<User> result = users.addFriend(email, friend);

		if (result.isPresent()) {

			return ResponseEntity.ok(getEntityModel());

		} else {

			return ResponseEntity.notFound().build();

		}

	}

	@DeleteMapping(path = "{email}/friends/{friendEmail}", produces = MediaType.APPLICATION_JSON_VALUE)
	ResponseEntity<EntityModel<User>> updateFriends(@PathVariable("email") @Email String email, @PathVariable("friendEmail") @Email String friendEmail) {

		Optional<User> result = users.deleteFriend(email, friendEmail);

		if (result.isPresent()) {

			return ResponseEntity.noContent().build();

		} else {

			return ResponseEntity.notFound().build();

		}

	}

}