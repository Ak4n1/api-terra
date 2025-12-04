package com.ak4n1.terra.api.terra_api.withdrawal.services;

import com.ak4n1.terra.api.terra_api.withdrawal.dto.CharacterPermissionDTO;

import java.util.List;

/**
 * Servicio para gestionar permisos de retiro de Terra Coins por personaje.
 * 
 * @author ak4n1
 * @since 1.0
 */
public interface WithdrawalPermissionService {

    /**
     * Obtiene todos los personajes de una cuenta con su estado de permiso.
     * 
     * @param email Email del account master
     * @return Lista de personajes con estado de permiso
     */
    List<CharacterPermissionDTO> getCharactersWithPermissionStatus(String email);

    /**
     * Otorga permiso de retiro a un personaje.
     * 
     * @param email Email del account master
     * @param characterId ID del personaje
     * @param characterName Nombre del personaje
     * @param ipAddress IP desde donde se realiza la operación
     * @return true si se otorgó el permiso exitosamente
     */
    boolean grantPermission(String email, Integer characterId, String characterName, String ipAddress);

    /**
     * Revoca permiso de retiro a un personaje.
     * 
     * @param email Email del account master
     * @param characterId ID del personaje
     * @param characterName Nombre del personaje
     * @param ipAddress IP desde donde se realiza la operación
     * @return true si se revocó el permiso exitosamente
     */
    boolean revokePermission(String email, Integer characterId, String characterName, String ipAddress);

    /**
     * Verifica si un personaje tiene permiso de retiro.
     * 
     * @param characterId ID del personaje
     * @return true si tiene permiso
     */
    boolean hasPermission(Integer characterId);
}

