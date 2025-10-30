package com.ak4n1.terra.api.terra_api.game.services;

import com.ak4n1.terra.api.terra_api.game.dto.ClanResponseDTO;

/**
 * Servicio principal para la gestión de clanes del juego.
 * 
 * <p>Este servicio proporciona métodos para consultar información detallada de clanes,
 * incluyendo miembros, estadísticas, guerras activas y alianzas. Es el servicio
 * RECOMENDADO para interactuar con los datos de clanes desde los controladores.
 * 
 * @see ClanServiceImpl
 * @see ClanResponseDTO
 * @author ak4n1
 * @since 1.0
 */
public interface ClanService {

    /**
     * Obtiene información completa de un clan por su ID.
     * 
     * <p>La información incluye:
     * <ul>
     *   <li>Datos básicos del clan (nombre, nivel, reputación)</li>
     *   <li>Lista de miembros con estado online</li>
     *   <li>Estado de guerra activa</li>
     *   <li>Información de alianzas y líder</li>
     * </ul>
     * 
     * @param id ID del clan a consultar
     * @return DTO con toda la información del clan
     * @throws RuntimeException si el clan no existe
     */
    ClanResponseDTO getClanById(Integer id);

}
