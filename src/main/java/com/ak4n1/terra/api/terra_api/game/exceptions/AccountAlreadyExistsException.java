package com.ak4n1.terra.api.terra_api.game.exceptions;

public class AccountAlreadyExistsException extends RuntimeException {
    public AccountAlreadyExistsException() {
        super("GAME_ACCOUNT_ALREADY_EXISTS");
    }
    
    public AccountAlreadyExistsException(String message) {
        super(message);
    }
}
