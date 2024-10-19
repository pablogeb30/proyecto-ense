package usc.etse.grei.ense.p3.project.controller;

import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
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
import usc.etse.grei.ense.p3.project.service.MovieService;
import usc.etse.grei.ense.p3.project.util.SortUtil;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
			@RequestParam(name = "page", required = false, defaultValue = "0") int page,
			@RequestParam(name = "size", required = false, defaultValue = "20") int size,
			@RequestParam(name = "sort", required = false, defaultValue = "") List<String> sort,
			@RequestParam(name = "keywords", required = false, defaultValue = "") List<String> keywords,
			@RequestParam(name = "genres", required = false, defaultValue = "") List<String> genres,
			@RequestParam(name = "releaseDate", required = false, defaultValue = "") String releaseDate,
			@RequestParam(name = "cast", required = false, defaultValue = "") List<String> cast,
			@RequestParam(name = "crew", required = false, defaultValue = "") List<String> crew
	) {

		List<Sort.Order> criteria = SortUtil.getCriteria(sort);

		ExampleMatcher matcher = ExampleMatcher
				.matchingAll()
				.withIgnoreCase()
				.withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);

		Movie filterMovie = new Movie();

		if (!genres.isEmpty()) {
			filterMovie.setGenres(genres);
		}

		if (!keywords.isEmpty()) {
			filterMovie.setKeywords(keywords);
		}

		Date parsedDate = null;

		if (!releaseDate.isBlank()) {

			try {

				LocalDate date = LocalDate.parse(releaseDate);

				parsedDate = new Date(date.getDayOfMonth(), date.getMonthValue(), date.getYear());

				filterMovie.setReleaseDate(parsedDate);

			} catch (Exception e) {

				return ResponseHandler.generateResponse(true, e.getMessage(), 0, null, getEntityModel(), HttpStatus.BAD_REQUEST);

			}

		}

		List<Cast> castList = new ArrayList<>();

		if (!cast.isEmpty()) {

			for (String castString : cast) {

				Cast newCast = new Cast();

				String[] parts = castString.split("-", 3);

				if (parts.length != 3) {
					return ResponseHandler.generateResponse(true, "Invalid cast", 0, null, getEntityModel(), HttpStatus.BAD_REQUEST);
				}

				if (!parts[0].equals("*")) {
					newCast.setId(parts[0]);
				}

				if (!parts[1].equals("*")) {
					newCast.setName(parts[1]);
				}

				if (!parts[2].equals("*")) {
					newCast.setCharacter(parts[2]);
				}

				castList.add(newCast);

			}

		}

		List<Crew> crewList = new ArrayList<>();

		if (!crew.isEmpty()) {

			for (String crewString : crew) {

				Crew newCrew = new Crew();

				String[] parts = crewString.split("-", 3);

				if (parts.length != 3) {
					return ResponseHandler.generateResponse(true, "Invalid crew", 0, null, getEntityModel(), HttpStatus.BAD_REQUEST);
				}

				if (!parts[0].equals("*")) {
					newCrew.setId(parts[0]);
				}

				if (!parts[1].equals("*")) {
					newCrew.setName(parts[1]);
				}

				if (!parts[2].equals("*")) {
					newCrew.setJob(parts[2]);
				}

				crewList.add(newCrew);

			}

		}

		Example<Movie> filter = Example.of(
				filterMovie,
				matcher
		);

		Result<List<Movie>> result = movies.get(page, size, Sort.by(criteria), filter, castList, crewList);
		return ResponseHandler.generateResponse(result.isError(), result.getMessaje(), result.getInternalCode(), result.getResult(), getEntityModel(), result.getStatus());

	}

	@GetMapping(path = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	ResponseEntity<Object> getMovie(@PathVariable("id") String id) {

		Result<Movie> result = movies.get(id);
		return ResponseHandler.generateResponse(result.isError(), result.getMessaje(), result.getInternalCode(), result.getResult(), getEntityModel(), result.getStatus());

	}

	@PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	ResponseEntity<Object> createMovie(@Validated(OnCreate.class) @RequestBody Movie movie) {

		Result<Movie> result = movies.create(movie);
		return ResponseHandler.generateResponse(result.isError(), result.getMessaje(), result.getInternalCode(), result.getResult(), getEntityModel(), result.getStatus());

	}

	@PatchMapping(path = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	ResponseEntity<Object> updateMovie(@PathVariable("id") @NotBlank String id, @RequestBody List<Map<String, Object>> updates) {

		Result<Movie> result = movies.update(id, updates);
		return ResponseHandler.generateResponse(result.isError(), result.getMessaje(), result.getInternalCode(), result.getResult(), getEntityModel(), result.getStatus());

	}

	@DeleteMapping(path = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	ResponseEntity<Object> deleteMovie(@PathVariable("id") @NotBlank String id) {

		Result<Movie> result = movies.delete(id);
		return ResponseHandler.generateResponse(result.isError(), result.getMessaje(), result.getInternalCode(), result.getResult(), getEntityModel(), result.getStatus());

	}

	@PostMapping(path = "{id}/cast", produces = MediaType.APPLICATION_JSON_VALUE)
	ResponseEntity<Object> createCast(@PathVariable("id") @NotBlank String id, @RequestBody @Validated(OnRelation.class) Cast cast) {

		Result<Cast> result = movies.createCast(id, cast);
		return ResponseHandler.generateResponse(result.isError(), result.getMessaje(), result.getInternalCode(), result.getResult(), getEntityModel(), result.getStatus());

	}

	@PatchMapping(path = "{id}/cast/{castId}", produces = MediaType.APPLICATION_JSON_VALUE)
	ResponseEntity<Object> updateCast(@PathVariable("id") @NotBlank String id, @PathVariable("castId") @NotBlank String castId, @RequestBody List<Map<String, Object>> updates) {

		Result<Cast> result = movies.updateCast(id, castId, updates);
		return ResponseHandler.generateResponse(result.isError(), result.getMessaje(), result.getInternalCode(), result.getResult(), getEntityModel(), result.getStatus());

	}

	@DeleteMapping(path = "{id}/cast/{castId}", produces = MediaType.APPLICATION_JSON_VALUE)
	ResponseEntity<Object> deleteCast(@PathVariable("id") @NotBlank String id, @PathVariable("castId") @NotBlank String castId) {

		Result<Cast> result = movies.deleteCast(id, castId);
		return ResponseHandler.generateResponse(result.isError(), result.getMessaje(), result.getInternalCode(), result.getResult(), getEntityModel(), result.getStatus());

	}

	@PostMapping(path = "{id}/crew", produces = MediaType.APPLICATION_JSON_VALUE)
	ResponseEntity<Object> createCrew(@PathVariable("id") @NotBlank String id, @RequestBody @Validated(OnRelation.class) Crew crew) {

		Result<Crew> result = movies.createCrew(id, crew);
		return ResponseHandler.generateResponse(result.isError(), result.getMessaje(), result.getInternalCode(), result.getResult(), getEntityModel(), result.getStatus());

	}

	@PatchMapping(path = "{id}/crew/{crewId}", produces = MediaType.APPLICATION_JSON_VALUE)
	ResponseEntity<Object> updateCrew(@PathVariable("id") @NotBlank String id, @PathVariable("crewId") @NotBlank String crewId, @RequestBody List<Map<String, Object>> updates) {

		Result<Crew> result = movies.updateCrew(id, crewId, updates);
		return ResponseHandler.generateResponse(result.isError(), result.getMessaje(), result.getInternalCode(), result.getResult(), getEntityModel(), result.getStatus());

	}

	@DeleteMapping(path = "{id}/crew/{crewId}", produces = MediaType.APPLICATION_JSON_VALUE)
	ResponseEntity<Object> deleteCrew(@PathVariable("id") @NotBlank String id, @PathVariable("crewId") @NotBlank String crewId) {

		Result<Crew> result = movies.deleteCrew(id, crewId);
		return ResponseHandler.generateResponse(result.isError(), result.getMessaje(), result.getInternalCode(), result.getResult(), getEntityModel(), result.getStatus());

	}

	@GetMapping(path = "{movieId}/assessments", produces = MediaType.APPLICATION_JSON_VALUE)
	ResponseEntity<Object> getAssessments(
			@PathVariable("movieId") @NotBlank String movieId,
			@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "20") int size,
			@RequestParam(name = "sort", defaultValue = "") List<String> sort
	) {

		List<Sort.Order> criteria = SortUtil.getCriteria(sort);

		ExampleMatcher matcher = ExampleMatcher
				.matchingAll()
				.withIgnoreCase()
				.withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);

		Example<Assessment> filter = Example.of(
				new Assessment().setMovie(new Movie().setId(movieId)),
				matcher
		);

		Result<List<Assessment>> result = assessments.get(page, size, Sort.by(criteria), filter);
		return ResponseHandler.generateResponse(result.isError(), result.getMessaje(), result.getInternalCode(), result.getResult(), getEntityModel(), result.getStatus());

	}

	@PostMapping(path = "{movieId}/assessments", produces = MediaType.APPLICATION_JSON_VALUE)
	ResponseEntity<Object> createAssessment(@PathVariable("movieId") @NotBlank String movieId, @Validated(OnMovieCreate.class) @RequestBody Assessment assessment) {

		Result<Assessment> result = assessments.createForMovie(movieId, assessment);
		return ResponseHandler.generateResponse(result.isError(), result.getMessaje(), result.getInternalCode(), result.getResult(), getEntityModel(), result.getStatus());

	}

	@PatchMapping(path = "{movieId}/assessments/{assessmentId}", produces = MediaType.APPLICATION_JSON_VALUE)
	ResponseEntity<Object> updateAssessment(@PathVariable("movieId") @NotBlank String movieId, @PathVariable("assessmentId") @NotBlank String assessmentId, @RequestBody List<Map<String, Object>> updates) {

		Result<Assessment> result = assessments.update(assessmentId, updates);
		return ResponseHandler.generateResponse(result.isError(), result.getMessaje(), result.getInternalCode(), result.getResult(), getEntityModel(), result.getStatus());

	}

	@DeleteMapping(path = "{movieId}/assessments/{assessmentId}", produces = MediaType.APPLICATION_JSON_VALUE)
	ResponseEntity<Object> deleteAssessment(@PathVariable("movieId") @NotBlank String movieId, @PathVariable("assessmentId") @NotBlank String assessmentId) {

		Result<Assessment> result = assessments.delete(assessmentId);
		return ResponseHandler.generateResponse(result.isError(), result.getMessaje(), result.getInternalCode(), result.getResult(), getEntityModel(), result.getStatus());

	}

}