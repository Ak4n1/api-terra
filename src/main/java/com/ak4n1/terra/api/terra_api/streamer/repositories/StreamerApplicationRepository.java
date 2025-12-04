package com.ak4n1.terra.api.terra_api.streamer.repositories;

import com.ak4n1.terra.api.terra_api.streamer.entities.ApplicationStatus;
import com.ak4n1.terra.api.terra_api.streamer.entities.StreamerApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad StreamerApplication.
 * 
 * @author ak4n1
 * @since 1.0
 */
@Repository
public interface StreamerApplicationRepository extends JpaRepository<StreamerApplication, Long> {

    /**
     * Busca todas las solicitudes de un usuario específico.
     * 
     * @param accountId ID del usuario
     * @return Lista de solicitudes ordenadas por fecha de envío descendente
     */
    @Query("SELECT sa FROM StreamerApplication sa WHERE sa.accountMaster.id = :accountId ORDER BY sa.submittedAt DESC")
    List<StreamerApplication> findByAccountMasterId(@Param("accountId") Long accountId);

    /**
     * Busca todas las solicitudes pendientes.
     * 
     * @return Lista de solicitudes pendientes ordenadas por fecha de envío ascendente
     */
    @Query("SELECT sa FROM StreamerApplication sa WHERE sa.status = :status ORDER BY sa.submittedAt ASC")
    List<StreamerApplication> findByStatus(@Param("status") ApplicationStatus status);

    /**
     * Verifica si el usuario tiene una solicitud pendiente.
     * 
     * @param accountId ID del usuario
     * @return Optional con la solicitud pendiente si existe
     */
    @Query("SELECT sa FROM StreamerApplication sa WHERE sa.accountMaster.id = :accountId AND sa.status = :status")
    Optional<StreamerApplication> findPendingByAccountMasterId(@Param("accountId") Long accountId, @Param("status") ApplicationStatus status);

    /**
     * Busca una solicitud por ID con todas sus relaciones cargadas.
     * 
     * @param id ID de la solicitud
     * @return Optional con la solicitud si existe
     */
    @Query("SELECT sa FROM StreamerApplication sa LEFT JOIN FETCH sa.platforms WHERE sa.id = :id")
    Optional<StreamerApplication> findByIdWithPlatforms(@Param("id") Long id);
}

