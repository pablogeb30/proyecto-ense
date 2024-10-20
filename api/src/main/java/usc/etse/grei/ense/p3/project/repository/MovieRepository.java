package usc.etse.grei.ense.p3.project.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import usc.etse.grei.ense.p3.project.model.Movie;

/**
 * Interfaz de acceso a los datos de películas
 */
public interface MovieRepository extends MongoRepository<Movie, String> {

}