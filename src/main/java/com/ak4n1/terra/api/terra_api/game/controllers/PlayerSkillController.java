package com.ak4n1.terra.api.terra_api.game.controllers;

import com.ak4n1.terra.api.terra_api.game.dto.PlayerRequestDTO;
import com.ak4n1.terra.api.terra_api.game.dto.SkillDTO;
import com.ak4n1.terra.api.terra_api.game.services.PlayerSkillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para endpoints de skills de personajes.
 */
@RestController
@RequestMapping("/api/game/skills")
public class PlayerSkillController {

    @Autowired
    private PlayerSkillService playerSkillService;
    
    /**
     * Obtiene todos los skills de un personaje por su ID.
     * 
     * @param request DTO con el playerId del personaje
     * @return Lista de skills del personaje
     */
    @PostMapping("/character")
    public List<SkillDTO> getCharacterSkills(@RequestBody PlayerRequestDTO request) {
        return playerSkillService.getSkillsByCharacterId(request.getPlayerId());
    }
}
