package usc.etse.grei.ense.p3.project.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import usc.etse.grei.ense.p3.project.model.Assessment;

/**
 * Interfaz de acceso a los datos de comentarios
 */
public interface AssessmentRepository extends MongoRepository<Assessment, String> {

}