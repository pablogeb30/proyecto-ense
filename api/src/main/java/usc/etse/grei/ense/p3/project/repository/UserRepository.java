package usc.etse.grei.ense.p3.project.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import usc.etse.grei.ense.p3.project.model.User;

/**
 * Interfaz de acceso a los datos de usuarios
 */
public interface UserRepository extends MongoRepository<User, String> {

}