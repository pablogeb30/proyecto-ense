package usc.etse.grei.ense.p3.project.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import usc.etse.grei.ense.p3.project.model.Person;

public interface PeopleRepository extends MongoRepository<Person, String> {

}