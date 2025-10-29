package com.ak4n1.terra.api.terra_api.game.services;

import com.ak4n1.terra.api.terra_api.game.dto.ItemCatalogDTO;
import com.ak4n1.terra.api.terra_api.game.l2j.data.ItemTable;
import com.ak4n1.terra.api.terra_api.game.l2j.model.item.ItemTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Servicio para acceder al catálogo de items del juego
 * Actúa como wrapper de ItemTable para la API REST
 */
@Service
public class ItemCatalogService {
    
    private static final Logger logger = LoggerFactory.getLogger(ItemCatalogService.class);
    
    @Autowired
    private ItemTable itemTable;
    
    /**
     * Obtiene un item por su ID
     */
    public Optional<ItemCatalogDTO> getItemById(int id) {
        ItemTemplate template = itemTable.getTemplate(id);
        if (template == null) {
            logger.debug("Item {} no encontrado", id);
            return Optional.empty();
        }
        return Optional.of(ItemCatalogDTO.fromItemTemplate(template));
    }
    
    /**
     * Obtiene todos los items del catálogo
     */
    public List<ItemCatalogDTO> getAllItems() {
        logger.debug("Obteniendo todos los items del catálogo");
        Collection<ItemTemplate> templates = itemTable.getAllItems();
        
        return templates.stream()
                .map(ItemCatalogDTO::fromItemTemplate)
                .collect(Collectors.toList());
    }
    
    /**
     * Busca items por nombre
     */
    public List<ItemCatalogDTO> searchByName(String name) {
        if (name == null || name.isBlank()) {
            logger.warn("Búsqueda con nombre vacío");
            return List.of();
        }
        
        logger.debug("Buscando items con nombre: {}", name);
        Collection<ItemTemplate> templates = itemTable.searchByName(name);
        
        return templates.stream()
                .map(ItemCatalogDTO::fromItemTemplate)
                .collect(Collectors.toList());
    }
    
    /**
     * Filtra items por tipo
     */
    public List<ItemCatalogDTO> getItemsByType(String type) {
        logger.debug("Filtrando items por tipo: {}", type);
        Collection<ItemTemplate> templates = itemTable.getAllItems();
        
        return templates.stream()
                .filter(t -> t.getItemType().equalsIgnoreCase(type))
                .map(ItemCatalogDTO::fromItemTemplate)
                .collect(Collectors.toList());
    }
    
    /**
     * Filtra items por grade
     */
    public List<ItemCatalogDTO> getItemsByGrade(String grade) {
        logger.debug("Filtrando items por grade: {}", grade);
        Collection<ItemTemplate> templates = itemTable.getAllItems();
        
        return templates.stream()
                .filter(t -> t.getGrade().equalsIgnoreCase(grade))
                .map(ItemCatalogDTO::fromItemTemplate)
                .collect(Collectors.toList());
    }
    
    /**
     * Recarga el catálogo desde los XMLs
     */
    public void reloadCatalog() {
        logger.info("Recargando catálogo de items...");
        itemTable.reload();
        logger.info("Catálogo recargado exitosamente");
    }
    
    /**
     * Obtiene estadísticas del catálogo
     */
    public CatalogStats getStats() {
        int totalItems = itemTable.getItemCount();
        Collection<ItemTemplate> all = itemTable.getAllItems();
        
        long weapons = all.stream().filter(t -> t.getItemType().equals("Weapon")).count();
        long armors = all.stream().filter(t -> t.getItemType().equals("Armor")).count();
        long etcItems = all.stream().filter(t -> t.getItemType().equals("EtcItem")).count();
        
        return new CatalogStats(totalItems, weapons, armors, etcItems);
    }
    
    /**
     * Clase interna para estadísticas del catálogo
     */
    public static class CatalogStats {
        private final int totalItems;
        private final long weapons;
        private final long armors;
        private final long etcItems;
        
        public CatalogStats(int totalItems, long weapons, long armors, long etcItems) {
            this.totalItems = totalItems;
            this.weapons = weapons;
            this.armors = armors;
            this.etcItems = etcItems;
        }
        
        public int getTotalItems() {
            return totalItems;
        }
        
        public long getWeapons() {
            return weapons;
        }
        
        public long getArmors() {
            return armors;
        }
        
        public long getEtcItems() {
            return etcItems;
        }
    }
}

