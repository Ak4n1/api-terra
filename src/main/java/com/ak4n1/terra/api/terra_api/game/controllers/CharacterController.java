package com.ak4n1.terra.api.terra_api.game.controllers;

import com.ak4n1.terra.api.terra_api.game.dto.CharacterResponseDTO;
import com.ak4n1.terra.api.terra_api.game.services.CharacterService;
import com.ak4n1.terra.api.terra_api.game.services.CharacterServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ========================================
 * CONTROLADOR DE PERSONAJES - TERRA API
 * ========================================
 *
 * Este controlador maneja todas las operaciones relacionadas con personajes del juego.
 * Incluye endpoints para paginación, estadísticas y funcionalidades básicas.
 *
 * TODOS LOS ENDPOINTS REQUIEREN AUTENTICACIÓN JWT
 *
 * ========================================
 * IMPLEMENTACIÓN DE PAGINACIÓN EN ANGULAR
 * ========================================
 *
 * Para implementar la paginación en el frontend, usa estos endpoints:
 *
 * 1. PAGINACIÓN BÁSICA:
 *    GET /api/game/characters/by-email/paginated?email=xxx&page=0&size=5
 *
 *    Respuesta:
 *    {
 *      "content": [...],           // Lista de personajes
 *      "totalElements": 15,        // Total en BD
 *      "totalPages": 3,           // Páginas totales
 *      "currentPage": 0,          // Página actual (0-based)
 *      "size": 5,                // Elementos por página
 *      "hasNext": true,          // Tiene siguiente
 *      "hasPrevious": false      // Tiene anterior
 *    }
 *
 * 2. ESTADÍSTICAS:
 *    GET /api/game/characters/by-email/stats?email=xxx
 *
 *    Respuesta:
 *    {
 *      "totalCharacters": 15,
 *      "totalPages": 3,
 *      "maxLevel": 85,
 *      "averageLevel": 67.33
 *    }
 *
 * 3. INFORMACIÓN COMPLETA:
 *    GET /api/game/characters/by-email/complete?email=xxx
 *
 *    Respuesta:
 *    {
 *      "characters": [...],       // Máximo 5 personajes
 *      "stats": {...}            // Estadísticas
 *    }
 *
 * ========================================
 * EJEMPLO DE USO EN ANGULAR
 * ========================================
 *
 * // En tu CharacterService:
 * getCharactersPaginated(email: string, page: number, size: number = 5) {
 *   return this.http.get<PaginatedResponse<Character>>(
 *     `${this.apiUrl}/api/game/characters/by-email/paginated?email=${email}&page=${page}&size=${size}`
 *   );
 * }
 *
 * // En tu componente:
 * loadCharacters(page: number = 0) {
 *   this.characterService.getCharactersPaginated(this.userEmail, page, 5)
 *     .subscribe(response => {
 *       this.characters = response.content;
 *       this.currentPage = response.currentPage;
 *       this.totalPages = response.totalPages;
 *       this.hasNext = response.hasNext;
 *       this.hasPrevious = response.hasPrevious;
 *     });
 * }
 *
 * // Controles de navegación:
 * nextPage() {
 *   if (this.hasNext) {
 *     this.loadCharacters(this.currentPage + 1);
 *   }
 * }
 *
 * previousPage() {
 *   if (this.hasPrevious) {
 *     this.loadCharacters(this.currentPage - 1);
 *   }
 * }
 */
@RestController
@RequestMapping("/api/game/characters")
public class CharacterController {

    @Autowired
    private CharacterService characterService;

    @Autowired
    private CharacterServiceImpl characterServiceImpl;

    /**
     * Obtener personajes por email (máximo 5)
     *
     * USO: Para mostrar una vista rápida de los personajes principales
     * ORDEN: Por nivel descendente (más alto primero)
     *
     * @param email - Email del usuario autenticado
     * @return Lista de personajes (máximo 5)
     */
    @GetMapping("/by-email")
    public ResponseEntity<List<CharacterResponseDTO>> getCharactersByEmail(@RequestParam String email) {
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        List<CharacterResponseDTO> characters = characterService.getCharactersByEmail(email);

        if (characters.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(characters);
    }

    /**
     * Obtener personajes con paginación completa
     *
     * USO: Para implementar paginación en el frontend
     * ORDEN: Por nivel descendente (más alto primero)
     *
     * @param email - Email del usuario autenticado
     * @param page - Número de página (0-based, default: 0)
     * @param size - Elementos por página (default: 5)
     * @return Respuesta paginada con metadatos
     */
    @GetMapping("/by-email/paginated")
    public ResponseEntity<Map<String, Object>> getCharactersByEmailPaginated(
            @RequestParam String email,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            Page<CharacterResponseDTO> characterPage = characterServiceImpl.getCharactersByEmailWithPagination(email, page, size);

            Map<String, Object> response = new HashMap<>();
            response.put("content", characterPage.getContent());
            response.put("totalElements", characterPage.getTotalElements());
            response.put("totalPages", characterPage.getTotalPages());
            response.put("currentPage", characterPage.getNumber());
            response.put("size", characterPage.getSize());
            response.put("hasNext", characterPage.hasNext());
            response.put("hasPrevious", characterPage.hasPrevious());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtener estadísticas de personajes
     *
     * USO: Para mostrar resumen o dashboard del usuario
     *
     * @param email - Email del usuario autenticado
     * @return Estadísticas calculadas
     */
    @GetMapping("/by-email/stats")
    public ResponseEntity<CharacterServiceImpl.CharacterStats> getCharacterStatsByEmail(@RequestParam String email) {
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            CharacterServiceImpl.CharacterStats stats = characterServiceImpl.getCharacterStatsByEmail(email);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtener personajes + estadísticas en una sola llamada
     *
     * USO: Para cargar información completa de una vez
     *
     * @param email - Email del usuario autenticado
     * @return Personajes y estadísticas combinados
     */
    @GetMapping("/by-email/complete")
    public ResponseEntity<Map<String, Object>> getCharactersComplete(@RequestParam String email) {
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            List<CharacterResponseDTO> characters = characterService.getCharactersByEmail(email);
            CharacterServiceImpl.CharacterStats stats = characterServiceImpl.getCharacterStatsByEmail(email);

            Map<String, Object> response = new HashMap<>();
            response.put("characters", characters);
            response.put("stats", stats);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}