package com.ak4n1.terra.api.terra_api.game.exceptions;

public class InvalidCreationCodeException extends RuntimeException {
    public InvalidCreationCodeException() {
        super("INVALID_CREATION_CODE");
    }
    
    public InvalidCreationCodeException(String message) {
        super(message);
    }
}

