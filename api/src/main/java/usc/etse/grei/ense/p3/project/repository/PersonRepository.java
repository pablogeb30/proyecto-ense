package usc.etse.grei.ense.p3.project.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import usc.etse.grei.ense.p3.project.model.Person;

/**
 * Interfaz de acceso a los datos de personas
 */
public interface PersonRepository extends MongoRepository<Person, String> {

}