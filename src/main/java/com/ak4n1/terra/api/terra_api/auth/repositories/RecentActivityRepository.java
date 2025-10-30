package com.ak4n1.terra.api.terra_api.auth.repositories;


import com.ak4n1.terra.api.terra_api.auth.entities.RecentActivity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repositorio para la entidad RecentActivity (actividad reciente de usuarios).
 * 
 * @see RecentActivity
 * @author ak4n1
 * @since 1.0
 */
public interface RecentActivityRepository extends JpaRepository<RecentActivity, Long> {

    /**
     * Busca todas las actividades recientes de un usuario ordenadas por timestamp descendente.
     * 
     * @param email Email del usuario
     * @return Lista de actividades recientes ordenadas de más reciente a más antigua
     */
    List<RecentActivity> findByAccountMaster_EmailOrderByTimestampDesc(String email);

}