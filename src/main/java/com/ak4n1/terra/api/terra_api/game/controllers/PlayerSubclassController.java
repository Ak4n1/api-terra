package com.ak4n1.terra.api.terra_api.game.controllers;

import com.ak4n1.terra.api.terra_api.game.dto.PlayerRequestDTO;
import com.ak4n1.terra.api.terra_api.game.dto.SubclassDTO;
import com.ak4n1.terra.api.terra_api.game.services.PlayerSubclassService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST para gestionar subclases de personajes.
 * 
 * <p>Proporciona endpoints para obtener información de subclases de personajes.
 * Requiere autenticación JWT.
 * 
 * @author ak4n1
 * @since 1.0
 */
@RestController
@RequestMapping("/api/game")
public class PlayerSubclassController {
    
    @Autowired
    private PlayerSubclassService playerSubclassService;
    
    /**
     * Obtiene todas las subclases de un personaje por su ID.
     * 
     * @param request DTO con el ID del personaje (playerId)
     * @return Lista de SubclassDTO con las subclases del personaje
     */
    @PostMapping("/subclasses/character")
    public List<SubclassDTO> getCharacterSubclasses(@RequestBody PlayerRequestDTO request) {
        return playerSubclassService.getSubclassesByCharacterId(request.getPlayerId());
    }
    
    /**
     * Endpoint de prueba para obtener subclases de un personaje específico.
     * 
     * @return Lista de SubclassDTO con las subclases del personaje de prueba
     */
    @GetMapping("/subclasses/character")
    public List<SubclassDTO> getTest() {
        return playerSubclassService.getSubclassesByCharacterId(29520);
    }
}

