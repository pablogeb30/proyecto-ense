package usc.etse.grei.ense.p3.project.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import usc.etse.grei.ense.p3.project.model.Assessment;
import usc.etse.grei.ense.p3.project.model.Movie;
import usc.etse.grei.ense.p3.project.model.User;
import usc.etse.grei.ense.p3.project.repository.AssessmentRepository;
import usc.etse.grei.ense.p3.project.repository.MovieRepository;
import usc.etse.grei.ense.p3.project.repository.UserRepository;
import usc.etse.grei.ense.p3.project.util.PatchUtil;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AssessmentService {

	private final AssessmentRepository assessments;
	private final MongoTemplate mongo;
	private final PatchUtil patchUtil;
	private final UserRepository users;
	private final MovieRepository movies;


	@Autowired
	public AssessmentService(AssessmentRepository assessments, UserRepository users, MovieRepository movies, MongoTemplate mongo, PatchUtil patchUtil) {
		this.users = users;
		this.movies = movies;
		this.assessments = assessments;
		this.mongo = mongo;
		this.patchUtil = patchUtil;
	}

	public Optional<Page<Assessment>> get(int page, int size, Sort sort, Example<Assessment> filter) {

		Pageable request = PageRequest.of(page, size, sort);

		Criteria criteria = Criteria.byExample(filter);

		Query query = Query.query(criteria).with(request);
		query.fields().include("_id", "rating", "user", "movie", "comment");

		List<Assessment> result = mongo.find(query, Assessment.class);

		return Optional.of(new PageImpl<>(result, request, result.size()));

	}

	public Optional<Assessment> createForMovie(String movieId, Assessment assessment) {

		try {

			Optional<Movie> existMovie = movies.findById(movieId);

			if (existMovie.isEmpty()) {
				return Optional.empty();
			}

			Movie movie = new Movie();
			movie.setId(existMovie.get().getId());
			movie.setTitle(existMovie.get().getTitle());

			assessment.setMovie(movie);

			Optional<User> existUser = users.findById(assessment.getUser().getEmail());

			if (existUser.isEmpty()) {
				return Optional.empty();
			}

			assessments.insert(assessment);
			return Optional.of(assessment);

		} catch (Exception e) {

			return Optional.empty();

		}

	}

	public Optional<Assessment> createForUser(String userId, Assessment assessment) {

		try {

			Optional<User> existUser = users.findById(userId);

			if (existUser.isEmpty()) {
				return Optional.empty();
			}

			User user = new User();
			user.setEmail(existUser.get().getEmail());
			user.setName(existUser.get().getName());

			assessment.setUser(user);

			Optional<Movie> existMovie = movies.findById(assessment.getMovie().getId());

			if (existMovie.isEmpty()) {
				return Optional.empty();
			}

			assessments.insert(assessment);
			return Optional.of(assessment);

		} catch (Exception e) {

			return Optional.empty();

		}

	}

	public Optional<Assessment> update(String assessmentId, List<Map<String, Object>> operations) {

		try {

			Optional<Assessment> assessmentResult = assessments.findById(assessmentId);

			if (assessmentResult.isEmpty()) {
				return Optional.empty();
			}

			operations.removeIf(op -> op.containsKey("path") && (op.get("path").equals("/movie") || op.get("path").equals("/user")));

			Assessment filteredAssessment = patchUtil.patch(assessmentResult.get(), operations);
			Assessment updatedAssessment = assessments.save(filteredAssessment);
			return Optional.of(updatedAssessment);

		} catch (Exception e) {

			return Optional.empty();

		}

	}

	public Optional<Assessment> delete(String assessmentId) {

		Optional<Assessment> assessmentResult = assessments.findById(assessmentId);

		if (assessmentResult.isEmpty()) {
			return Optional.empty();
		}

		Assessment assessment = assessmentResult.get();

		assessments.deleteById(assessment.getId());

		return Optional.of(assessment);

	}

}