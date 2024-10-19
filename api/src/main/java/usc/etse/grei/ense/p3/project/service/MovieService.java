package usc.etse.grei.ense.p3.project.service;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import usc.etse.grei.ense.p3.project.model.*;
import usc.etse.grei.ense.p3.project.model.Date;
import usc.etse.grei.ense.p3.project.repository.AssessmentRepository;
import usc.etse.grei.ense.p3.project.repository.MovieRepository;
import usc.etse.grei.ense.p3.project.repository.PersonRepository;
import usc.etse.grei.ense.p3.project.util.PatchUtil;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MovieService {

	private final MovieRepository movies;
	private final MongoTemplate mongo;
	private final PatchUtil patchUtil;
	private final AssessmentRepository assessments;
	private final PersonRepository persons;
	private final Validator validator;

	@Autowired
	public MovieService(MovieRepository movies, MongoTemplate mongo, PatchUtil patchUtil, AssessmentRepository assessments, PersonRepository persons, Validator validator) {
		this.movies = movies;
		this.mongo = mongo;
		this.patchUtil = patchUtil;
		this.assessments = assessments;
		this.persons = persons;
		this.validator = validator;
	}

	public Result<List<Movie>> get(int page, int size, Sort sort, Example<Movie> filter) {

		Pageable request = PageRequest.of(page, size, sort);

		Criteria criteria = Criteria.byExample(filter);

		Query query = Query.query(criteria).with(request);

		List<Movie> result = mongo.find(query, Movie.class);

		return new Result<>(result, false, "Movies found", 0, Result.Code.OK);

	}

	public Result<Movie> get(String id) {

		Movie result = movies.findById(id).orElse(null);

		if (result != null) {

			return new Result<>(result, false, "Movie found", 0, Result.Code.OK);

		} else {

			return new Result<>(null, false, "No movie", 0, Result.Code.NOT_FOUND);

		}

	}

	public Result<Movie> create(Movie movie) {

		try {

			Example<Movie> testMovie = Example.of(new Movie().setTitle(movie.getTitle()));
			Optional<Movie> result = movies.findOne(testMovie);

			if (result.isPresent()) {
				return new Result<>(null, false, "Movie already exists", 0, Result.Code.CONFLICT);
			}

			Date date = movie.getReleaseDate();

			if (date != null) {

				LocalDate releaseDate = LocalDate.of(date.getYear(), date.getMonth(), date.getDay());

				if (releaseDate.isAfter(LocalDate.now())) {
					return new Result<>(null, true, "Invalid releaseDate", 0, Result.Code.BAD_REQUEST);
				}

			}

			movies.insert(movie);

			return new Result<>(movie, false, "Movie created", 0, Result.Code.CREATED);

		} catch (Exception e) {

			return new Result<>(null, true, e.getLocalizedMessage(), 0, Result.Code.BAD_REQUEST);

		}

	}

	public Result<Movie> update(String id, List<Map<String, Object>> operations) {

		try {

			Movie originalMovie = movies.findById(id).orElse(null);

			if (originalMovie == null) {
				return new Result<>(null, true, "Movie not found", 0, Result.Code.NOT_FOUND);
			}

			operations.removeIf(op -> op.containsKey("path") && (op.get("path").equals("/id") || ((String) op.get("path")).startsWith("/crew") || ((String) op.get("path")).startsWith("/cast")));

			Movie filteredMovie = patchUtil.patch(originalMovie, operations);

			List<Crew> crewCopy = filteredMovie.getCrew();
			List<Cast> castCopy = filteredMovie.getCast();

			filteredMovie.setCrew(null);
			filteredMovie.setCast(null);

			Set<ConstraintViolation<Movie>> violations = validator.validate(filteredMovie, OnUpdate.class);

			if (!violations.isEmpty()) {
				return new Result<>(null, true, "Invalid cast", 0, Result.Code.BAD_REQUEST);
			}

			filteredMovie.setCrew(crewCopy);
			filteredMovie.setCast(castCopy);

			Date date = filteredMovie.getReleaseDate();

			if (date != null) {

				LocalDate releaseDate = LocalDate.of(date.getYear(), date.getMonth(), date.getDay());

				if (releaseDate.isAfter(LocalDate.now())) {
					return new Result<>(null, true, "Invalid releaseDate", 0, Result.Code.BAD_REQUEST);
				}

			}

			Movie updatedMovie = movies.save(filteredMovie);

			ExampleMatcher matcher = ExampleMatcher.matching();
			Example<Assessment> filter = Example.of(
					new Assessment().setMovie(new Movie().setId(updatedMovie.getId())),
					matcher
			);

			if (!originalMovie.getTitle().equals(updatedMovie.getTitle())) {
				assessments.findAll(filter).forEach(assessment -> {

					Movie movie = assessment.getMovie();

					movie.setTitle(updatedMovie.getTitle());

					assessment.setMovie(movie);

					assessments.save(assessment);

				});
			}

			return new Result<>(updatedMovie, false, "Movie updated", 0, Result.Code.OK);

		} catch (Exception e) {

			return new Result<>(null, true, e.getLocalizedMessage(), 0, Result.Code.BAD_REQUEST);

		}

	}

	public Result<Movie> delete(String id) {

		Movie movie = movies.findById(id).orElse(null);

		if (movie == null) {
			return new Result<>(null, true, "Movie not found", 0, Result.Code.NOT_FOUND);
		}

		ExampleMatcher matcher = ExampleMatcher.matching();
		Example<Assessment> filter = Example.of(
				new Assessment().setMovie(new Movie().setId(movie.getId())),
				matcher
		);

		assessments.deleteAll(assessments.findAll(filter));

		movies.delete(movie);

		return new Result<>(movie, false, "Movie deleted", 0, Result.Code.OK);

	}

	public Result<Cast> addCast(String id, Cast cast) {

		Movie movie = movies.findById(id).orElse(null);

		if (movie == null) {
			return new Result<>(null, true, "Movie not found", 0, Result.Code.NOT_FOUND);
		}

		Example<Person> testPerson = Example.of(new Person().setId(cast.getId()).setName(cast.getName()));
		Person person = persons.findOne(testPerson).orElse(null);

		if (person == null) {
			return new Result<>(null, true, "Person not found", 0, Result.Code.NOT_FOUND);
		}

		if (movie.getCast() == null) {
			movie.setCast(List.of(cast));
		} else {
			movie.getCast().add(cast);
		}

		movies.save(movie);

		return new Result<>(cast, false, "Cast added", 0, Result.Code.CREATED);

	}

	public Result<Cast> updateCast(String id, String castId, List<Map<String, Object>> operations) {

		try {

			Movie movie = movies.findById(id).orElse(null);

			if (movie == null) {
				return new Result<>(null, true, "Movie not found", 0, Result.Code.NOT_FOUND);
			}

			if (movie.getCast() == null) {
				return new Result<>(null, true, "Cast not found", 0, Result.Code.NOT_FOUND);
			}

			Cast cast = movie.getCast().stream().filter(c -> c.getId().equals(castId)).findFirst().orElse(null);

			if (cast == null) {
				return new Result<>(null, true, "Cast not found", 0, Result.Code.NOT_FOUND);
			}

			operations.removeIf(op -> op.containsKey("path") && (op.get("path").equals("/id") || op.get("path").equals("/name")));

			Cast filteredCast = patchUtil.patch(cast, operations);

			Set<ConstraintViolation<Cast>> violations = validator.validate(filteredCast, OnRelation.class);

			if (!violations.isEmpty()) {
				return new Result<>(null, true, "Invalid cast", 0, Result.Code.BAD_REQUEST);
			}

			movie.getCast().remove(cast);
			movie.getCast().add(filteredCast);

			movies.save(movie);

			return new Result<>(filteredCast, false, "Cast updated", 0, Result.Code.OK);

		} catch (Exception e) {

			return new Result<>(null, true, e.getLocalizedMessage(), 0, Result.Code.BAD_REQUEST);

		}

	}

	public Result<Cast> deleteCast(String id, String castId) {

		Movie movie = movies.findById(id).orElse(null);

		if (movie == null) {
			return new Result<>(null, true, "Movie not found", 0, Result.Code.NOT_FOUND);
		}

		if (movie.getCast() == null) {
			return new Result<>(null, true, "Cast not found", 0, Result.Code.NOT_FOUND);
		}

		Cast cast = movie.getCast().stream().filter(c -> c.getId().equals(castId)).findFirst().orElse(null);

		if (cast == null) {
			return new Result<>(null, true, "Cast not found", 0, Result.Code.NOT_FOUND);
		}

		movie.getCast().remove(cast);

		movies.save(movie);

		return new Result<>(cast, false, "Cast removed", 0, Result.Code.OK);

	}

	public Result<Crew> addCrew(String id, Crew crew) {

		Movie movie = movies.findById(id).orElse(null);

		if (movie == null) {
			return new Result<>(null, true, "Movie not found", 0, Result.Code.NOT_FOUND);
		}

		Example<Person> testPerson = Example.of(new Person().setId(crew.getId()).setName(crew.getName()));
		Person person = persons.findOne(testPerson).orElse(null);

		if (person == null) {
			return new Result<>(null, true, "Person not found", 0, Result.Code.NOT_FOUND);
		}

		crew.setId(person.getId());

		Set<ConstraintViolation<Crew>> violations = validator.validate(crew, OnRelation.class);

		if (!violations.isEmpty()) {
			return new Result<>(null, true, "Invalid crew", 0, Result.Code.BAD_REQUEST);
		}

		if (movie.getCrew() == null) {
			movie.setCrew(List.of(crew));
		} else {
			movie.getCrew().add(crew);
		}

		movies.save(movie);

		return new Result<>(crew, false, "Crew added", 0, Result.Code.CREATED);

	}

	public Result<Crew> updateCrew(String id, String crewId, List<Map<String, Object>> operations) {

		try {

			Movie movie = movies.findById(id).orElse(null);

			if (movie == null) {
				return new Result<>(null, true, "Movie not found", 0, Result.Code.NOT_FOUND);
			}

			if (movie.getCrew() == null) {
				return new Result<>(null, true, "Crew not found", 0, Result.Code.NOT_FOUND);
			}

			Crew crew = movie.getCrew().stream().filter(c -> c.getId().equals(crewId)).findFirst().orElse(null);

			if (crew == null) {
				return new Result<>(null, true, "Crew not found", 0, Result.Code.NOT_FOUND);
			}

			operations.removeIf(op -> op.containsKey("path") && (op.get("path").equals("/id") || op.get("path").equals("/name")));

			Crew filteredCrew = patchUtil.patch(crew, operations);

			Set<ConstraintViolation<Crew>> violations = validator.validate(filteredCrew, OnRelation.class);

			if (!violations.isEmpty()) {
				System.out.println(violations);
				return new Result<>(null, true, "Invalid crew", 0, Result.Code.BAD_REQUEST);
			}

			movie.getCrew().remove(crew);
			movie.getCrew().add(filteredCrew);

			movies.save(movie);

			return new Result<>(filteredCrew, false, "Crew updated", 0, Result.Code.OK);

		} catch (Exception e) {

			return new Result<>(null, true, e.getLocalizedMessage(), 0, Result.Code.BAD_REQUEST);

		}

	}

	public Result<Crew> deleteCrew(String id, String crewId) {

		Movie movie = movies.findById(id).orElse(null);

		if (movie == null) {
			return new Result<>(null, true, "Movie not found", 0, Result.Code.NOT_FOUND);
		}

		if (movie.getCrew() == null) {
			return new Result<>(null, true, "Crew not found", 0, Result.Code.NOT_FOUND);
		}

		Crew crew = movie.getCrew().stream().filter(c -> c.getId().equals(crewId)).findFirst().orElse(null);

		if (crew == null) {
			return new Result<>(null, true, "Crew not found", 0, Result.Code.NOT_FOUND);
		}

		movie.getCrew().remove(crew);

		movies.save(movie);

		return new Result<>(crew, false, "Crew removed", 0, Result.Code.OK);

	}

}