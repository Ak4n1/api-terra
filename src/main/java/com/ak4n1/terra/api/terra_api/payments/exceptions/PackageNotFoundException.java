package com.ak4n1.terra.api.terra_api.payments.exceptions;

/**
 * Excepción lanzada cuando un paquete de monedas no se encuentra o no está activo
 */
public class PackageNotFoundException extends RuntimeException {
    
    public PackageNotFoundException(String message) {
        super(message);
    }
    
    public PackageNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

