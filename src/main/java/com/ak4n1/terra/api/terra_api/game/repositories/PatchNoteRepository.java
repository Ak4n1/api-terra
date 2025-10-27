package com.ak4n1.terra.api.terra_api.game.repositories;

import com.ak4n1.terra.api.terra_api.game.entities.PatchNote;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatchNoteRepository extends JpaRepository<PatchNote, Long> {
}
