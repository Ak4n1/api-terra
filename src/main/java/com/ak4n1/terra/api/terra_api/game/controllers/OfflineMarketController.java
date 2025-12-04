package com.ak4n1.terra.api.terra_api.game.controllers;

import com.ak4n1.terra.api.terra_api.game.dto.OfflineStoreDTO;
import com.ak4n1.terra.api.terra_api.game.services.OfflineMarketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/game/offline-market")
public class OfflineMarketController {

    @Autowired
    private OfflineMarketService offlineMarketService;

    /**
     * Obtiene todas las tiendas offline sin paginación.
     * 
     * @deprecated Usar {@link #getOfflineStoresPaginated} en su lugar para mejor rendimiento.
     * @return Lista de todas las tiendas offline
     */
    @GetMapping
    @Deprecated
    public List<OfflineStoreDTO> getOfflineStores() {
        return offlineMarketService.getAllOfflineStores();
    }

    /**
     * Obtiene tiendas offline con paginación y filtros.
     * 
     * @param page Número de página (0-based, default: 0)
     * @param size Elementos por página (default: 6)
     * @param searchTerm Término de búsqueda (nombre de personaje, título, nombre de item)
     * @param sortBy Ordenamiento ("time", "price", "name", "enchant", "buy", "sell", "pack")
     * @param storeType Filtro por tipo de tienda (1=Sell, 3=Buy, 8=Pack)
     * @return Respuesta paginada con tiendas offline filtradas
     */
    @GetMapping("/paginated")
    public ResponseEntity<Map<String, Object>> getOfflineStoresPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size,
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) Integer storeType) {
        
        // Construir mapa de filtros
        Map<String, String> filters = new HashMap<>();
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            filters.put("searchTerm", searchTerm);
        }
        if (sortBy != null && !sortBy.trim().isEmpty()) {
            filters.put("sortBy", sortBy);
        }
        if (storeType != null) {
            filters.put("storeType", storeType.toString());
        }
        
        Map<String, Object> response = offlineMarketService.getOfflineStoresPaginated(page, size, filters);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public List<OfflineStoreDTO> testItems() {
        return offlineMarketService.getTest();
    }

}