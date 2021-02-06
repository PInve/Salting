package it.ttf.invernizzi.salting.model.response;

import it.ttf.invernizzi.salting.persistence.entities.User;
import lombok.Data;

@Data
public class CreationResponse {
    String outcome;
    UserDtO user;

    public CreationResponse(String outcome, User user) {
        this.outcome = outcome;
        this.user = UserDtO.fromUser(user);
    }

    @Data
    static class UserDtO {
        Long id;
        String username, email;

        static UserDtO fromUser(User u) {
            UserDtO newUserDTO = new UserDtO();
            if(u != null) {
                newUserDTO.setId(u.getId());
                newUserDTO.setEmail(u.getEmail());
                newUserDTO.setUsername(u.getUsername());
            }
            return newUserDTO;
        }
    }
}
