package usc.etse.grei.ense.p3.project.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import usc.etse.grei.ense.p3.project.model.Movie;
import usc.etse.grei.ense.p3.project.repository.MovieRepository;
import usc.etse.grei.ense.p3.project.util.PatchUtil;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class MovieService {

	private final MovieRepository movies;
	private final MongoTemplate mongo;
	private final PatchUtil patchUtil;

	@Autowired
	public MovieService(MovieRepository movies, MongoTemplate mongo, PatchUtil patchUtil) {
		this.movies = movies;
		this.mongo = mongo;
		this.patchUtil = patchUtil;
	}

	public Optional<Page<Movie>> get(int page, int size, Sort sort, Example<Movie> filter) {

		Pageable request = PageRequest.of(page, size, sort);

		Criteria criteria = Criteria.byExample(filter);

		Query query = Query.query(criteria).with(request);
		query.fields().include("title", "overview", "genres", "releaseDate", "resources");

		List<Movie> result = mongo.find(query, Movie.class);

		return Optional.of(new PageImpl<>(result, request, result.size()));

	}

	public Optional<Movie> get(String id) {
		return movies.findById(id);
	}

	public Optional<Movie> create(Movie movie) {

		try {

			movies.insert(movie);
			return Optional.of(movie);

		} catch (Exception e) {

			return Optional.empty();

		}

	}

	public Optional<Movie> update(String id, List<Map<String, Object>> operations) {

		Optional<Movie> movie = movies.findById(id);

		if (!movie.isPresent()) {
			return Optional.empty();
		}

		try {

			Movie filteredMovie = patchUtil.patch(movie.get(), operations);
			Movie updatedMovie = movies.save(filteredMovie);
			return Optional.of(updatedMovie);

		} catch (Exception e) {

			return Optional.empty();

		}

	}

	public Optional<Movie> delete(String id) {

		Movie movie = movies.findById(id).orElse(null);

		if (movie == null) {
			return Optional.empty();
		}

		movies.delete(movie);
		return Optional.of(movie);

	}

}