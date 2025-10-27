package com.ak4n1.terra.api.terra_api.auth.exceptions;

public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException() {
        super("EMAIL_ALREADY_EXISTS");
    }
    
    public EmailAlreadyExistsException(String message) {
        super(message);
    }
}
