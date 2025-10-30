package com.ak4n1.terra.api.terra_api.auth.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ak4n1.terra.api.terra_api.auth.entities.AccountMaster;

import java.util.Optional;

/**
 * Repositorio para la entidad AccountMaster (usuarios principales).
 * 
 * @see AccountMaster
 * @author ak4n1
 * @since 1.0
 */
public interface AccountMasterRepository extends JpaRepository<AccountMaster, Long> {

    /**
     * Busca un usuario por su token de verificación de email.
     * 
     * @param verificationToken Token de verificación
     * @return Optional con el usuario si existe, vacío si no
     */
    Optional<AccountMaster> findByVerificationToken(String verificationToken);
    
    /**
     * Verifica si existe un usuario con el email dado.
     * 
     * @param email Email a verificar
     * @return true si existe, false si no
     */
    boolean existsByEmail(String email);
    
    /**
     * Busca un usuario por su email.
     * 
     * @param email Email del usuario
     * @return Optional con el usuario si existe, vacío si no
     */
    Optional<AccountMaster> findByEmail(String email);
    
    /**
     * Busca un usuario por su token de reseteo de contraseña.
     * 
     * @param tokenUser Token de reseteo
     * @return Optional con el usuario si existe, vacío si no
     */
    Optional<AccountMaster> findByPasswordResetToken(String tokenUser);

}
