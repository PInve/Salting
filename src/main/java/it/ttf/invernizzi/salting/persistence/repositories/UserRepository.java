package it.ttf.invernizzi.salting.persistence.repositories;

import it.ttf.invernizzi.salting.persistence.entities.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {
    User findByUsername(String username);;
}
