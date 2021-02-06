package it.ttf.invernizzi.salting.services;

import it.ttf.invernizzi.salting.model.response.CreationResponse;
import it.ttf.invernizzi.salting.persistence.entities.User;

public interface UserService {
    CreationResponse createUser(String username, String email, String password, String passwordCheck);
    boolean loginUser(String username, String password);
    User findByUsername(String username);
    User save(User user);
}
