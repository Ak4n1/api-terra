package com.ak4n1.terra.api.terra_api.withdrawal.repositories;

import com.ak4n1.terra.api.terra_api.withdrawal.entities.CharacterWithdrawalPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para gestionar permisos de retiro de personajes.
 * 
 * @author ak4n1
 * @since 1.0
 */
@Repository
public interface CharacterWithdrawalPermissionRepository extends JpaRepository<CharacterWithdrawalPermission, Long> {

    /**
     * Busca todos los permisos de retiro para un email de cuenta.
     * 
     * @param accountEmail Email del account master
     * @return Lista de permisos
     */
    List<CharacterWithdrawalPermission> findByAccountEmail(String accountEmail);

    /**
     * Busca un permiso específico por email y character ID.
     * 
     * @param accountEmail Email del account master
     * @param characterId ID del personaje
     * @return Permiso si existe
     */
    Optional<CharacterWithdrawalPermission> findByAccountEmailAndCharacterId(String accountEmail, Integer characterId);

    /**
     * Verifica si existe un permiso para un personaje específico.
     * 
     * @param characterId ID del personaje
     * @return true si tiene permiso
     */
    boolean existsByCharacterId(Integer characterId);

    /**
     * Elimina el permiso de un personaje específico.
     * 
     * @param accountEmail Email del account master
     * @param characterId ID del personaje
     */
    void deleteByAccountEmailAndCharacterId(String accountEmail, Integer characterId);

    /**
     * Obtiene los IDs de personajes con permiso para una cuenta.
     * 
     * @param accountEmail Email del account master
     * @return Lista de character IDs con permiso
     */
    @Query("SELECT p.characterId FROM CharacterWithdrawalPermission p WHERE p.accountEmail = :email")
    List<Integer> findCharacterIdsByAccountEmail(@Param("email") String accountEmail);
}

