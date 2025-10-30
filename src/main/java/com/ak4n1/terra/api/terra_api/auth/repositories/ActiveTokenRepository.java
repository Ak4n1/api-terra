package com.ak4n1.terra.api.terra_api.auth.repositories;

import com.ak4n1.terra.api.terra_api.game.entities.AccountGame;
import com.ak4n1.terra.api.terra_api.auth.entities.ActiveToken;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad ActiveToken (tokens JWT activos).
 * 
 * @see ActiveToken
 * @author ak4n1
 * @since 1.0
 */
@Repository
public interface ActiveTokenRepository extends CrudRepository<ActiveToken, Long> {

    /**
     * Busca un token activo por su valor de token JWT.
     * 
     * @param token Valor del token JWT
     * @return Optional con el token activo si existe, vacío si no
     */
    Optional<ActiveToken> findByToken(String token);
    
    /**
     * Busca todos los tokens activos de un usuario por su email.
     * 
     * @param email Email del usuario
     * @return Lista de tokens activos del usuario
     */
    List<ActiveToken> findByAccountMaster_Email(String email);

    /**
     * Elimina todos los tokens antiguos de un usuario por su ID.
     * 
     * @param userId ID del usuario
     */
    @Transactional
    @Modifying
    @Query("DELETE FROM ActiveToken a WHERE a.accountMaster.id = :userId")
    void deleteOldTokensByUserId(@Param("userId") Long userId);

    /**
     * Elimina un token activo por su valor de token JWT.
     * 
     * @param token Valor del token JWT a eliminar
     */
    void deleteByToken(String token);

}
