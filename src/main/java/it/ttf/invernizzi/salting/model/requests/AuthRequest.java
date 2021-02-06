package it.ttf.invernizzi.salting.model.requests;

import lombok.Data;

@Data
public class AuthRequest {
    private String username, password;
}
