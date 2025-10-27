package com.ak4n1.terra.api.terra_api.game.controllers;


import com.ak4n1.terra.api.terra_api.game.entities.PatchNote;
import com.ak4n1.terra.api.terra_api.game.services.PatchNotesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/game/patch-notes")
public class PatchNoteController {

    @Autowired
    private PatchNotesService patchNotesService;

    @GetMapping
    public List<PatchNote> getAllPatchNotes() {
        return patchNotesService.getAllNotes();
    }

}
