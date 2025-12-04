package com.ak4n1.terra.api.terra_api.security.services;

import com.ak4n1.terra.api.terra_api.auth.repositories.AccountMasterRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * Resuelve el userId del usuario autenticado a partir del Authentication.
 * 
 * <p>Compatibilidad: si el principal es un email, lo resuelve a id consultando la BD.
 * Si el principal ya es un userId (String numérico), lo devuelve directamente.
 */
@Component
public class CurrentUserResolver {

    private final AccountMasterRepository accountMasterRepository;

    public CurrentUserResolver(AccountMasterRepository accountMasterRepository) {
        this.accountMasterRepository = accountMasterRepository;
    }

    /**
     * Obtiene el userId (Long) desde el Authentication actual.
     *
     * @param authentication contexto de seguridad
     * @return userId del usuario autenticado, o null si no puede resolverse
     */
    public Long resolveCurrentUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (principal == null) {
            return null;
        }

        String principalStr = String.valueOf(principal);
        // Si ya es numérico, devolverlo como ID
        try {
            return Long.valueOf(principalStr);
        } catch (NumberFormatException ignored) { }

        // Si es email, resolver a id en BD
        return accountMasterRepository.findByEmail(principalStr)
                .map(u -> u.getId())
                .orElse(null);
    }
}


