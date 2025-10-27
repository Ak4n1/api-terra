package com.ak4n1.terra.api.terra_api.game.exceptions;

public class CreationCodeExpiredException extends RuntimeException {
    public CreationCodeExpiredException() {
        super("CREATION_CODE_EXPIRED");
    }
    
    public CreationCodeExpiredException(String message) {
        super(message);
    }
}

