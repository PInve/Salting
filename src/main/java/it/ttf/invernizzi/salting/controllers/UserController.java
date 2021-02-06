package it.ttf.invernizzi.salting.controllers;

import it.ttf.invernizzi.salting.model.requests.AuthRequest;
import it.ttf.invernizzi.salting.model.requests.NewUserRequest;
import it.ttf.invernizzi.salting.model.response.AuthResponse;
import it.ttf.invernizzi.salting.model.response.CreationResponse;
import it.ttf.invernizzi.salting.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/new")
    public ResponseEntity<CreationResponse> registerNewUser(@RequestBody NewUserRequest registrationJson) {
        CreationResponse outcome = userService.createUser(registrationJson.getUsername(),
                registrationJson.getEmail(),
                registrationJson.getPassword(),
                registrationJson.getPasswordCheck()
        );
        if (outcome.getUser() != null)
            return new ResponseEntity<>(outcome, new HttpHeaders(), HttpStatus.CREATED);
        return new ResponseEntity<>(outcome, new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/login")
    public AuthResponse authenticateUser(@RequestBody AuthRequest authJson) {
        if( userService.loginUser(authJson.getUsername(), authJson.getPassword()))
            return new AuthResponse("Log in successful");
        return new AuthResponse("Invalid username or password");
    }

}
