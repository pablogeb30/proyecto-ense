package usc.etse.grei.ense.p3.project.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import usc.etse.grei.ense.p3.project.model.Assessment;

public interface AssessmentRepository extends MongoRepository<Assessment, String> {

}