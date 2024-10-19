package usc.etse.grei.ense.p3.project.controller;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Sort;
import org.springframework.hateoas.EntityModel;
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
		entityModel.add(linkTo(methodOn(UserController.class).getUsers(0, 0, null, "", "")).withRel("users"));

		return entityModel;

	}

	@GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	ResponseEntity<Object> getUsers(
			@RequestParam(name = "page", required = false, defaultValue = "0") int page,
			@RequestParam(name = "size", required = false, defaultValue = "20") int size,
			@RequestParam(name = "sort", required = false, defaultValue = "") List<String> sort,
			@RequestParam(name = "email", required = false, defaultValue = "") String email,
			@RequestParam(name = "name", required = false, defaultValue = "") String name
	) {

		List<Sort.Order> criteria = SortUtil.getCriteria(sort);

		ExampleMatcher matcher = ExampleMatcher
				.matchingAll()
				.withIgnoreCase()
				.withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);

		Example<User> filter = Example.of(
				new User().setEmail(email).setName(name),
				matcher
		);

		Result<List<User>> result = users.get(page, size, Sort.by(criteria), filter);
		return ResponseHandler.generateResponse(result.isError(), result.getMessaje(), result.getInternalCode(), result.getResult(), getEntityModel(), result.getStatus());

	}

	@GetMapping(path = "{email}", produces = MediaType.APPLICATION_JSON_VALUE)
	ResponseEntity<Object> getUser(@PathVariable("email") @Email String email) {

		Result<User> result = users.get(email);
		return ResponseHandler.generateResponse(result.isError(), result.getMessaje(), result.getInternalCode(), result.getResult(), getEntityModel(), result.getStatus());

	}

	@PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	ResponseEntity<Object> createUser(@Validated(OnCreate.class) @RequestBody User user) {

		Result<User> result = users.create(user);
		return ResponseHandler.generateResponse(result.isError(), result.getMessaje(), result.getInternalCode(), result.getResult(), getEntityModel(), result.getStatus());

	}

	@PatchMapping(path = "{email}", produces = MediaType.APPLICATION_JSON_VALUE)
	ResponseEntity<Object> updateUser(@PathVariable("email") @Email String email, @RequestBody List<Map<String, Object>> updates) {

		Result<User> result = users.update(email, updates);
		return ResponseHandler.generateResponse(result.isError(), result.getMessaje(), result.getInternalCode(), result.getResult(), getEntityModel(), result.getStatus());

	}

	@DeleteMapping(path = "{email}")
	ResponseEntity<Object> deleteUser(@PathVariable("email") @Email String email) {

		Result<User> result = users.delete(email);
		return ResponseHandler.generateResponse(result.isError(), result.getMessaje(), result.getInternalCode(), result.getResult(), getEntityModel(), result.getStatus());

	}

	@PostMapping(path = "{email}/friends", produces = MediaType.APPLICATION_JSON_VALUE)
	ResponseEntity<Object> addFriend(@PathVariable("email") @Email String email, @Validated(OnRelation.class) @RequestBody User friend) {

		Result<User> result = users.addFriend(email, friend, true);
		return ResponseHandler.generateResponse(result.isError(), result.getMessaje(), result.getInternalCode(), result.getResult(), getEntityModel(), result.getStatus());


	}

	@DeleteMapping(path = "{email}/friends/{friendEmail}", produces = MediaType.APPLICATION_JSON_VALUE)
	ResponseEntity<Object> deleteFriend(@PathVariable("email") @Email String email, @PathVariable("friendEmail") @Email String friendEmail) {

		Result<User> result = users.deleteFriend(email, friendEmail, true);
		return ResponseHandler.generateResponse(result.isError(), result.getMessaje(), result.getInternalCode(), result.getResult(), getEntityModel(), result.getStatus());

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

		Result<List<Assessment>> result = assessments.get(page, size, Sort.by(criteria), filter);
		return ResponseHandler.generateResponse(result.isError(), result.getMessaje(), result.getInternalCode(), result.getResult(), getEntityModel(), result.getStatus());

	}

	@PostMapping(path = "{id}/assessments", produces = MediaType.APPLICATION_JSON_VALUE)
	ResponseEntity<Object> createAssessment(@PathVariable("id") @NotBlank String userId, @Validated(OnUserCreate.class) @RequestBody Assessment assessment) {

		Result<Assessment> result = assessments.createForUser(userId, assessment);
		return ResponseHandler.generateResponse(result.isError(), result.getMessaje(), result.getInternalCode(), result.getResult(), getEntityModel(), result.getStatus());

	}

	@PatchMapping(path = "{userId}/assessments/{assessmentId}", produces = MediaType.APPLICATION_JSON_VALUE)
	ResponseEntity<Object> updateAssessment(@PathVariable("userId") @NotBlank String userId, @PathVariable("assessmentId") @NotBlank String assessmentId, @RequestBody List<Map<String, Object>> updates) {

		Result<Assessment> result = assessments.update(assessmentId, updates);
		return ResponseHandler.generateResponse(result.isError(), result.getMessaje(), result.getInternalCode(), result.getResult(), getEntityModel(), result.getStatus());

	}

	@DeleteMapping(path = "{userId}/assessments/{assessmentId}", produces = MediaType.APPLICATION_JSON_VALUE)
	ResponseEntity<Object> deleteAssessment(@PathVariable("userId") @NotBlank String userId, @PathVariable("assessmentId") @NotBlank String assessmentId) {

		Result<Assessment> result = assessments.delete(assessmentId);
		return ResponseHandler.generateResponse(result.isError(), result.getMessaje(), result.getInternalCode(), result.getResult(), getEntityModel(), result.getStatus());

	}

}