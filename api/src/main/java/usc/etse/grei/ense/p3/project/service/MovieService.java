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
import usc.etse.grei.ense.p3.project.repository.AssessmentRepository;
import usc.etse.grei.ense.p3.project.repository.MovieRepository;
import usc.etse.grei.ense.p3.project.repository.PersonRepository;
import usc.etse.grei.ense.p3.project.util.PatchUtil;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Servicio que implementa la lógica de negocio para películas
 */
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

	/**
	 * Metodo que obtiene una lista de películas utilizando filtrado y ordenación
	 *
	 * @param page     número de página
	 * @param size     número de películas por página
	 * @param sort     criterio de ordenación
	 * @param filter   criterio de filtrado por película
	 * @param castList criterio de filtrado por reparto
	 * @param crewList criterio de filtrado por equipo de trabajo
	 * @return resultado de la búsqueda
	 */
	public Result<List<Movie>> get(int page, int size, Sort sort, Example<Movie> filter, List<Cast> castList, List<Crew> crewList) {

		Pageable request = PageRequest.of(page, size, sort);

		Criteria criteria = Criteria.byExample(filter);

		if (!castList.isEmpty()) {

			List<String> castIds = castList.stream().map(Cast::getId).filter(Objects::nonNull).collect(Collectors.toList());
			List<String> castNames = castList.stream().map(Cast::getName).filter(Objects::nonNull).collect(Collectors.toList());
			List<String> castCharacters = castList.stream().map(Cast::getCharacter).filter(Objects::nonNull).collect(Collectors.toList());

			if (!castIds.isEmpty()) {
				criteria.and("cast.id").in(castIds);
			}

			if (!castNames.isEmpty()) {
				criteria.and("cast.name").in(castNames);
			}

			if (!castCharacters.isEmpty()) {
				criteria.and("cast.character").in(castCharacters);
			}

		}

		if (!crewList.isEmpty()) {

			List<String> crewIds = crewList.stream().map(Crew::getId).filter(Objects::nonNull).collect(Collectors.toList());
			List<String> crewNames = crewList.stream().map(Crew::getName).filter(Objects::nonNull).collect(Collectors.toList());
			List<String> crewJobs = crewList.stream().map(Crew::getJob).filter(Objects::nonNull).collect(Collectors.toList());

			if (!crewIds.isEmpty()) {
				criteria.and("crew.id").in(crewIds);
			}

			if (!crewNames.isEmpty()) {
				criteria.and("crew.name").in(crewNames);
			}

			if (!crewJobs.isEmpty()) {
				criteria.and("crew.job").in(crewJobs);
			}

		}

		Query query = Query.query(criteria).with(request);
		query.fields().include("_id", "title", "overview", "genres", "releaseDate", "resources");

		List<Movie> result = mongo.find(query, Movie.class);

		return new Result<>(result, false, "Movies found", 0, Result.Code.OK);

	}

	/**
	 * Metodo que obtiene una película a partir de su id
	 *
	 * @param id identificador de la película
	 * @return resultado de la búsqueda
	 */
	public Result<Movie> get(String id) {

		Movie result = movies.findById(id).orElse(null);

		if (result == null) {
			return new Result<>(null, false, "No movie", 0, Result.Code.NOT_FOUND);
		}

		return new Result<>(result, false, "Movie found", 0, Result.Code.OK);

	}

	/**
	 * Metodo que inserta una película en la base de datos
	 *
	 * @param movie película que se inserta
	 * @return resultado de la inserción
	 */
	public Result<Movie> create(Movie movie) {

		try {

			Example<Movie> testMovie = Example.of(new Movie().setTitle(movie.getTitle()));
			Movie result = movies.findOne(testMovie).orElse(null);

			if (result != null) {
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

	/**
	 * Metodo que modifica la información de una película almacenada
	 *
	 * @param id         identificador de la pelicula modificada
	 * @param operations lista de operaciones de modificación
	 * @return resultado de la modificación
	 */
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
				return new Result<>(null, true, "Invalid due to violations", 0, Result.Code.BAD_REQUEST);
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

	/**
	 * Metodo que elimina una película de la base de datos
	 *
	 * @param id identificador de la película eliminada
	 * @return resultado de la eliminación
	 */
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

	/**
	 * Metodo que añade un actor al reparto de una película
	 *
	 * @param id   identificador de la película
	 * @param cast actor añadido al reparto de la película
	 * @return resultado de la inserción
	 */
	public Result<Cast> createCast(String id, Cast cast) {

		Movie movie = movies.findById(id).orElse(null);

		if (movie == null) {
			return new Result<>(null, true, "Movie not found", 0, Result.Code.NOT_FOUND);
		}

		Example<Person> testPerson = Example.of(new Person().setId(cast.getId()).setName(cast.getName()));
		Person person = persons.findOne(testPerson).orElse(null);

		if (person == null) {
			return new Result<>(null, true, "Person not found", 0, Result.Code.NOT_FOUND);
		}

		cast.setRelationId(cast.hashCode());

		if (movie.getCast() == null) {
			movie.setCast(List.of(cast));
		} else if (movie.getCast().stream().anyMatch(c -> c.getRelationId() == cast.getRelationId())) {
			return new Result<>(null, true, "Person is already in cast", 0, Result.Code.CONFLICT);
		} else {
			movie.getCast().add(cast);
		}

		movies.save(movie);

		return new Result<>(cast, false, "Cast added", 0, Result.Code.CREATED);

	}

	/**
	 * Metodo que modifica la información de un actor del reparto de una película
	 *
	 * @param id         identificador de la película
	 * @param relationId identificador
	 * @param operations lista de operaciones de modificación
	 * @return resultado de la modificación
	 */
	public Result<Cast> updateCast(String id, Integer relationId, List<Map<String, Object>> operations) {

		try {

			Movie movie = movies.findById(id).orElse(null);

			if (movie == null) {
				return new Result<>(null, true, "Movie not found", 0, Result.Code.NOT_FOUND);
			}

			if (movie.getCast() == null) {
				return new Result<>(null, true, "Cast not found", 0, Result.Code.NOT_FOUND);
			}

			Cast cast = movie.getCast().stream().filter(c -> c.getRelationId() == (relationId)).findFirst().orElse(null);

			if (cast == null) {
				return new Result<>(null, true, "Cast not found", 0, Result.Code.NOT_FOUND);
			}

			operations.removeIf(op -> op.containsKey("path") && (op.get("path").equals("/id") || op.get("path").equals("/relationId") || op.get("path").equals("/name")));

			Cast filteredCast = patchUtil.patch(cast, operations);

			Set<ConstraintViolation<Cast>> violations = validator.validate(filteredCast, OnRelation.class);

			if (!violations.isEmpty()) {
				return new Result<>(null, true, "Invalid cast", 0, Result.Code.BAD_REQUEST);
			}

			filteredCast.setRelationId(filteredCast.hashCode());

			if (movie.getCast().stream().anyMatch(c -> c.getRelationId() == filteredCast.getRelationId())) {
				return new Result<>(null, true, "Combination person-character is already in cast", 0, Result.Code.CONFLICT);
			}

			movie.getCast().remove(cast);
			movie.getCast().add(filteredCast);

			movies.save(movie);

			return new Result<>(filteredCast, false, "Cast updated", 0, Result.Code.OK);

		} catch (Exception e) {

			return new Result<>(null, true, e.getLocalizedMessage(), 0, Result.Code.BAD_REQUEST);

		}

	}

	/**
	 * Metodo que elimina un actor del reparto de una película
	 *
	 * @param id         identificador de la película
	 * @param relationId identificador del actor
	 * @return resultado de la eliminación
	 */
	public Result<Cast> deleteCast(String id, Integer relationId) {

		Movie movie = movies.findById(id).orElse(null);

		if (movie == null) {
			return new Result<>(null, true, "Movie not found", 0, Result.Code.NOT_FOUND);
		}

		if (movie.getCast() == null) {
			return new Result<>(null, true, "Cast not found", 0, Result.Code.NOT_FOUND);
		}

		Cast cast = movie.getCast().stream().filter(c -> c.getRelationId() == relationId).findFirst().orElse(null);

		if (cast == null) {
			return new Result<>(null, true, "Cast not found", 0, Result.Code.NOT_FOUND);
		}

		movie.getCast().remove(cast);

		movies.save(movie);

		return new Result<>(cast, false, "Cast removed", 0, Result.Code.OK);

	}

	/**
	 * Metodo que añade un trabajador al equipo de una película
	 *
	 * @param id   identificador de la película
	 * @param crew trabajador que se añade
	 * @return resultado de la inserción
	 */
	public Result<Crew> createCrew(String id, Crew crew) {

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

		crew.setRelationId(crew.hashCode());

		if (movie.getCrew() == null) {
			movie.setCrew(List.of(crew));
		} else if (movie.getCrew().stream().anyMatch(c -> c.getRelationId().equals(crew.getRelationId()))) {
			return new Result<>(null, true, "Person is already in crew", 0, Result.Code.CONFLICT);
		} else {
			movie.getCrew().add(crew);
		}

		movies.save(movie);

		return new Result<>(crew, false, "Crew added", 0, Result.Code.CREATED);

	}

	/**
	 * Metodo que modifica la información de un trabajador del equipo de una película
	 *
	 * @param id         identificador de la película
	 * @param relationId identificador de la relacion
	 * @param operations lista de operaciones de modificación
	 * @return resultado de la modificación
	 */
	public Result<Crew> updateCrew(String id, Integer relationId, List<Map<String, Object>> operations) {

		try {

			Movie movie = movies.findById(id).orElse(null);

			if (movie == null) {
				return new Result<>(null, true, "Movie not found", 0, Result.Code.NOT_FOUND);
			}

			if (movie.getCrew() == null) {
				return new Result<>(null, true, "Crew not found", 0, Result.Code.NOT_FOUND);
			}

			Crew crew = movie.getCrew().stream().filter(c -> c.getRelationId().equals(relationId)).findFirst().orElse(null);

			if (crew == null) {
				return new Result<>(null, true, "Crew not found", 0, Result.Code.NOT_FOUND);
			}

			operations.removeIf(op -> op.containsKey("path") && (op.get("path").equals("/id") || op.get("path").equals("/relationId") || op.get("path").equals("/name")));

			Crew filteredCrew = patchUtil.patch(crew, operations);

			Set<ConstraintViolation<Crew>> violations = validator.validate(filteredCrew, OnRelation.class);

			if (!violations.isEmpty()) {
				return new Result<>(null, true, "Invalid crew", 0, Result.Code.BAD_REQUEST);
			}

			filteredCrew.setRelationId(filteredCrew.hashCode());

			if (movie.getCrew().stream().anyMatch(c -> c.getRelationId().equals(filteredCrew.getRelationId()))) {
				return new Result<>(null, true, "Combination person-job is already in crew", 0, Result.Code.CONFLICT);
			}

			movie.getCrew().remove(crew);
			movie.getCrew().add(filteredCrew);

			movies.save(movie);

			return new Result<>(filteredCrew, false, "Crew updated", 0, Result.Code.OK);

		} catch (Exception e) {

			return new Result<>(null, true, e.getLocalizedMessage(), 0, Result.Code.BAD_REQUEST);

		}

	}

	/**
	 * Metodo que elimina un trabajador del equipo de una película
	 *
	 * @param id         identificador de la película
	 * @param relationId identificador de la relacion
	 * @return resultado de la eliminación
	 */
	public Result<Crew> deleteCrew(String id, Integer relationId) {

		Movie movie = movies.findById(id).orElse(null);

		if (movie == null) {
			return new Result<>(null, true, "Movie not found", 0, Result.Code.NOT_FOUND);
		}

		if (movie.getCrew() == null) {
			return new Result<>(null, true, "Crew not found", 0, Result.Code.NOT_FOUND);
		}

		Crew crew = movie.getCrew().stream().filter(c -> c.getRelationId().equals(relationId)).findFirst().orElse(null);

		if (crew == null) {
			return new Result<>(null, true, "Crew not found", 0, Result.Code.NOT_FOUND);
		}

		movie.getCrew().remove(crew);

		movies.save(movie);

		return new Result<>(crew, false, "Crew removed", 0, Result.Code.OK);

	}

}