package com.ak4n1.terra.api.terra_api.auth.exceptions;

public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException() {
        super("INVALID_CREDENTIALS");
    }
    
    public InvalidCredentialsException(String message) {
        super(message);
    }
}
