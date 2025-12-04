package com.ak4n1.terra.api.terra_api.streamer.entities;

/**
 * Enum que representa el estado de una solicitud de streamer.
 * 
 * @author ak4n1
 * @since 1.0
 */
public enum ApplicationStatus {
    PENDING,    // Pendiente de revisión
    APPROVED,   // Aprobada (se asigna ROLE_STREAMER)
    REJECTED    // Rechazada
}

