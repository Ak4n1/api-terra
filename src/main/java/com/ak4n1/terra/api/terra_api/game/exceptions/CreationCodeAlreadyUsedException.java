package com.ak4n1.terra.api.terra_api.game.exceptions;

public class CreationCodeAlreadyUsedException extends RuntimeException {
    public CreationCodeAlreadyUsedException() {
        super("CREATION_CODE_ALREADY_USED");
    }
    
    public CreationCodeAlreadyUsedException(String message) {
        super(message);
    }
}

