package usc.etse.grei.ense.p3.project.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import usc.etse.grei.ense.p3.project.model.Assessment;
import usc.etse.grei.ense.p3.project.model.Movie;
import usc.etse.grei.ense.p3.project.repository.AssessmentRepository;
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
	private final AssessmentRepository assessments;

	@Autowired
	public MovieService(MovieRepository movies, MongoTemplate mongo, PatchUtil patchUtil, AssessmentRepository assessments) {
		this.movies = movies;
		this.mongo = mongo;
		this.patchUtil = patchUtil;
		this.assessments = assessments;
	}

	public Optional<Page<Movie>> get(int page, int size, Sort sort, Example<Movie> filter) {

		Pageable request = PageRequest.of(page, size, sort);

		Criteria criteria = Criteria.byExample(filter);

		Query query = Query.query(criteria).with(request);
		query.fields().exclude("_id");

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

		try {

			Optional<Movie> movie = movies.findById(id);

			if (!movie.isPresent()) {
				return Optional.empty();
			}

			Movie originalMovie = movie.get();
			Movie filteredMovie = patchUtil.patch(originalMovie, operations);
			Movie updatedMovie = movies.save(filteredMovie);

			ExampleMatcher matcher = ExampleMatcher.matching();
			Example<Assessment> filter = Example.of(
					new Assessment().setMovie(new Movie().setId(updatedMovie.getId())),
					matcher
			);

			if (!originalMovie.getTitle().equals(updatedMovie.getTitle()) || !originalMovie.getId().equals(updatedMovie.getId())) {
				assessments.findAll(filter).forEach(assessment -> {
					assessment.getMovie().setTitle(updatedMovie.getTitle());
					assessment.getMovie().setId(updatedMovie.getId());
					assessments.save(assessment);
				});
			}

			return Optional.of(updatedMovie);

		} catch (Exception e) {

			return Optional.empty();

		}

	}

	public Optional<Movie> delete(String id) {

		Optional<Movie> result = movies.findById(id);

		if (result.isEmpty()) {
			return Optional.empty();
		}

		Movie movie = result.get();

		ExampleMatcher matcher = ExampleMatcher.matching();
		Example<Assessment> filter = Example.of(
				new Assessment().setMovie(new Movie().setId(movie.getId())),
				matcher
		);

		assessments.findAll(filter).forEach(assessment -> {
			assessments.delete(assessment);
		});

		movies.delete(movie);

		return Optional.of(movie);

	}

}