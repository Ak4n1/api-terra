package com.ak4n1.terra.api.terra_api.auth.repositories;

import com.ak4n1.terra.api.terra_api.auth.entities.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad RefreshToken (refresh tokens JWT activos).
 * 
 * @see RefreshToken
 * @author ak4n1
 * @since 1.0
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /**
     * Busca un refresh token activo por su valor de token JWT.
     * 
     * @param token Valor del refresh token JWT
     * @return Optional con el refresh token activo si existe, vacío si no
     */
    Optional<RefreshToken> findByToken(String token);
    
    /**
     * Busca todos los refresh tokens activos de un usuario por su email.
     * 
     * @param email Email del usuario
     * @return Lista de refresh tokens activos del usuario
     */
    List<RefreshToken> findByAccountMaster_Email(String email);

    /**
     * Elimina un refresh token activo por su valor de token JWT.
     * 
     * @param token Valor del refresh token JWT a eliminar
     */
    void deleteByToken(String token);

    /**
     * Elimina todos los refresh tokens antiguos de un usuario por su ID.
     * 
     * @param userId ID del usuario
     */
    @Transactional
    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.accountMaster.id = :userId")
    void deleteOldTokensByUserId(@Param("userId") Long userId);

    /**
     * Elimina todos los refresh tokens expirados y revocados.
     * 
     * @param now Fecha actual para comparar con expiresAt
     */
    @Transactional
    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.expiresAt < :now OR r.revoked = true")
    void deleteExpiredAndRevokedTokens(@Param("now") Date now);

    /**
     * Revoca todos los refresh tokens activos de un usuario por su ID.
     * 
     * @param userId ID del usuario
     */
    @Transactional
    @Modifying
    @Query("UPDATE RefreshToken r SET r.revoked = true WHERE r.accountMaster.id = :userId AND r.revoked = false")
    void revokeAllByUserId(@Param("userId") Long userId);
}

