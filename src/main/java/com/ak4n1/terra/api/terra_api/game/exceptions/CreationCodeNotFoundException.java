package com.ak4n1.terra.api.terra_api.game.exceptions;

public class CreationCodeNotFoundException extends RuntimeException {
    public CreationCodeNotFoundException() {
        super("CREATION_CODE_NOT_FOUND");
    }
    
    public CreationCodeNotFoundException(String message) {
        super(message);
    }
}

