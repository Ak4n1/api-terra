package com.ak4n1.terra.api.terra_api.auth.exceptions;

public class TokenExpiredException extends RuntimeException {
    public TokenExpiredException() {
        super("TOKEN_EXPIRED");
    }
    
    public TokenExpiredException(String message) {
        super(message);
    }
}
