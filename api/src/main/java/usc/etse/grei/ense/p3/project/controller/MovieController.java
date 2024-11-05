package usc.etse.grei.ense.p3.project.controller;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.hateoas.server.LinkRelationProvider;
import usc.etse.grei.ense.p3.project.handler.ResponseHandler;
import usc.etse.grei.ense.p3.project.model.*;
import usc.etse.grei.ense.p3.project.service.AssessmentService;
import usc.etse.grei.ense.p3.project.service.MovieService;
import usc.etse.grei.ense.p3.project.util.SortUtil;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * Controlador de las operaciones sobre películas
 */
@RestController
@RequestMapping("movies")
public class MovieController {

	private final MovieService movies;
	private final AssessmentService assessments;
	private final LinkRelationProvider relationProvider;

	@Autowired
	public MovieController(MovieService movies, AssessmentService assessments, LinkRelationProvider relationProvider) {
		this.movies = movies;
		this.assessments = assessments;
		this.relationProvider = relationProvider;
	}

	/**
	 * Metodo que gestiona la operación GET /movies
	 *
	 * @param page        número de página
	 * @param size        número de películas por página
	 * @param sort        criterio de ordenación
	 * @param keywords    criterio de búsqueda por palabras clave
	 * @param genres      criterio de búsqueda por género
	 * @param releaseDate criterio de búsqueda por fecha de estreno
	 * @param cast        criterio de búsqueda por reparto
	 * @param crew        criterio de búsqueda por equipo de trabajo
	 * @return respuesta HTTP
	 */
	@GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("hasRole('USER')")
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

		Date parsedDate;

		if (!releaseDate.isBlank()) {

			try {

				LocalDate date = LocalDate.parse(releaseDate);

				parsedDate = new Date(date.getDayOfMonth(), date.getMonthValue(), date.getYear());

				filterMovie.setReleaseDate(parsedDate);

			} catch (Exception e) {

				return ResponseHandler.generateResponse(true, e.getMessage(), 0, null, new ArrayList<>(), HttpStatus.BAD_REQUEST);

			}

		}

		List<Cast> castList = new ArrayList<>();

		if (!cast.isEmpty()) {

			for (String castString : cast) {

				Cast newCast = new Cast();

				String[] parts = castString.split("-", 3);

				if (parts.length != 3) {
					return ResponseHandler.generateResponse(true, "Invalid cast", 0, null, new ArrayList<>(), HttpStatus.BAD_REQUEST);
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
					return ResponseHandler.generateResponse(true, "Invalid crew", 0, null, new ArrayList<>(), HttpStatus.BAD_REQUEST);
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

		Result<Page<Movie>> result = movies.get(page, size, Sort.by(criteria), filter, castList, crewList);
		ArrayList<Link> links = new ArrayList<>();

		if (result.getResult() != null) {

			Page<Movie> movies = result.getResult();
			Pageable metadata = movies.getPageable();

			Link self = linkTo(methodOn(MovieController.class).getMovies(page, size, sort, keywords, genres, releaseDate, cast, crew)).withSelfRel();
			Link first = linkTo(methodOn(MovieController.class).getMovies(metadata.first().getPageNumber(), size, sort, keywords, genres, releaseDate, cast, crew)).withRel(IanaLinkRelations.FIRST);
			Link next = linkTo(methodOn(MovieController.class).getMovies(metadata.next().getPageNumber(), size, sort, keywords, genres, releaseDate, cast, crew)).withRel(IanaLinkRelations.NEXT);
			Link previous = linkTo(methodOn(MovieController.class).getMovies(metadata.previousOrFirst().getPageNumber(), size, sort, keywords, genres, releaseDate, cast, crew)).withRel(IanaLinkRelations.PREVIOUS);
			Link last = linkTo(methodOn(MovieController.class).getMovies(movies.getTotalPages() - 1, size, sort, keywords, genres, releaseDate, cast, crew)).withRel(IanaLinkRelations.LAST);
			Link resource = linkTo(methodOn(MovieController.class).getMovie(null)).withRel(relationProvider.getItemResourceRelFor(Movie.class));

			links.add(self);
			links.add(first);
			links.add(next);
			links.add(previous);
			links.add(last);
			links.add(resource);

		}

		return ResponseHandler.generateResponse(result.isError(), result.getMessaje(), result.getInternalCode(), result.getResult().stream().toArray(), links, result.getStatus());

	}

	/**
	 * Metodo que gestiona la operación GET /movies/{id}
	 *
	 * @param id identificador de la película
	 * @return respuesta HTTP
	 */
	@GetMapping(path = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("hasRole('USER')")
	ResponseEntity<Object> getMovie(@PathVariable("id") String id) {

		Result<Movie> result = movies.get(id);
		ArrayList<Link> links = new ArrayList<>();

		if (result.getResult() != null) {

			Link self = linkTo(methodOn(MovieController.class).getMovie(id)).withSelfRel();
			Link all = linkTo(MovieController.class).withRel(relationProvider.getCollectionResourceRelFor(Movie.class));

			links.add(self);
			links.add(all);
		}
		return ResponseHandler.generateResponse(result.isError(), result.getMessaje(), result.getInternalCode(), result.getResult(), links, result.getStatus());

	}

	/**
	 * Metodo que gestiona la operación POST /movies
	 *
	 * @param movie película añadida
	 * @return respuesta HTTP
	 */
	@PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("hasRole('ADMIN')")
	ResponseEntity<Object> createMovie(@Validated(OnCreate.class) @RequestBody Movie movie) {

		Result<Movie> result = movies.create(movie);
		ArrayList<Link> links = new ArrayList<>();

		if (result.getResult() != null) {

			Link self = linkTo(methodOn(MovieController.class).getMovie(result.getResult().getId())).withSelfRel();
			Link all = linkTo(MovieController.class).withRel(relationProvider.getCollectionResourceRelFor(Movie.class));

			links.add(self);
			links.add(all);
		}

		return ResponseHandler.generateResponse(result.isError(), result.getMessaje(), result.getInternalCode(), result.getResult(), links, result.getStatus());

	}

	/**
	 * Metodo que gestiona la operación PATCH /movies/{id}
	 *
	 * @param id      identificador de la película
	 * @param updates lista de operaciones de modificación
	 * @return respuesta HTTP
	 */
	@PatchMapping(path = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("hasRole('ADMIN')")
	ResponseEntity<Object> updateMovie(@PathVariable("id") @NotBlank String id, @RequestBody List<Map<String, Object>> updates) {

		Result<Movie> result = movies.update(id, updates);
		ArrayList<Link> links = new ArrayList<>();

		if (result.getResult() != null) {

			Link self = linkTo(methodOn(MovieController.class).getMovie(result.getResult().getId())).withSelfRel();
			Link all = linkTo(MovieController.class).withRel(relationProvider.getCollectionResourceRelFor(Movie.class));

			links.add(self);
			links.add(all);

		}

		return ResponseHandler.generateResponse(result.isError(), result.getMessaje(), result.getInternalCode(), result.getResult(), links, result.getStatus());

	}

	/**
	 * Metodo que gestiona la operación DELETE /movies/{id}
	 *
	 * @param id identificador de la película
	 * @return respuesta HTTP
	 */
	@DeleteMapping(path = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("hasRole('ADMIN')")
	ResponseEntity<Object> deleteMovie(@PathVariable("id") @NotBlank String id) {

		Result<Movie> result = movies.delete(id);
		ArrayList<Link> links = new ArrayList<>();

		if (result.getResult() != null) {

			Link all = linkTo(MovieController.class).withRel(relationProvider.getCollectionResourceRelFor(Movie.class));

			links.add(all);

		}

		return ResponseHandler.generateResponse(result.isError(), result.getMessaje(), result.getInternalCode(), result.getResult(), links, result.getStatus());

	}

	/**
	 * Metodo que gestiona la operación POST /movies/{id}/cast
	 *
	 * @param id   identificador de la película
	 * @param cast actor añadido
	 * @return respuesta HTTP
	 */
	@PostMapping(path = "{id}/cast", produces = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("hasRole('ADMIN')")
	ResponseEntity<Object> createCast(@PathVariable("id") @NotBlank String id, @RequestBody @Validated(OnRelation.class) Cast cast) {

		Result<Cast> result = movies.createCast(id, cast);
		ArrayList<Link> links = new ArrayList<>();

		if (result.getResult() != null) {

			Link self = linkTo(methodOn(MovieController.class).getMovie(id)).withSelfRel();
			Link all = linkTo(MovieController.class).withRel(relationProvider.getCollectionResourceRelFor(Movie.class));

			links.add(self);
			links.add(all);
		}

		return ResponseHandler.generateResponse(result.isError(), result.getMessaje(), result.getInternalCode(), result.getResult(), links, result.getStatus());

	}

	/**
	 * Metodo que gestiona la operación PATCH /movies/{id}/cast/{relationId}
	 *
	 * @param id         identificador de la película
	 * @param relationId identificador de la relación
	 * @param updates    lista de operaciones de modificación
	 * @return respuesta HTTP
	 */
	@PatchMapping(path = "{id}/cast/{relationId}", produces = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("hasRole('ADMIN')")
	ResponseEntity<Object> updateCast(@PathVariable("id") @NotBlank String id, @PathVariable("relationId") @NotNull Integer relationId, @RequestBody List<Map<String, Object>> updates) {

		Result<Cast> result = movies.updateCast(id, relationId, updates);
		ArrayList<Link> links = new ArrayList<>();

		if (result.getResult() != null) {

			Link self = linkTo(methodOn(MovieController.class).getMovie(id)).withSelfRel();
			Link all = linkTo(MovieController.class).withRel(relationProvider.getCollectionResourceRelFor(Movie.class));

			links.add(self);
			links.add(all);
		}

		return ResponseHandler.generateResponse(result.isError(), result.getMessaje(), result.getInternalCode(), result.getResult(), links, result.getStatus());

	}

	/**
	 * Metodo que gestiona la operación DELETE /movies/{id}/cast/{relationId}
	 *
	 * @param id         identificador de la película
	 * @param relationId identificador de la relación
	 * @return respuesta HTTP
	 */
	@DeleteMapping(path = "{id}/cast/{relationId}", produces = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("hasRole('ADMIN')")
	ResponseEntity<Object> deleteCast(@PathVariable("id") @NotBlank String id, @PathVariable("relationId") @NotNull Integer relationId) {

		Result<Cast> result = movies.deleteCast(id, relationId);
		ArrayList<Link> links = new ArrayList<>();

		if (result.getResult() != null) {

			Link all = linkTo(MovieController.class).withRel(relationProvider.getCollectionResourceRelFor(Movie.class));

			links.add(all);

		}

		return ResponseHandler.generateResponse(result.isError(), result.getMessaje(), result.getInternalCode(), result.getResult(), links, result.getStatus());

	}

	/**
	 * Metodo que gestiona la operación POST /movies/{id}/crew
	 *
	 * @param id   identificador de la película
	 * @param crew trabajador añadido
	 * @return respuesta HTTP
	 */
	@PostMapping(path = "{id}/crew", produces = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("hasRole('ADMIN')")
	ResponseEntity<Object> createCrew(@PathVariable("id") @NotBlank String id, @RequestBody @Validated(OnRelation.class) Crew crew) {

		Result<Crew> result = movies.createCrew(id, crew);
		ArrayList<Link> links = new ArrayList<>();

		if (result.getResult() != null) {

			Link self = linkTo(methodOn(MovieController.class).getMovie(id)).withSelfRel();
			Link all = linkTo(MovieController.class).withRel(relationProvider.getCollectionResourceRelFor(Movie.class));

			links.add(self);
			links.add(all);
		}

		return ResponseHandler.generateResponse(result.isError(), result.getMessaje(), result.getInternalCode(), result.getResult(), links, result.getStatus());

	}

	/**
	 * Metodo que gestiona la operación PATCH /movies/{id}/crew/{relationId}
	 *
	 * @param id         identificador de la película
	 * @param relationId identificador de la relación
	 * @param updates    lista de operaciones de modificación
	 * @return respuesta HTTP
	 */
	@PatchMapping(path = "{id}/crew/{relationId}", produces = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("hasRole('ADMIN')")
	ResponseEntity<Object> updateCrew(@PathVariable("id") @NotBlank String id, @PathVariable("relationId") @NotNull Integer relationId, @RequestBody List<Map<String, Object>> updates) {

		Result<Crew> result = movies.updateCrew(id, relationId, updates);
		ArrayList<Link> links = new ArrayList<>();

		if (result.getResult() != null) {

			Link self = linkTo(methodOn(MovieController.class).getMovie(id)).withSelfRel();
			Link all = linkTo(MovieController.class).withRel(relationProvider.getCollectionResourceRelFor(Movie.class));

			links.add(self);
			links.add(all);
		}

		return ResponseHandler.generateResponse(result.isError(), result.getMessaje(), result.getInternalCode(), result.getResult(), links, result.getStatus());

	}

	/**
	 * Metodo que gestiona la operación DELETE /movies/{id}/crew/{relationId}
	 *
	 * @param id         identificador de la película
	 * @param relationId identificador de la relacion
	 * @return respuesta HTTP
	 */
	@DeleteMapping(path = "{id}/crew/{relationId}", produces = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("hasRole('ADMIN')")
	ResponseEntity<Object> deleteCrew(@PathVariable("id") @NotBlank String id, @PathVariable("relationId") @NotNull Integer relationId) {

		Result<Crew> result = movies.deleteCrew(id, relationId);
		ArrayList<Link> links = new ArrayList<>();

		if (result.getResult() != null) {

			Link all = linkTo(MovieController.class).withRel(relationProvider.getCollectionResourceRelFor(Movie.class));

			links.add(all);

		}

		return ResponseHandler.generateResponse(result.isError(), result.getMessaje(), result.getInternalCode(), result.getResult(), links, result.getStatus());

	}

	/**
	 * Metodo que gestiona la operación GET /movies/{movieId}/assessments
	 *
	 * @param movieId identificador de la película
	 * @param page    número de página
	 * @param size    número de comentarios por página
	 * @param sort    criterio de ordenación
	 * @return respuesta HTTP
	 */
	@GetMapping(path = "{movieId}/assessments", produces = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("hasRole('USER')")
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

		Result<Page<Assessment>> result = assessments.get(page, size, Sort.by(criteria), filter);
		ArrayList<Link> links = new ArrayList<>();

		if (result.getResult() != null) {

			Page<Assessment> assesments = result.getResult();
			Pageable metadata = assesments.getPageable();

			Link movie = linkTo(methodOn(MovieController.class).getMovie(movieId)).withRel("movie");
			Link first = linkTo(methodOn(MovieController.class).getAssessments(movieId, metadata.first().getPageNumber(), size, sort)).withRel(IanaLinkRelations.FIRST);
			Link last = linkTo(methodOn(MovieController.class).getAssessments(movieId, assesments.getTotalPages() - 1, size, sort)).withRel(IanaLinkRelations.LAST);
			Link next = linkTo(methodOn(MovieController.class).getAssessments(movieId, metadata.next().getPageNumber(), size, sort)).withRel(IanaLinkRelations.NEXT);
			Link previous = linkTo(methodOn(MovieController.class).getAssessments(movieId, metadata.previousOrFirst().getPageNumber(), size, sort)).withRel(IanaLinkRelations.PREVIOUS);

			links.add(movie);
			links.add(first);
			links.add(last);
			links.add(next);
			links.add(previous);

		}

		return ResponseHandler.generateResponse(result.isError(), result.getMessaje(), result.getInternalCode(), result.getResult().stream().toArray(), links, result.getStatus());

	}

	/**
	 * Metodo que gestiona la operación POST /movies/{movieId}/assessments
	 *
	 * @param movieId    identificador de la película
	 * @param assessment comentario añadido
	 * @return respuesta HTTP
	 */
	@PostMapping(path = "{movieId}/assessments", produces = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("#assessment.user != null and #assessment.user.email != null and #assessment.user.email == principal")
	ResponseEntity<Object> createAssessment(@PathVariable("movieId") @NotBlank String movieId, @Validated(OnMovieCreate.class) @RequestBody Assessment assessment) {

		Result<Assessment> result = assessments.createForMovie(movieId, assessment);
		ArrayList<Link> links = new ArrayList<>();

		if (result.getResult() != null) {

			Link movie = linkTo(methodOn(MovieController.class).getMovie(result.getResult().getMovie().getId())).withRel("movie");
			Link movieAssessments = linkTo(methodOn(MovieController.class).getAssessments(result.getResult().getMovie().getId(), 0, 20, new ArrayList<>())).withRel("movieAssessments");

			links.add(movie);
			links.add(movieAssessments);

		}

		return ResponseHandler.generateResponse(result.isError(), result.getMessaje(), result.getInternalCode(), result.getResult(), links, result.getStatus());

	}

	/**
	 * Metodo que gestiona la operación PATCH /movies/{movieId}/assessments/{assessmentId}
	 *
	 * @param movieId      identificador de la película
	 * @param assessmentId identificador del comentario
	 * @param updates      lista de operaciones de modificación
	 * @return respuesta HTTP
	 */
	@PatchMapping(path = "{movieId}/assessments/{assessmentId}", produces = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("@assessmentService.isAssessmentOwner(#assessmentId, principal)")
	ResponseEntity<Object> updateAssessment(@PathVariable("movieId") @NotBlank String movieId, @PathVariable("assessmentId") @NotBlank String assessmentId, @RequestBody List<Map<String, Object>> updates) {

		Result<Assessment> result = assessments.updateForMovie(movieId, assessmentId, updates);
		ArrayList<Link> links = new ArrayList<>();

		if (result.getResult() != null) {

			Link self = linkTo(methodOn(MovieController.class).updateAssessment(movieId, assessmentId, updates)).withSelfRel();
			Link movieAssessments = linkTo(methodOn(MovieController.class).getAssessments(result.getResult().getMovie().getId(), 0, 20, new ArrayList<>())).withRel("movieAssessments");
			Link userAssessments = linkTo(methodOn(UserController.class).getAssessments(result.getResult().getUser().getEmail(), 0, 20, new ArrayList<>())).withRel("userAssessments");

			links.add(self);
			links.add(movieAssessments);
			links.add(userAssessments);

		}

		return ResponseHandler.generateResponse(result.isError(), result.getMessaje(), result.getInternalCode(), result.getResult(), links, result.getStatus());

	}

	/**
	 * Metodo que gestiona la operación DELETE /movies/{movieId}/assessments/{assessmentId}
	 *
	 * @param movieId      identificador de la película
	 * @param assessmentId identificador del comentario
	 * @return respuesta HTTP
	 */
	@DeleteMapping(path = "{movieId}/assessments/{assessmentId}", produces = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("hasRole('ADMIN') or @assessmentService.isAssessmentOwner(#assessmentId, principal)")
	ResponseEntity<Object> deleteAssessment(@PathVariable("movieId") @NotBlank String movieId, @PathVariable("assessmentId") @NotBlank String assessmentId) {

		Result<Assessment> result = assessments.deleteForMovie(movieId, assessmentId);
		ArrayList<Link> links = new ArrayList<>();

		if (result.getResult() != null) {

			Link movieAssessments = linkTo(methodOn(MovieController.class).getAssessments(result.getResult().getMovie().getId(), 0, 20, new ArrayList<>())).withRel("movieAssessments");
			Link userAssessments = linkTo(methodOn(UserController.class).getAssessments(result.getResult().getUser().getEmail(), 0, 20, new ArrayList<>())).withRel("userAssessments");

			links.add(movieAssessments);
			links.add(userAssessments);

		}

		return ResponseHandler.generateResponse(result.isError(), result.getMessaje(), result.getInternalCode(), result.getResult(), links, result.getStatus());

	}

}