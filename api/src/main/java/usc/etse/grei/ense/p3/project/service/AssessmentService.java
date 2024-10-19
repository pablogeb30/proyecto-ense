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

	public Result<List<Assessment>> get(int page, int size, Sort sort, Example<Assessment> filter) {

		Pageable request = PageRequest.of(page, size, sort);

		Criteria criteria = Criteria.byExample(filter);

		Query query = Query.query(criteria).with(request);
		query.fields().include("_id", "rating", "user", "movie", "comment");

		List<Assessment> result = mongo.find(query, Assessment.class);

		return new Result<>(result, false, "Assessments data", 0, Result.Code.OK);

	}

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

			assessments.insert(assessment);

			return new Result<>(assessment, false, "Assessment created", 0, Result.Code.CREATED);

		} catch (Exception e) {

			return new Result<>(null, true, e.getLocalizedMessage(), 0, Result.Code.BAD_REQUEST);

		}

	}

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

			assessments.insert(assessment);

			return new Result<>(assessment, false, "Assessment created", 0, Result.Code.CREATED);

		} catch (Exception e) {

			return new Result<>(null, true, e.getLocalizedMessage(), 0, Result.Code.BAD_REQUEST);

		}

	}

	public Result<Assessment> update(String assessmentId, List<Map<String, Object>> operations) {

		try {

			Assessment assessment = assessments.findById(assessmentId).orElse(null);

			if (assessment == null) {
				return new Result<>(null, false, "Assessment not found", 0, Result.Code.NOT_FOUND);
			}

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

	public Result<Assessment> delete(String assessmentId) {

		Assessment assessment = assessments.findById(assessmentId).orElse(null);

		if (assessment == null) {
			return new Result<>(null, false, "Assessment not found", 0, Result.Code.NOT_FOUND);
		}

		assessments.deleteById(assessment.getId());

		return new Result<>(assessment, false, "Assessment deleted", 0, Result.Code.OK);

	}

}