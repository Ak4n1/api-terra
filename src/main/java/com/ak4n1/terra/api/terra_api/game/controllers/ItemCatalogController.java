package com.ak4n1.terra.api.terra_api.game.controllers;

import com.ak4n1.terra.api.terra_api.game.dto.ItemCatalogDTO;
import com.ak4n1.terra.api.terra_api.game.services.ItemCatalogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ========================================
 * CONTROLADOR DE CATÁLOGO DE ITEMS - TERRA API
 * ========================================
 *
 * Este controlador maneja el catálogo de items del juego (NO inventarios de jugadores).
 * Los datos se cargan desde los XMLs del servidor L2J en memoria.
 *
 * ========================================
 * ENDPOINTS DISPONIBLES
 * ========================================
 *
 * 1. OBTENER ITEM POR ID:
 *    GET /api/game/catalog/items/{id}
 *    Ejemplo: GET /api/game/catalog/items/69  (Bastard Sword)
 *
 * 2. LISTAR TODOS LOS ITEMS:
 *    GET /api/game/catalog/items
 *    Retorna: Lista completa de items del catálogo
 *
 * 3. BUSCAR POR NOMBRE:
 *    GET /api/game/catalog/items/search?name=sword
 *    Búsqueda case-insensitive parcial
 *
 * 4. FILTRAR POR TIPO:
 *    GET /api/game/catalog/items/filter/type?type=Weapon
 *    Tipos: "Weapon", "Armor", "EtcItem"
 *
 * 5. FILTRAR POR GRADE:
 *    GET /api/game/catalog/items/filter/grade?grade=D
 *    Grades: "NONE", "D", "C", "B", "A", "S"
 *
 * 6. ESTADÍSTICAS DEL CATÁLOGO:
 *    GET /api/game/catalog/items/stats
 *    Retorna: Total de items, weapons, armors, etc.
 *
 * 7. RECARGAR CATÁLOGO (ADMIN):
 *    POST /api/game/catalog/items/admin/reload
 *    Recarga los XMLs sin reiniciar la API
 *
 * ========================================
 * RESPUESTA TÍPICA (ItemCatalogDTO)
 * ========================================
 *
 * {
 *   "id": 69,
 *   "name": "Bastard Sword",
 *   "icon": "icon.weapon_bastard_sword_i00",
 *   "type": "Weapon",
 *   "subType": "SWORD",
 *   "grade": "D",
 *   "weight": 1510,
 *   "price": 644000,
 *   "stackable": false,
 *   "sellable": true,
 *   "tradeable": true,
 *   "bodyPart": "rhand",
 *   "pAtk": 51,
 *   "mAtk": 32,
 *   "soulshots": 1,
 *   "spiritshots": 1
 * }
 */
@RestController
@RequestMapping("/api/game/catalog/items")
public class ItemCatalogController {
    
    @Autowired
    private ItemCatalogService catalogService;
    
    /**
     * Obtener item por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ItemCatalogDTO> getItemById(@PathVariable int id) {
        return catalogService.getItemById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Listar todos los items del catálogo
     * ADVERTENCIA: Puede ser una respuesta grande (miles de items)
     */
    @GetMapping
    public ResponseEntity<List<ItemCatalogDTO>> getAllItems() {
        List<ItemCatalogDTO> items = catalogService.getAllItems();
        return ResponseEntity.ok(items);
    }
    
    /**
     * Buscar items por nombre (case-insensitive, búsqueda parcial)
     */
    @GetMapping("/search")
    public ResponseEntity<List<ItemCatalogDTO>> searchItems(@RequestParam String name) {
        if (name == null || name.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        
        List<ItemCatalogDTO> items = catalogService.searchByName(name);
        return ResponseEntity.ok(items);
    }
    
    /**
     * Filtrar items por tipo
     * Tipos válidos: "Weapon", "Armor", "EtcItem"
     */
    @GetMapping("/filter/type")
    public ResponseEntity<List<ItemCatalogDTO>> getItemsByType(@RequestParam String type) {
        if (type == null || type.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        
        List<ItemCatalogDTO> items = catalogService.getItemsByType(type);
        return ResponseEntity.ok(items);
    }
    
    /**
     * Filtrar items por grade
     * Grades válidos: "NONE", "D", "C", "B", "A", "S"
     */
    @GetMapping("/filter/grade")
    public ResponseEntity<List<ItemCatalogDTO>> getItemsByGrade(@RequestParam String grade) {
        if (grade == null || grade.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        
        List<ItemCatalogDTO> items = catalogService.getItemsByGrade(grade);
        return ResponseEntity.ok(items);
    }
    
    /**
     * Obtener estadísticas del catálogo
     */
    @GetMapping("/stats")
    public ResponseEntity<ItemCatalogService.CatalogStats> getStats() {
        ItemCatalogService.CatalogStats stats = catalogService.getStats();
        return ResponseEntity.ok(stats);
    }
    
    /**
     * Recargar catálogo desde XMLs
     * Requiere autenticación de administrador (implementar si es necesario)
     */
    @PostMapping("/admin/reload")
    public ResponseEntity<Map<String, Object>> reloadCatalog() {
        try {
            catalogService.reloadCatalog();
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Catálogo recargado exitosamente");
            response.put("totalItems", catalogService.getStats().getTotalItems());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Error recargando catálogo: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
}

