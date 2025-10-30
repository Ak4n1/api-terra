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
 * Servicio principal para acceder al catálogo de items del juego.
 * 
 * <p>Este servicio actúa como wrapper de ItemTable para la API REST, proporcionando
 * métodos para buscar, filtrar y consultar items del catálogo cargado en memoria.
 * Es el servicio RECOMENDADO para interactuar con el catálogo de items desde los controladores.
 * 
 * @see com.ak4n1.terra.api.terra_api.game.l2j.data.ItemTable
 * @see ItemCatalogDTO
 * @author ak4n1
 * @since 1.0
 */
@Service
public class ItemCatalogService {
    
    private static final Logger logger = LoggerFactory.getLogger(ItemCatalogService.class);
    
    @Autowired
    private ItemTable itemTable;
    
    /**
     * Obtiene un item del catálogo por su ID.
     * 
     * @param id ID del item a buscar
     * @return Optional con el DTO del item si existe, vacío si no se encuentra
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
     * Obtiene todos los items disponibles en el catálogo.
     * 
     * @return Lista completa de DTOs de todos los items del catálogo
     */
    public List<ItemCatalogDTO> getAllItems() {
        logger.debug("Obteniendo todos los items del catálogo");
        Collection<ItemTemplate> templates = itemTable.getAllItems();
        
        return templates.stream()
                .map(ItemCatalogDTO::fromItemTemplate)
                .collect(Collectors.toList());
    }
    
    /**
     * Busca items en el catálogo por nombre (búsqueda parcial case-insensitive).
     * 
     * @param name Nombre o fragmento del nombre a buscar
     * @return Lista de DTOs que coinciden con la búsqueda, lista vacía si el nombre es nulo o en blanco
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
     * Filtra items del catálogo por tipo de item.
     * 
     * <p>Los tipos válidos son: "Weapon", "Armor", "EtcItem"
     * 
     * @param type Tipo de item a filtrar (case-insensitive)
     * @return Lista de DTOs de items que coinciden con el tipo especificado
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
     * Filtra items del catálogo por grade de cristal.
     * 
     * <p>Los grades válidos son: "NONE", "D", "C", "B", "A", "S"
     * 
     * @param grade Grade de cristal a filtrar (case-insensitive)
     * @return Lista de DTOs de items que coinciden con el grade especificado
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
     * Recarga completamente el catálogo de items desde los archivos XML.
     * 
     * <p>Este método limpia el catálogo actual y vuelve a cargar todos los items
     * desde la ruta configurada. Útil cuando se actualizan los archivos XML.
     * 
     * @see com.ak4n1.terra.api.terra_api.game.l2j.data.ItemTable#reload()
     */
    public void reloadCatalog() {
        logger.info("Recargando catálogo de items...");
        itemTable.reload();
        logger.info("Catálogo recargado exitosamente");
    }
    
    /**
     * Obtiene estadísticas agregadas del catálogo de items.
     * 
     * <p>Las estadísticas incluyen el total de items y el desglose por tipo
     * (armas, armaduras e items misceláneos).
     * 
     * @return Objeto CatalogStats con las estadísticas del catálogo
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
     * Clase interna que contiene estadísticas agregadas del catálogo de items.
     * 
     * @since 1.0
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

