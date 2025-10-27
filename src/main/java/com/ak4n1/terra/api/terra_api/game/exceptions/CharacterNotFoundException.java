package com.ak4n1.terra.api.terra_api.game.exceptions;

public class CharacterNotFoundException extends RuntimeException {
    public CharacterNotFoundException() {
        super("CHARACTER_NOT_FOUND");
    }
    
    public CharacterNotFoundException(String message) {
        super(message);
    }
}
