package usc.etse.grei.ense.p3.project.controller;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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
import usc.etse.grei.ense.p3.project.model.*;
import usc.etse.grei.ense.p3.project.service.AssessmentService;
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
	private final AssessmentService assessments;

	@Autowired
	public UserController(UserService users, AssessmentService assessments) {
		this.users = users;
		this.assessments = assessments;
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

		if (dbUsers.isPresent()) {

			List<User> userList = dbUsers.get().getContent();

			return ResponseHandler.generateResponse(false, "ok", 0, userList, getEntityModel(), HttpStatus.ACCEPTED);

		} else {

			return ResponseHandler.generateResponse(true, "error", 0, null, getEntityModel(), HttpStatus.NOT_FOUND);

		}

	}

	@GetMapping(path = "{email}", produces = MediaType.APPLICATION_JSON_VALUE)
	ResponseEntity<Object> getUser(@PathVariable("email") @Email String email) {

		Optional<User> user = users.get(email);

		if (user.isPresent()) {

			return ResponseHandler.generateResponse(false, "ok", 0, user.get(), getEntityModel(), HttpStatus.ACCEPTED);

		} else {

			return ResponseHandler.generateResponse(true, "error", 0, null, getEntityModel(), HttpStatus.NOT_FOUND);

		}

	}

	@PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	ResponseEntity<Object> createUser(@Validated(OnCreate.class) @RequestBody User user) {

		Optional<User> result = users.create(user);

		if (result.isPresent()) {

			return ResponseHandler.generateResponse(false, "ok", 0, result.get(), getEntityModel(), HttpStatus.ACCEPTED);

		} else {

			return ResponseHandler.generateResponse(true, "error", 0, null, getEntityModel(), HttpStatus.CONFLICT);

		}

	}

	@PatchMapping(path = "{email}", produces = MediaType.APPLICATION_JSON_VALUE)
	ResponseEntity<Object> updateUser(@PathVariable("email") @Email String email, @RequestBody List<Map<String, Object>> updates) {

		Optional<User> result = users.update(email, updates);

		if (result.isPresent()) {

			return ResponseHandler.generateResponse(false, "ok", 0, result.get(), getEntityModel(), HttpStatus.OK);

		} else {

			return ResponseHandler.generateResponse(true, "error", 0, null, getEntityModel(), HttpStatus.NOT_FOUND);

		}

	}

	@DeleteMapping(path = "{email}")
	ResponseEntity<Object> deleteUser(@PathVariable("email") @Email String email) {

		Optional<User> result = users.delete(email);

		if (result.isPresent()) {

			return ResponseHandler.generateResponse(false, "ok", 0, result.get(), getEntityModel(), HttpStatus.OK);

		} else {

			return ResponseHandler.generateResponse(true, "error", 0, null, getEntityModel(), HttpStatus.NOT_FOUND);

		}

	}

	@PostMapping(path = "{email}/friends", produces = MediaType.APPLICATION_JSON_VALUE)
	ResponseEntity<Object> addFriend(@PathVariable("email") @Email String email, @Validated(OnRelation.class) @RequestBody User friend) {

		Optional<User> result = users.addFriend(email, friend);

		if (result.isPresent()) {

			return ResponseHandler.generateResponse(false, "ok", 0, result.get(), getEntityModel(), HttpStatus.OK);

		} else {

			return ResponseHandler.generateResponse(true, "error", 0, null, getEntityModel(), HttpStatus.NOT_FOUND);

		}

	}

	@DeleteMapping(path = "{email}/friends/{friendEmail}", produces = MediaType.APPLICATION_JSON_VALUE)
	ResponseEntity<Object> deleteFriend(@PathVariable("email") @Email String email, @PathVariable("friendEmail") @Email String friendEmail) {

		Optional<User> result = users.deleteFriend(email, friendEmail);

		if (result.isPresent()) {

			return ResponseHandler.generateResponse(false, "ok", 0, result.get(), getEntityModel(), HttpStatus.OK);

		} else {

			return ResponseHandler.generateResponse(true, "error", 0, null, getEntityModel(), HttpStatus.NOT_FOUND);

		}

	}

	@GetMapping(path = "{id}/assessments", produces = MediaType.APPLICATION_JSON_VALUE)
	ResponseEntity<Object> getAssessments(
			@PathVariable("id") @NotBlank String userId,
			@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "20") int size,
			@RequestParam(name = "sort", defaultValue = "") List<String> sort
	) {

		List<Sort.Order> criteria = SortUtil.getCriteria(sort);

		ExampleMatcher matcher = ExampleMatcher
				.matchingAny()
				.withIgnoreCase()
				.withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);

		Example<Assessment> filter = Example.of(
				new Assessment().setUser(new User().setEmail(userId)),
				matcher
		);

		Optional<Page<Assessment>> dbAssessments = assessments.get(page, size, Sort.by(criteria), filter);

		if (dbAssessments.isPresent()) {
			return ResponseHandler.generateResponse(false, "ok", 0, dbAssessments.get().getContent(), getEntityModel(), HttpStatus.OK);
		} else {
			return ResponseHandler.generateResponse(true, "error", 0, null, getEntityModel(), HttpStatus.NOT_FOUND);
		}

	}

	@PostMapping(path = "{id}/assessments", produces = MediaType.APPLICATION_JSON_VALUE)
	ResponseEntity<Object> createAssessment(@PathVariable("id") @NotBlank String userId, @Validated(OnUserCreate.class) @RequestBody Assessment assessment) {

		Optional<Assessment> createResult = assessments.createForUser(userId, assessment);

		if (createResult.isPresent()) {
			return ResponseHandler.generateResponse(false, "ok", 0, createResult, getEntityModel(), HttpStatus.CREATED);
		} else {
			return ResponseHandler.generateResponse(true, "error", 0, createResult, getEntityModel(), HttpStatus.CONFLICT);
		}

	}

	@PatchMapping(path = "{userId}/assessment/{assessmentId}", produces = MediaType.APPLICATION_JSON_VALUE)
	ResponseEntity<Object> updateAssessment(@PathVariable("userId") @NotBlank String userId, @PathVariable("assessmentId") @NotBlank String assessmentId, @RequestBody List<Map<String, Object>> updates) {

		Optional<Assessment> result = assessments.update(assessmentId, updates);

		if (result.isPresent()) {

			return ResponseHandler.generateResponse(false, "ok", 0, result, getEntityModel(), HttpStatus.OK);

		} else {

			return ResponseHandler.generateResponse(true, "error", 0, result, getEntityModel(), HttpStatus.NOT_FOUND);

		}

	}

	@DeleteMapping(path = "{userId}/assessments/{assessmentId}", produces = MediaType.APPLICATION_JSON_VALUE)
	ResponseEntity<Object> deleteAssessment(@PathVariable("userId") @NotBlank String userId, @PathVariable("assessmentId") @NotBlank String assessmentId) {

		Optional<Assessment> createResult = assessments.delete(assessmentId);

		if (createResult.isPresent()) {
			return ResponseHandler.generateResponse(false, "ok", 0, createResult, getEntityModel(), HttpStatus.OK);
		} else {
			return ResponseHandler.generateResponse(true, "error", 0, createResult, getEntityModel(), HttpStatus.NOT_FOUND);
		}

	}

}