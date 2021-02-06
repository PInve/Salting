package it.ttf.invernizzi.salting.services.sdjpa;

import it.ttf.invernizzi.salting.authentication.PasswordAuthentication;
import it.ttf.invernizzi.salting.model.response.AuthResponse;
import it.ttf.invernizzi.salting.model.response.CreationResponse;
import it.ttf.invernizzi.salting.persistence.entities.User;
import it.ttf.invernizzi.salting.persistence.repositories.UserRepository;
import it.ttf.invernizzi.salting.services.UserService;
import it.ttf.invernizzi.salting.validators.UserValidator;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    // can also be initialized with PasswordAuthentication(int cost) constructor
    // to use a custom cost instead of default
    // cost defines the exponential computational cost of hashing a password
    // values allowed: 0  to 30
    // the default cost is 16
    private final PasswordAuthentication passwordAuthentication = new PasswordAuthentication();

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public CreationResponse createUser(String username, String email, String password, String passwordCheck) {
        User newUser = null;
        String outcome = "";
        if(UserValidator.validateUsername(username)) {
            if(userRepository.findByUsername(username) == null) {
                if(UserValidator.validateEmail(email)) {
                    if(PasswordAuthentication.validatePassword(password)) {
                        if(password.equals(passwordCheck)) {
                            newUser = new User(username, email, passwordAuthentication.hash(password.toCharArray()));
                            save(newUser);
                            outcome += "new user created successfully";
                        }
                        else outcome += ("The two inserted passwords don't match");
                    }
                    else outcome += ("Invalid password: must be at least 8 characters, contain a number, at least one uppercase, one lowercase and a special character");
                }
                else outcome += ("Invalid email address");
            }
            else outcome += ("Username already exists");
        }
        else outcome += ("Invalid username");

        return new CreationResponse(outcome, newUser);
    }

    @Override
    public boolean loginUser(String username, String password) {

        User user = findByUsername(username);
        if(user == null) return false;

        String token = user.getToken();
        return passwordAuthentication.authenticate(password.toCharArray(), token);
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }
}
