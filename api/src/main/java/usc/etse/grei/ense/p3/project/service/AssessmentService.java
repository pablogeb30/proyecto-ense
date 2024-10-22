package usc.etse.grei.ense.p3.project.service;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import usc.etse.grei.ense.p3.project.model.*;
import usc.etse.grei.ense.p3.project.repository.AssessmentRepository;
import usc.etse.grei.ense.p3.project.repository.MovieRepository;
import usc.etse.grei.ense.p3.project.repository.UserRepository;
import usc.etse.grei.ense.p3.project.util.PatchUtil;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Servicio que implementa la lógica de negocio para comentarios
 */
@Service
public class AssessmentService {

	private final AssessmentRepository assessments;
	private final MongoTemplate mongo;
	private final PatchUtil patchUtil;
	private final UserRepository users;
	private final MovieRepository movies;
	private final Validator validator;

	@Autowired
	public AssessmentService(AssessmentRepository assessments, UserRepository users, MovieRepository movies, MongoTemplate mongo, PatchUtil patchUtil, Validator validator) {
		this.users = users;
		this.movies = movies;
		this.assessments = assessments;
		this.mongo = mongo;
		this.patchUtil = patchUtil;
		this.validator = validator;
	}

	/**
	 * Metodo que obtiene una lista de comentarios utilizando filtrado y ordenación
	 *
	 * @param page   número de página
	 * @param size   número de comentarios por página
	 * @param sort   criterio de ordenación
	 * @param filter criterio de filtrado por usuario o película
	 * @return resultado de la búsqueda
	 */
	public Result<List<Assessment>> get(int page, int size, Sort sort, Example<Assessment> filter) {

		Pageable request = PageRequest.of(page, size, sort);

		Criteria criteria = Criteria.byExample(filter);

		Query query = Query.query(criteria).with(request);
		query.fields().include("_id", "rating", "user", "movie", "comment");

		List<Assessment> result = mongo.find(query, Assessment.class);

		return new Result<>(result, false, "Assessments data", 0, Result.Code.OK);

	}

	/**
	 * Metodo que añade un comentario a la base de datos a partir de la película
	 *
	 * @param movieId    identificador de la película
	 * @param assessment comentario añadido
	 * @return resultado de la inserción
	 */
	public Result<Assessment> createForMovie(String movieId, Assessment assessment) {

		try {

			Movie movie = movies.findById(movieId).orElse(null);

			if (movie == null) {
				return new Result<>(null, false, "Movie not found", 0, Result.Code.NOT_FOUND);
			}

			Movie relationMovie = new Movie();
			relationMovie.setId(movie.getId());
			relationMovie.setTitle(movie.getTitle());

			assessment.setMovie(relationMovie);

			User existUser = users.findById(assessment.getUser().getEmail()).orElse(null);

			if (existUser == null) {
				return new Result<>(null, false, "User not found", 0, Result.Code.NOT_FOUND);
			}

			User relationUser = new User();
			relationUser.setEmail(existUser.getEmail());
			relationUser.setName(existUser.getName());

			Example<Assessment> assessmentExample = Example.of(new Assessment().setUser(relationUser).setMovie(relationMovie));
			if (assessments.findOne(assessmentExample).isPresent()) {
				return new Result<>(null, false, "Assessment already exists for this user and movie", 0, Result.Code.CONFLICT);
			}

			assessments.insert(assessment);

			return new Result<>(assessment, false, "Assessment created", 0, Result.Code.CREATED);

		} catch (Exception e) {

			return new Result<>(null, true, e.getLocalizedMessage(), 0, Result.Code.BAD_REQUEST);

		}

	}

	/**
	 * Metodo que añade un comentario a la base de datos a partir del usuario
	 *
	 * @param userId     identificador del usuario
	 * @param assessment comentario añadido
	 * @return resultado de la inserción
	 */
	public Result<Assessment> createForUser(String userId, Assessment assessment) {

		try {

			User user = users.findById(userId).orElse(null);

			if (user == null) {
				return new Result<>(null, false, "User not found", 0, Result.Code.NOT_FOUND);
			}

			User relationUser = new User();
			relationUser.setEmail(user.getEmail());
			relationUser.setName(user.getName());

			assessment.setUser(relationUser);

			Movie existMovie = movies.findById(assessment.getMovie().getId()).orElse(null);

			if (existMovie == null) {
				return new Result<>(null, false, "Movie not found", 0, Result.Code.NOT_FOUND);
			}

			Movie relationMovie = new Movie();
			relationMovie.setId(existMovie.getId());
			relationMovie.setTitle(existMovie.getTitle());

			Example<Assessment> assessmentExample = Example.of(new Assessment().setUser(relationUser).setMovie(relationMovie));
			if (assessments.findOne(assessmentExample).isPresent()) {
				return new Result<>(null, false, "Assessment already exists for this user and movie", 0, Result.Code.CONFLICT);
			}

			assessments.insert(assessment);

			return new Result<>(assessment, false, "Assessment created", 0, Result.Code.CREATED);

		} catch (Exception e) {

			return new Result<>(null, true, e.getLocalizedMessage(), 0, Result.Code.BAD_REQUEST);

		}

	}

	/**
	 * Metodo que modifica la información de un comentario existente a partir de la película
	 *
	 * @param movieId      identificador de la película
	 * @param assessmentId identificador del comentario
	 * @param operations   lista de operaciones de modificación
	 * @return resultado de la modificación
	 */
	public Result<Assessment> updateForMovie(String movieId, String assessmentId, List<Map<String, Object>> operations) {

		Assessment assessment = assessments.findById(assessmentId).orElse(null);

		if (assessment == null || assessment.getMovie() == null || !assessment.getMovie().getId().equals(movieId)) {
			return new Result<>(null, false, "Assessment not found", 0, Result.Code.NOT_FOUND);
		}

		return update(assessment, operations);

	}

	/**
	 * Metodo que modifica la información de un comentario existente a partir del usuario
	 *
	 * @param userId       identificador del usuario
	 * @param assessmentId identificador del comentario
	 * @param operations   lista de operaciones de modificación
	 * @return resultado de la modificación
	 */
	public Result<Assessment> updateForUser(String userId, String assessmentId, List<Map<String, Object>> operations) {

		Assessment assessment = assessments.findById(assessmentId).orElse(null);

		if (assessment == null || assessment.getUser() == null || !assessment.getUser().getEmail().equals(userId)) {
			return new Result<>(null, false, "Assessment not found", 0, Result.Code.NOT_FOUND);
		}

		return update(assessment, operations);

	}

	/**
	 * Metodo que modifica la información de un comentario existente
	 *
	 * @param assessment comentario
	 * @param operations lista de operaciones de modificación
	 * @return resultado de la modificación
	 */
	public Result<Assessment> update(Assessment assessment, List<Map<String, Object>> operations) {

		try {

			operations.removeIf(op -> op.containsKey("path") && (op.get("path").equals("/movie") || op.get("path").equals("/user")));

			Assessment filteredAssessment = patchUtil.patch(assessment, operations);

			Set<ConstraintViolation<Assessment>> violations = validator.validate(filteredAssessment, OnRelation.class);

			if (!violations.isEmpty()) {
				return new Result<>(null, true, "Not valid due to violations", 0, Result.Code.BAD_REQUEST);
			}

			Assessment updatedAssessment = assessments.save(filteredAssessment);

			return new Result<>(updatedAssessment, false, "Assessment updated", 0, Result.Code.OK);

		} catch (Exception e) {

			return new Result<>(null, true, e.getLocalizedMessage(), 0, Result.Code.BAD_REQUEST);

		}

	}

	/**
	 * Metodo que elimina un comentario de la base de datos a partir de la película
	 *
	 * @param movieId      identificador de la película
	 * @param assessmentId identificador del comentario
	 * @return resultado de la eliminación
	 */
	public Result<Assessment> deleteForMovie(String movieId, String assessmentId) {

		Assessment assessment = assessments.findById(assessmentId).orElse(null);

		if (assessment == null || assessment.getMovie() == null || !assessment.getMovie().getId().equals(movieId)) {
			return new Result<>(null, false, "Assessment not found", 0, Result.Code.NOT_FOUND);
		}

		return delete(assessment);

	}

	/**
	 * Metodo que elimina un comentario de la base de datos a partir del usuario
	 *
	 * @param userId       identificador del usuario
	 * @param assessmentId identificador del comentario
	 * @return resultado de la eliminación
	 */
	public Result<Assessment> deleteForUser(String userId, String assessmentId) {

		Assessment assessment = assessments.findById(assessmentId).orElse(null);

		if (assessment == null || assessment.getUser() == null || !assessment.getUser().getEmail().equals(userId)) {
			return new Result<>(null, false, "Assessment not found", 0, Result.Code.NOT_FOUND);
		}

		return delete(assessment);

	}

	/**
	 * Metodo que elimina un comentario de la base de datos
	 *
	 * @param assessment comentario
	 * @return resultado de la eliminación
	 */
	public Result<Assessment> delete(Assessment assessment) {

		assessments.deleteById(assessment.getId());

		return new Result<>(assessment, false, "Assessment deleted", 0, Result.Code.OK);

	}

}