package usc.etse.grei.ense.p3.project.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import usc.etse.grei.ense.p3.project.model.Movie;
import usc.etse.grei.ense.p3.project.service.MovieService;
import usc.etse.grei.ense.p3.project.util.SortUtil;

import java.util.List;

@RestController
@RequestMapping("movies")
public class MovieController {

	private final MovieService movies;

	@Autowired
	public MovieController(MovieService movies) {
		this.movies = movies;
	}

	@GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	ResponseEntity<Page<Movie>> get(
			@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "20") int size,
			@RequestParam(name = "sort", defaultValue = "") List<String> sort
	) {

		List<Sort.Order> criteria = SortUtil.getCriteria(sort);

		return ResponseEntity.of(movies.get(page, size, Sort.by(criteria)));
	}

	@GetMapping(path = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	ResponseEntity<Movie> get(@PathVariable("id") String id) {
		return ResponseEntity.of(movies.get(id));
	}

}