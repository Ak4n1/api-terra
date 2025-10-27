package com.ak4n1.terra.api.terra_api.auth.exceptions;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException() {
        super("USER_NOT_FOUND");
    }
    
    public UserNotFoundException(String message) {
        super(message);
    }
}
