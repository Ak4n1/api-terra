package com.ak4n1.terra.api.terra_api.game.services;

import com.ak4n1.terra.api.terra_api.game.entities.PatchNote;

import java.util.List;

/**
 * Servicio principal para la gestión de notas de parche del juego.
 * 
 * <p>Este servicio proporciona acceso a todas las notas de parche publicadas,
 * permitiendo a los usuarios conocer los cambios y actualizaciones del juego.
 * Es el servicio RECOMENDADO para consultar las notas de parche desde los controladores.
 * 
 * @see PatchNotesServiceImpl
 * @see PatchNote
 * @author ak4n1
 * @since 1.0
 */
public interface PatchNotesService {
    
    /**
     * Obtiene todas las notas de parche disponibles.
     * 
     * @return Lista de entidades PatchNote con toda la información de las notas de parche
     */
    List<PatchNote> getAllNotes();

}
