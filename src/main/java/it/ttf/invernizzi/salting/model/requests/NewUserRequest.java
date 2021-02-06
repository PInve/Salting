package it.ttf.invernizzi.salting.model.requests;

import lombok.Data;

@Data
public class NewUserRequest {
    private String username, email, password, passwordCheck;
}
