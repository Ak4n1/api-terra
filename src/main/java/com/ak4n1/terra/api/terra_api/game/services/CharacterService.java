package com.ak4n1.terra.api.terra_api.game.services;

import com.ak4n1.terra.api.terra_api.game.dto.CharacterResponseDTO;
import com.ak4n1.terra.api.terra_api.game.entities.Character;

import java.util.List;

/**
 * Servicio principal para la gestión de personajes del juego.
 * 
 * <p>Este servicio proporciona métodos para consultar información de personajes
 * tanto por nombre de cuenta como por email del usuario. Es el servicio RECOMENDADO
 * para interactuar con los datos de personajes desde los controladores.
 * 
 * @see CharacterServiceImpl
 * @see Character
 * @see CharacterResponseDTO
 * @author ak4n1
 * @since 1.0
 */
public interface CharacterService {

    /**
     * Obtiene todos los personajes asociados a una cuenta de juego por nombre.
     * 
     * @param accountName Nombre de la cuenta de juego (login)
     * @return Lista de entidades Character asociadas a la cuenta
     */
    List<Character> getCharactersByAccountName(String accountName);

    /**
     * Obtiene todos los personajes como DTOs asociados a una cuenta de juego por nombre.
     * 
     * @param accountName Nombre de la cuenta de juego (login)
     * @return Lista de DTOs CharacterResponseDTO con la información de los personajes
     */
    List<CharacterResponseDTO> getCharactersDTOByAccountName(String accountName);

    /**
     * Obtiene los personajes principales (máximo 5) asociados a un email de usuario.
     * 
     * <p>Este método está optimizado para mostrar una vista rápida y retorna
     * solo los primeros 5 personajes ordenados por nivel descendente.
     * 
     * @param email Email del usuario asociado a la cuenta
     * @return Lista de DTOs CharacterResponseDTO (máximo 5 personajes)
     */
    List<CharacterResponseDTO> getCharactersByEmail(String email);
}
