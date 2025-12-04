package com.ak4n1.terra.api.terra_api.auth.repositories;

import com.ak4n1.terra.api.terra_api.auth.entities.AccountRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para la entidad AccountRole.
 * 
 * <p>Proporciona métodos de acceso a la tabla accounts_roles.
 * 
 * @author ak4n1
 * @since 1.0
 */
@Repository
public interface AccountRoleRepository extends JpaRepository<AccountRole, AccountRole.AccountRoleId> {

    /**
     * Encuentra todas las relaciones de un usuario por su ID.
     * 
     * @param accountId ID del usuario
     * @return Lista de AccountRole asociadas al usuario
     */
    List<AccountRole> findByAccountId(Long accountId);

    /**
     * Encuentra todas las relaciones de un rol por su ID.
     * 
     * @param roleId ID del rol
     * @return Lista de AccountRole asociadas al rol
     */
    List<AccountRole> findByRoleId(Long roleId);

    /**
     * Elimina todas las relaciones de un usuario.
     * 
     * @param accountId ID del usuario
     */
    void deleteByAccountId(Long accountId);

    /**
     * Elimina todas las relaciones de un rol.
     * 
     * @param roleId ID del rol
     */
    void deleteByRoleId(Long roleId);
}

