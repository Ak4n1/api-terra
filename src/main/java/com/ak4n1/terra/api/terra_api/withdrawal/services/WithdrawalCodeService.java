package com.ak4n1.terra.api.terra_api.withdrawal.services;

import java.util.Map;

/**
 * Servicio para gestionar códigos de retiro de Terra Coins.
 * 
 * @author ak4n1
 * @since 1.0
 */
public interface WithdrawalCodeService {
    
    /**
     * Genera un código de retiro y lo envía por email.
     * 
     * @param email Email del usuario
     * @return Map con el resultado de la operación
     */
    Map<String, Object> generateAndSendCode(String email);
    
    /**
     * Valida un código de retiro (usado desde L2J).
     * 
     * @param email Email del usuario
     * @param code Código de 6 dígitos
     * @return true si el código es válido
     */
    boolean validateCode(String email, String code);
    
    /**
     * Marca un código como usado.
     * 
     * @param email Email del usuario
     * @param code Código de 6 dígitos
     * @return true si se marcó correctamente
     */
    boolean markCodeAsUsed(String email, String code);
}

