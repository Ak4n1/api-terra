package com.ak4n1.terra.api.terra_api.auth.repositories;

import com.ak4n1.terra.api.terra_api.auth.entities.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Role (roles de usuario).
 * 
 * @see Role
 * @author ak4n1
 * @since 1.0
 */
public interface RoleRepository  extends JpaRepository<Role, Long> {

    /**
     * Busca un rol por su nombre.
     * 
     * @param name Nombre del rol (ej: "ROLE_USER", "ROLE_ADMIN")
     * @return Optional con el rol si existe, vacío si no
     */
    Optional<Role> findByName(String name);





}
