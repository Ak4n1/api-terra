package com.ak4n1.terra.api.terra_api.auth.exceptions;

public class UserDisabledException extends RuntimeException {
    public UserDisabledException() {
        super("USER_DISABLED");
    }
    
    public UserDisabledException(String message) {
        super(message);
    }
}
