package com.ak4n1.terra.api.terra_api.payments.exceptions;

/**
 * Excepción base para errores relacionados con pagos
 */
public class PaymentException extends RuntimeException {
    
    public PaymentException(String message) {
        super(message);
    }
    
    public PaymentException(String message, Throwable cause) {
        super(message, cause);
    }
}

