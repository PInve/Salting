package it.ttf.invernizzi.salting.validators;

public class UserValidator {

    public static boolean validateUsername(String username) {
        return username.matches("^[a-zA-Z0-9._-]{3,}$");
    }

    public static boolean validateEmail(String email) {
        return email.matches("^([a-zA-ZÀ-ȕ0-9_\\-.]+)@([a-zA-ZÀ-ȕ0-9_\\-.]+)\\.([a-zA-ZÀ-ȕ]{2,5})$");
    }
}
