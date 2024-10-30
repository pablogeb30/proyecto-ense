package usc.etse.grei.ense.p3.project.controller;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.LinkRelationProvider;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import usc.etse.grei.ense.p3.project.handler.ResponseHandler;
import usc.etse.grei.ense.p3.project.model.*;
import usc.etse.grei.ense.p3.project.service.AssessmentService;
import usc.etse.grei.ense.p3.project.service.UserService;
import usc.etse.grei.ense.p3.project.util.SortUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * Controlador de las operaciones sobre usuarios
 */
@RestController
@RequestMapping("users")
public class UserController {

	private final UserService users;
	private final AssessmentService assessments;
	private final LinkRelationProvider relationProvider;

	@Autowired
	public UserController(UserService users, AssessmentService assessments, LinkRelationProvider relationProvider) {
		this.users = users;
		this.assessments = assessments;
		this.relationProvider = relationProvider;
	}

	/**
	 * Metodo que gestiona la operación GET /users
	 *
	 * @param page  número de página
	 * @param size  número de usuarios por página
	 * @param sort  criterio de ordenación
	 * @param email criterio de búsqueda por correo electrónico
	 * @param name  criterio de búsqueda por nombre
	 * @return respuesta HTTP
	 */
	@GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("hasRole('USER')")
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

		Result<Page<User>> result = users.get(page, size, Sort.by(criteria), filter);
		ArrayList<Link> links = new ArrayList<>();

		if (result.getResult() != null) {

			Page<User> users = result.getResult();
			Pageable metadata = users.getPageable();

			Link self = linkTo(methodOn(UserController.class).getUsers(page, size, sort, email, name)).withSelfRel();
			Link first = linkTo(methodOn(UserController.class).getUsers(metadata.first().getPageNumber(), size, sort, email, name)).withRel(IanaLinkRelations.FIRST);
			Link last = linkTo(methodOn(UserController.class).getUsers(users.getTotalPages() - 1, size, sort, email, name)).withRel(IanaLinkRelations.LAST);
			Link next = linkTo(methodOn(UserController.class).getUsers(metadata.next().getPageNumber(), size, sort, email, name)).withRel(IanaLinkRelations.NEXT);
			Link previous = linkTo(methodOn(UserController.class).getUsers(metadata.previousOrFirst().getPageNumber(), size, sort, email, name)).withRel(IanaLinkRelations.PREVIOUS);
			Link one = linkTo(methodOn(UserController.class).getUser(null)).withRel(relationProvider.getItemResourceRelFor(User.class));

			links.add(self);
			links.add(first);
			links.add(last);
			links.add(next);
			links.add(previous);
			links.add(one);

		}

		return ResponseHandler.generateResponse(result.isError(), result.getMessaje(), result.getInternalCode(), result.getResult().stream().toList(), links, result.getStatus());

	}

	/**
	 * Metodo que gestiona la operación GET /users/{email}
	 *
	 * @param email correo electrónico del usuario
	 * @return respuesta HTTP
	 */
	@GetMapping(path = "{email}", produces = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("hasRole('ADMIN') or #email == principal or @userService.areFriends(#email, principal)")
	ResponseEntity<Object> getUser(@PathVariable("email") @NotBlank @Email String email) {

		Result<User> result = users.get(email);
		ArrayList<Link> links = new ArrayList<>();

		if (result.getResult() != null) {

			Link self = linkTo(methodOn(UserController.class).getUser(email)).withSelfRel();
			Link all = linkTo(UserController.class).withRel(relationProvider.getCollectionResourceRelFor(User.class));

			links.add(self);
			links.add(all);

		}

		return ResponseHandler.generateResponse(result.isError(), result.getMessaje(), result.getInternalCode(), result.getResult(), links, result.getStatus());

	}

	/**
	 * Metodo que gestiona la operación POST /users
	 *
	 * @param user usuario añadido
	 * @return respuesa HTTP
	 */
	@PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	ResponseEntity<Object> createUser(@Validated(OnCreate.class) @RequestBody User user) {

		Result<User> result = users.create(user);
		ArrayList<Link> links = new ArrayList<>();

		if (result.getResult() != null) {

			Link self = linkTo(methodOn(UserController.class).getUser(result.getResult().getEmail())).withSelfRel();
			Link all = linkTo(UserController.class).withRel(relationProvider.getCollectionResourceRelFor(User.class));

			links.add(self);
			links.add(all);

		}

		return ResponseHandler.generateResponse(result.isError(), result.getMessaje(), result.getInternalCode(), result.getResult(), links, result.getStatus());

	}

	/**
	 * Metodo que gestiona la operación PATCH /users/{email}
	 *
	 * @param email   correo electrónico del usuario
	 * @param updates lista de operaciones de modificación
	 * @return respuesta HTTP
	 */
	@PatchMapping(path = "{email}", produces = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("#email == principal")
	ResponseEntity<Object> updateUser(@PathVariable("email") @NotBlank @Email String email, @RequestBody List<Map<String, Object>> updates) {

		Result<User> result = users.update(email, updates);
		ArrayList<Link> links = new ArrayList<>();

		if (result.getResult() != null) {

			Link self = linkTo(methodOn(UserController.class).getUser(result.getResult().getEmail())).withSelfRel();
			Link all = linkTo(UserController.class).withRel(relationProvider.getCollectionResourceRelFor(User.class));

			links.add(self);
			links.add(all);

		}

		return ResponseHandler.generateResponse(result.isError(), result.getMessaje(), result.getInternalCode(), result.getResult(), links, result.getStatus());

	}

	/**
	 * Metodo que gestiona la operación DELETE /users/{email}
	 *
	 * @param email correo electrónico del usuario
	 * @return respuesta HTTP
	 */
	@DeleteMapping(path = "{email}")
	@PreAuthorize("#email == principal")
	ResponseEntity<Object> deleteUser(@PathVariable("email") @NotBlank @Email String email) {

		Result<User> result = users.delete(email);
		ArrayList<Link> links = new ArrayList<>();

		if (result.getResult() != null) {

			Link all = linkTo(UserController.class).withRel(relationProvider.getCollectionResourceRelFor(User.class));

			links.add(all);

		}

		return ResponseHandler.generateResponse(result.isError(), result.getMessaje(), result.getInternalCode(), result.getResult(), links, result.getStatus());

	}

	/**
	 * Metodo que gestiona la operación POST /users/{email}/friends
	 *
	 * @param email  correo electrónico del usuario
	 * @param friend usuario que se añade como amigo
	 * @return respuesta HTTP
	 */
	@PostMapping(path = "{email}/friends", produces = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("#email == principal")
	ResponseEntity<Object> createFriend(@PathVariable("email") @NotBlank @Email String email, @Validated(OnRelation.class) @RequestBody User friend) {

		Result<User> result = users.createFriend(email, friend, true);
		return ResponseHandler.generateResponse(result.isError(), result.getMessaje(), result.getInternalCode(), result.getResult(), new ArrayList<>(), result.getStatus());

	}

	/**
	 * Metodo que gestiona la operación DELETE /users/{email}/friends/{friendEmail}
	 *
	 * @param email       correo electrónico del usuario
	 * @param friendEmail correo electrónico del usuario amigo
	 * @return respuesta HTTP
	 */
	@DeleteMapping(path = "{email}/friends/{friendEmail}", produces = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("#email == principal")
	ResponseEntity<Object> deleteFriend(@PathVariable("email") @NotBlank @Email String email, @PathVariable("friendEmail") @NotBlank @Email String friendEmail) {

		Result<User> result = users.deleteFriend(email, friendEmail, true);
		return ResponseHandler.generateResponse(result.isError(), result.getMessaje(), result.getInternalCode(), result.getResult(), new ArrayList<>(), result.getStatus());

	}

	/**
	 * Metodo que gestiona la operación GET /users/{userId}/assessments
	 *
	 * @param userId correo electrónico del usuario
	 * @param page   número de página
	 * @param size   número de comentarios por página
	 * @param sort   criterio de ordenación
	 * @return respuesta HTTP
	 */
	@GetMapping(path = "{userId}/assessments", produces = MediaType.APPLICATION_JSON_VALUE)
	ResponseEntity<Object> getAssessments(
			@PathVariable("userId") @NotBlank @Email String userId,
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

		Result<Page<Assessment>> result = assessments.get(page, size, Sort.by(criteria), filter);
		return ResponseHandler.generateResponse(result.isError(), result.getMessaje(), result.getInternalCode(), result.getResult(), new ArrayList<>(), result.getStatus());

	}

	/**
	 * Metodo que gestiona la operación POST /users/{userId}/assessments
	 *
	 * @param userId     correo electrónico del usuario
	 * @param assessment comentario añadido
	 * @return respuesta HTTP
	 */
	@PostMapping(path = "{userId}/assessments", produces = MediaType.APPLICATION_JSON_VALUE)
	ResponseEntity<Object> createAssessment(@PathVariable("userId") @NotBlank @Email String userId, @Validated(OnUserCreate.class) @RequestBody Assessment assessment) {

		Result<Assessment> result = assessments.createForUser(userId, assessment);
		return ResponseHandler.generateResponse(result.isError(), result.getMessaje(), result.getInternalCode(), result.getResult(), new ArrayList<>(), result.getStatus());

	}

	/**
	 * Metodo que gestiona la operación PATCH /users/{userId}/assessments/{assessmentId}
	 *
	 * @param userId       correo electrónico del usuario
	 * @param assessmentId identificador del comentario
	 * @param updates      lista de operaciones de modificación
	 * @return respuesta HTTP
	 */
	@PatchMapping(path = "{userId}/assessments/{assessmentId}", produces = MediaType.APPLICATION_JSON_VALUE)
	ResponseEntity<Object> updateAssessment(@PathVariable("userId") @NotBlank @Email String userId, @PathVariable("assessmentId") @NotBlank String assessmentId, @RequestBody List<Map<String, Object>> updates) {

		Result<Assessment> result = assessments.updateForUser(userId, assessmentId, updates);
		return ResponseHandler.generateResponse(result.isError(), result.getMessaje(), result.getInternalCode(), result.getResult(), new ArrayList<>(), result.getStatus());

	}

	/**
	 * Metodo que gestiona la operación DELETE /users/{userId}/assessments/{assessmentId}
	 *
	 * @param userId       correo electrónico del usuario
	 * @param assessmentId identificador del comentario
	 * @return respuesta HTTP
	 */
	@DeleteMapping(path = "{userId}/assessments/{assessmentId}", produces = MediaType.APPLICATION_JSON_VALUE)
	ResponseEntity<Object> deleteAssessment(@PathVariable("userId") @NotBlank @Email String userId, @PathVariable("assessmentId") @NotBlank String assessmentId) {

		Result<Assessment> result = assessments.deleteForUser(userId, assessmentId);
		return ResponseHandler.generateResponse(result.isError(), result.getMessaje(), result.getInternalCode(), result.getResult(), new ArrayList<>(), result.getStatus());

	}

}