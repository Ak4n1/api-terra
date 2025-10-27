package com.ak4n1.terra.api.terra_api.game.exceptions;

public class GameAccountNotFoundException extends RuntimeException {
    public GameAccountNotFoundException() {
        super("GAME_ACCOUNT_NOT_FOUND");
    }
    
    public GameAccountNotFoundException(String message) {
        super(message);
    }
}

