package com.ak4n1.terra.api.terra_api.game.services;

import com.ak4n1.terra.api.terra_api.game.entities.PatchNote;
import com.ak4n1.terra.api.terra_api.game.repositories.PatchNoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
/**
 * Implementación del servicio de notas de parche.
 * 
 * <p>Este servicio proporciona acceso a todas las notas de parche publicadas,
 * permitiendo a los usuarios conocer los cambios y actualizaciones del juego.
 * 
 * @see PatchNotesService
 * @see PatchNoteRepository
 * @author ak4n1
 * @since 1.0
 */
@Service
public class PatchNotesServiceImpl implements PatchNotesService {


    @Autowired
    private PatchNoteRepository patchNoteRepository;

    /**
     * {@inheritDoc}
     * 
     * @return Lista de entidades PatchNote con toda la información de las notas de parche
     */
    @Override
    public List<PatchNote> getAllNotes() {
        return patchNoteRepository.findAll();
    }

}
