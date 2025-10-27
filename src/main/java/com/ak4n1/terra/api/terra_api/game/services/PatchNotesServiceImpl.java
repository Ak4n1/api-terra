package com.ak4n1.terra.api.terra_api.game.services;

import com.ak4n1.terra.api.terra_api.game.entities.PatchNote;
import com.ak4n1.terra.api.terra_api.game.repositories.PatchNoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service

public class PatchNotesServiceImpl implements PatchNotesService {


    @Autowired
    private PatchNoteRepository patchNoteRepository;

    @Override
    public List<PatchNote> getAllNotes() {
        return patchNoteRepository.findAll();
    }

}
