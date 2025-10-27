package com.ak4n1.terra.api.terra_api.game.exceptions;

public class InvalidGamePasswordException extends RuntimeException {
    public InvalidGamePasswordException() {
        super("INVALID_GAME_PASSWORD");
    }
    
    public InvalidGamePasswordException(String message) {
        super(message);
    }
}
