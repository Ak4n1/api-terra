package com.ak4n1.terra.api.terra_api.game.l2j.data;

import com.ak4n1.terra.api.terra_api.game.l2j.model.item.ItemTemplate;
import com.ak4n1.terra.api.terra_api.game.l2j.util.ItemXmlParser;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Tabla de items simplificada del core L2J
 * Carga todos los items en memoria al iniciar la aplicación
 */
@Component
public class ItemTable {
    
    private static final Logger logger = LoggerFactory.getLogger(ItemTable.class);
    
    @Value("${l2j.items.path:D:/Terra/L2J_Mobius_Classic_3.0_TheKamael/dist/game/data/stats/items}")
    private String itemsPath;
    
    private ItemTemplate[] _allTemplates;
    private final Map<Integer, ItemTemplate> _itemsMap = new HashMap<>();
    
    /**
     * Carga automática al iniciar Spring Boot
     */
    @PostConstruct
    public void init() {
        logger.info("🔄 Iniciando carga de items desde XMLs...");
        logger.info("📂 Ruta configurada: {}", itemsPath);
        loadItems();
    }
    
    /**
     * Carga todos los items desde los archivos XML
     */
    private void loadItems() {
        File itemsDir = new File(itemsPath);
        
        if (!itemsDir.exists() || !itemsDir.isDirectory()) {
            logger.error("❌ La ruta de items no existe o no es un directorio: {}", itemsPath);
            return;
        }
        
        File[] xmlFiles = itemsDir.listFiles((dir, name) -> name.endsWith(".xml"));
        
        if (xmlFiles == null || xmlFiles.length == 0) {
            logger.warn("⚠️ No se encontraron archivos XML en: {}", itemsPath);
            return;
        }
        
        logger.info("📁 Encontrados {} archivos XML para procesar", xmlFiles.length);
        
        int totalItems = 0;
        int highestId = 0;
        
        // Parsear todos los archivos
        for (File xmlFile : xmlFiles) {
            try {
                Map<Integer, ItemTemplate> parsedItems = ItemXmlParser.parseFile(xmlFile);
                
                for (Map.Entry<Integer, ItemTemplate> entry : parsedItems.entrySet()) {
                    int itemId = entry.getKey();
                    ItemTemplate item = entry.getValue();
                    
                    // Guardar en el mapa
                    _itemsMap.put(itemId, item);
                    
                    // Actualizar el ID más alto
                    if (itemId > highestId) {
                        highestId = itemId;
                    }
                }
                
                totalItems += parsedItems.size();
                logger.debug("✅ Archivo {} procesado: {} items", xmlFile.getName(), parsedItems.size());
                
            } catch (Exception e) {
                logger.error("❌ Error procesando archivo {}: {}", xmlFile.getName(), e.getMessage());
            }
        }
        
        // Construir el array indexado para búsqueda O(1)
        buildFastLookupTable(highestId);
        
        logger.info("🎉 Carga de items completada:");
        logger.info("   📊 Total items cargados: {}", totalItems);
        logger.info("   🔢 ID más alto: {}", highestId);
        logger.info("   💾 Items en memoria: {}", _itemsMap.size());
    }
    
    /**
     * Construye un array indexado por ID para búsqueda O(1)
     */
    private void buildFastLookupTable(int maxId) {
        logger.info("🗄️ Construyendo tabla de búsqueda rápida (tamaño: {})", maxId + 1);
        
        _allTemplates = new ItemTemplate[maxId + 1];
        
        for (Map.Entry<Integer, ItemTemplate> entry : _itemsMap.entrySet()) {
            _allTemplates[entry.getKey()] = entry.getValue();
        }
        
        logger.info("✅ Tabla de búsqueda construida");
    }
    
    /**
     * Obtiene un item por su ID - Búsqueda O(1)
     * 
     * @param id ID del item
     * @return ItemTemplate o null si no existe
     */
    public ItemTemplate getTemplate(int id) {
        if (id < 0 || id >= _allTemplates.length) {
            return null;
        }
        return _allTemplates[id];
    }
    
    /**
     * Obtiene todos los items
     */
    public Collection<ItemTemplate> getAllItems() {
        return _itemsMap.values();
    }
    
    /**
     * Obtiene el número total de items cargados
     */
    public int getItemCount() {
        return _itemsMap.size();
    }
    
    /**
     * Recarga todos los items desde los XMLs
     */
    public void reload() {
        logger.info("🔄 Recargando items desde XMLs...");
        _itemsMap.clear();
        _allTemplates = null;
        loadItems();
    }
    
    /**
     * Busca items por nombre (búsqueda parcial case-insensitive)
     */
    public Collection<ItemTemplate> searchByName(String name) {
        String searchLower = name.toLowerCase();
        return _itemsMap.values().stream()
                .filter(item -> item.getName().toLowerCase().contains(searchLower))
                .toList();
    }
}

