package usc.etse.grei.ense.p3.project.controller;

import jakarta.validation.Valid;
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
import usc.etse.grei.ense.p3.project.model.Assessment;
import usc.etse.grei.ense.p3.project.model.Movie;
import usc.etse.grei.ense.p3.project.model.OnMovieCreate;
import usc.etse.grei.ense.p3.project.service.AssessmentService;
import usc.etse.grei.ense.p3.project.service.MovieService;
import usc.etse.grei.ense.p3.project.util.SortUtil;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("movies")
public class MovieController {

	private final MovieService movies;
	private final AssessmentService assessments;

	@Autowired
	public MovieController(MovieService movies, AssessmentService assessments) {
		this.movies = movies;
		this.assessments = assessments;
	}

	private EntityModel<Movie> getEntityModel() {

		Movie movie = new Movie();
		EntityModel<Movie> entityModel = EntityModel.of(movie);

		return entityModel;

	}

	@GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	ResponseEntity<Object> getMovies(
			@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "20") int size,
			@RequestParam(name = "sort", defaultValue = "") List<String> sort,
			@RequestParam(name = "search", required = false, defaultValue = "") String search
	) {

		List<Sort.Order> criteria = SortUtil.getCriteria(sort);

		ExampleMatcher matcher = ExampleMatcher
				.matchingAny()
				.withIgnoreCase()
				.withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);

		Example<Movie> filter = Example.of(
				new Movie().setTitle(search),
				matcher
		);

		Optional<Page<Movie>> dbMovies = movies.get(page, size, Sort.by(criteria), filter);

		if (dbMovies.isPresent()) {
			return ResponseHandler.generateResponse(false, "ok", 0, dbMovies.get().getContent(), getEntityModel(), HttpStatus.OK);
		} else {
			return ResponseHandler.generateResponse(true, "error", 0, null, getEntityModel(), HttpStatus.NOT_FOUND);
		}

	}

	@GetMapping(path = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	ResponseEntity<Object> getMovie(@PathVariable("id") String id) {

		Optional<Movie> result = movies.get(id);

		if (result.isPresent()) {
			return ResponseHandler.generateResponse(false, "ok", 0, result.get(), getEntityModel(), HttpStatus.OK);
		} else {
			return ResponseHandler.generateResponse(true, "error", 0, null, getEntityModel(), HttpStatus.NOT_FOUND);
		}

	}

	@PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	ResponseEntity<Object> createMovie(@Valid @RequestBody Movie movie) {

		Optional<Movie> result = movies.create(movie);

		if (result.isPresent()) {

			return ResponseHandler.generateResponse(false, "ok", 0, result, getEntityModel(), HttpStatus.CREATED);

		} else {

			return ResponseHandler.generateResponse(true, "error", 0, result, getEntityModel(), HttpStatus.CONFLICT);

		}

	}

	@PatchMapping(path = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	ResponseEntity<Object> updateMovie(@PathVariable("id") @NotBlank String id, @RequestBody List<Map<String, Object>> updates) {

		Optional<Movie> result = movies.update(id, updates);

		if (result.isPresent()) {

			return ResponseHandler.generateResponse(false, "ok", 0, result, getEntityModel(), HttpStatus.OK);

		} else {

			return ResponseHandler.generateResponse(true, "error", 0, result, getEntityModel(), HttpStatus.NOT_FOUND);

		}

	}

	@DeleteMapping(path = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	ResponseEntity<Object> deleteMovie(@PathVariable("id") @NotBlank String id) {

		Optional<Movie> result = movies.delete(id);

		if (result.isPresent()) {

			return ResponseHandler.generateResponse(false, "ok", 0, null, getEntityModel(), HttpStatus.OK);

		} else {

			return ResponseHandler.generateResponse(true, "error", 0, result, getEntityModel(), HttpStatus.NOT_FOUND);

		}

	}

	@GetMapping(path = "{id}/assessments", produces = MediaType.APPLICATION_JSON_VALUE)
	ResponseEntity<Object> getAssessments(
			@PathVariable("id") @NotBlank String id,
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
				new Assessment().setMovie(new Movie().setId(id)),
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
	ResponseEntity<Object> createAssessment(@PathVariable("id") @NotBlank String movieId, @Validated(OnMovieCreate.class) @RequestBody Assessment assessment) {

		Optional<Assessment> createResult = assessments.createForMovie(movieId, assessment);

		if (createResult.isPresent()) {
			return ResponseHandler.generateResponse(false, "ok", 0, createResult, getEntityModel(), HttpStatus.CREATED);
		} else {
			return ResponseHandler.generateResponse(true, "error", 0, createResult, getEntityModel(), HttpStatus.CONFLICT);
		}

	}

	@PatchMapping(path = "{movieId}/assessment/{assessmentId}", produces = MediaType.APPLICATION_JSON_VALUE)
	ResponseEntity<Object> updateAssessment(@PathVariable("movieId") @NotBlank String movieId, @PathVariable("assessmentId") @NotBlank String assessmentId, @RequestBody List<Map<String, Object>> updates) {

		Optional<Assessment> result = assessments.update(assessmentId, updates);

		if (result.isPresent()) {

			return ResponseHandler.generateResponse(false, "ok", 0, result, getEntityModel(), HttpStatus.OK);

		} else {

			return ResponseHandler.generateResponse(true, "error", 0, result, getEntityModel(), HttpStatus.NOT_FOUND);

		}

	}

	@DeleteMapping(path = "{movieId}/assessments/{assessmentId}", produces = MediaType.APPLICATION_JSON_VALUE)
	ResponseEntity<Object> deleteAssessment(@PathVariable("movieId") @NotBlank String movieId, @PathVariable("assessmentId") @NotBlank String assessmentId) {

		Optional<Assessment> createResult = assessments.delete(assessmentId);

		if (createResult.isPresent()) {
			return ResponseHandler.generateResponse(false, "ok", 0, createResult, getEntityModel(), HttpStatus.OK);
		} else {
			return ResponseHandler.generateResponse(true, "error", 0, createResult, getEntityModel(), HttpStatus.NOT_FOUND);
		}

	}

}