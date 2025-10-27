package com.ak4n1.terra.api.terra_api.auth.exceptions;

public class EmailNotVerifiedException extends RuntimeException {
    public EmailNotVerifiedException() {
        super("EMAIL_NOT_VERIFIED");
    }
    
    public EmailNotVerifiedException(String message) {
        super(message);
    }
}
