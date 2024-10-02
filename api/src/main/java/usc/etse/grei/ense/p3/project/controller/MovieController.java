package usc.etse.grei.ense.p3.project.controller;

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
import usc.etse.grei.ense.p3.project.model.Movie;
import usc.etse.grei.ense.p3.project.model.OnCreate;
import usc.etse.grei.ense.p3.project.service.MovieService;
import usc.etse.grei.ense.p3.project.util.SortUtil;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("movies")
public class MovieController {

	private final MovieService movies;

	@Autowired
	public MovieController(MovieService movies) {
		this.movies = movies;
	}

	private EntityModel<Movie> getEntityModel() {

		Movie movie = new Movie();
		EntityModel<Movie> entityModel = EntityModel.of(movie);

		return entityModel;

	}

	@GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	ResponseEntity<Object> get(
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
				new Movie().setId(search),
				matcher
		);

		Optional<Page<Movie>> dbMovies = movies.get(page, size, Sort.by(criteria), filter);

		if (dbMovies.isPresent()) {
			return ResponseHandler.generateResponse(false, "ok", 0, dbMovies.get().getContent(), getEntityModel(), HttpStatus.ACCEPTED);
		} else {
			return ResponseHandler.generateResponse(true, "ok", 0, dbMovies.get().getContent(), getEntityModel(), HttpStatus.NOT_FOUND);
		}

	}

	@GetMapping(path = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	ResponseEntity<Movie> get(@PathVariable("id") String id) {
		return ResponseEntity.of(movies.get(id));
	}

	@PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	ResponseEntity<Object> createMovie(@Validated(OnCreate.class) @RequestBody Movie movie) {

		Optional<Movie> result = movies.create(movie);

		if (result.isPresent()) {

			return ResponseHandler.generateResponse(false, "ok", 0, result, getEntityModel(), HttpStatus.CREATED);

		} else {

			return ResponseHandler.generateResponse(true, "ok", 0, result, getEntityModel(), HttpStatus.NOT_FOUND);

		}

	}

	@PatchMapping(path = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	ResponseEntity<Object> updateMovie(@PathVariable("id") @NotBlank String id, @RequestBody List<Map<String, Object>> updates) {

		Optional<Movie> result = movies.update(id, updates);

		if (result.isPresent()) {

			return ResponseEntity.ok(getEntityModel());

		} else {

			return ResponseEntity.notFound().build();

		}

	}

	@DeleteMapping(path = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	ResponseEntity<Object> deleteMovie(@PathVariable("id") @NotBlank String id) {

		Optional<Movie> result = movies.delete(id);

		if (result.isPresent()) {

			return ResponseEntity.noContent().build();

		} else {

			return ResponseEntity.notFound().build();

		}

	}

}