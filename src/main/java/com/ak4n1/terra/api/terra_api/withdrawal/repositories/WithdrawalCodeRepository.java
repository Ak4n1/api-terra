package com.ak4n1.terra.api.terra_api.withdrawal.repositories;

import com.ak4n1.terra.api.terra_api.withdrawal.entities.WithdrawalCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.Optional;

/**
 * Repositorio para gestionar códigos de retiro de Terra Coins.
 * 
 * @author ak4n1
 * @since 1.0
 */
@Repository
public interface WithdrawalCodeRepository extends JpaRepository<WithdrawalCode, Long> {
    
    /**
     * Busca el código más reciente por email.
     */
    Optional<WithdrawalCode> findTopByEmailOrderByCreatedAtDesc(String email);
    
    /**
     * Busca un código válido (no usado y no expirado) por email.
     */
    @Query("SELECT w FROM WithdrawalCode w WHERE w.email = :email AND w.used = false AND w.expiresAt > :now ORDER BY w.createdAt DESC")
    Optional<WithdrawalCode> findValidCodeByEmail(@Param("email") String email, @Param("now") Timestamp now);
    
    /**
     * Busca un código por email y código (para validación desde L2J).
     */
    @Query("SELECT w FROM WithdrawalCode w WHERE w.email = :email AND w.code = :code AND w.used = false AND w.expiresAt > :now")
    Optional<WithdrawalCode> findValidCode(@Param("email") String email, @Param("code") String code, @Param("now") Timestamp now);
}

