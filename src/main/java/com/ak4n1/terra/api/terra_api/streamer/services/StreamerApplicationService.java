package com.ak4n1.terra.api.terra_api.streamer.services;

import com.ak4n1.terra.api.terra_api.streamer.dto.StreamerApplicationDTO;
import com.ak4n1.terra.api.terra_api.streamer.dto.StreamerApplicationResponseDTO;
import com.ak4n1.terra.api.terra_api.streamer.entities.ApplicationStatus;

import java.util.List;

/**
 * Interfaz del servicio para gestionar solicitudes de streamer.
 * 
 * @author ak4n1
 * @since 1.0
 */
public interface StreamerApplicationService {

    /**
     * Crea una nueva solicitud de streamer.
     * 
     * @param dto DTO con los datos de la solicitud
     * @param accountId ID del usuario que envía la solicitud
     * @return DTO con la solicitud creada
     * @throws IllegalStateException si el usuario ya tiene una solicitud pendiente o ya tiene el rol de streamer
     */
    StreamerApplicationResponseDTO submitApplication(StreamerApplicationDTO dto, Long accountId);

    /**
     * Obtiene todas las solicitudes de un usuario.
     * 
     * @param accountId ID del usuario
     * @return Lista de solicitudes del usuario
     */
    List<StreamerApplicationResponseDTO> getUserApplications(Long accountId);

    /**
     * Obtiene una solicitud específica por ID.
     * 
     * @param applicationId ID de la solicitud
     * @param accountId ID del usuario (para verificar que le pertenece)
     * @return DTO con la solicitud
     * @throws IllegalArgumentException si la solicitud no existe o no pertenece al usuario
     */
    StreamerApplicationResponseDTO getApplicationById(Long applicationId, Long accountId);

    /**
     * Obtiene todas las solicitudes aprobadas.
     * 
     * @return Lista de solicitudes aprobadas
     */
    List<StreamerApplicationResponseDTO> getApprovedApplications();

    /**
     * Obtiene todas las solicitudes pendientes.
     * 
     * @return Lista de solicitudes pendientes
     */
    List<StreamerApplicationResponseDTO> getPendingApplications();

    /**
     * Obtiene todas las solicitudes rechazadas.
     * 
     * @return Lista de solicitudes rechazadas
     */
    List<StreamerApplicationResponseDTO> getRejectedApplications();
}

