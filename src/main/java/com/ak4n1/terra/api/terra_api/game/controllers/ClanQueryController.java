package com.ak4n1.terra.api.terra_api.game.controllers;

import com.ak4n1.terra.api.terra_api.game.services.ClanQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Controlador para consultar información de clanes usando consultas SQL nativas.
 * 
 * <p>Este controlador no usa entidades JPA para evitar conflictos con la estructura
 * de la tabla clan_data del core del juego. Usa JdbcTemplate para consultas directas.
 */
@RestController
@RequestMapping("/api/game/clan")
public class ClanQueryController {

    @Autowired
    private ClanQueryService clanQueryService;

    /**
     * Obtiene información completa de un clan por su ID.
     * Compatible con el endpoint anterior POST /api/game/clan/by-id
     * 
     * @param request Mapa con el clanId
     * @return Información del clan o 404 si no existe
     */
    @PostMapping("/by-id")
    public ResponseEntity<Map<String, Object>> getClanById(@RequestBody Map<String, Integer> request) {
        Integer clanId = request.get("clanId");
        
        if (clanId == null || clanId <= 0) {
            return ResponseEntity.badRequest().build();
        }
        
        Optional<Map<String, Object>> clan = clanQueryService.getClanById(clanId);
        
        if (clan.isPresent()) {
            return ResponseEntity.ok(clan.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Obtiene información completa de un clan por su ID (nuevo endpoint con GET).
     * 
     * @param clanId ID del clan
     * @return Información del clan o 404 si no existe
     */
    @GetMapping("/{clanId}")
    public ResponseEntity<Map<String, Object>> getClanByIdGet(@PathVariable Integer clanId) {
        Optional<Map<String, Object>> clan = clanQueryService.getClanById(clanId);
        
        if (clan.isPresent()) {
            return ResponseEntity.ok(clan.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Obtiene solo el nombre de un clan por su ID.
     * 
     * @param clanId ID del clan
     * @return Nombre del clan o 404 si no existe
     */
    @GetMapping("/{clanId}/name")
    public ResponseEntity<Map<String, String>> getClanName(@PathVariable Integer clanId) {
        String clanName = clanQueryService.getClanName(clanId);
        
        if (clanName != null) {
            Map<String, String> response = new HashMap<>();
            response.put("clanName", clanName);
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}

