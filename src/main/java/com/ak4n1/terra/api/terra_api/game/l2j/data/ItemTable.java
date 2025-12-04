package com.ak4n1.terra.api.terra_api.game.l2j.data;

import com.ak4n1.terra.api.terra_api.game.l2j.model.item.ItemTemplate;
import com.ak4n1.terra.api.terra_api.game.l2j.util.ItemXmlParser;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/**
 * Tabla de items simplificada del core L2J.
 * 
 * <p>Componente principal que carga todos los items desde archivos XML en memoria
 * al iniciar la aplicación. Proporciona búsqueda O(1) por ID mediante un array indexado
 * y búsqueda por nombre usando streams. Es la clase RECOMENDADA para acceder al catálogo
 * de items desde cualquier parte de la aplicación.
 * 
 * <p>Características:
 * <ul>
 *   <li>Carga automática al iniciar Spring Boot mediante @PostConstruct</li>
 *   <li>Búsqueda O(1) por ID usando array indexado</li>
 *   <li>Búsqueda por nombre con streams (case-insensitive)</li>
 *   <li>Recarga manual disponible</li>
 * </ul>
 * 
 * @see ItemTemplate
 * @see ItemXmlParser
 * @author ak4n1
 * @since 1.0
 */
@Component
public class ItemTable {
    
    private static final Logger logger = LoggerFactory.getLogger(ItemTable.class);
    
    @Value("${l2j.items.path:classpath:static/items}")
    private String itemsPath;
    
    private final ResourceLoader resourceLoader;
    private ItemTemplate[] _allTemplates;
    private final Map<Integer, ItemTemplate> _itemsMap = new HashMap<>();
    
    public ItemTable(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }
    
    /**
     * Inicializa y carga todos los items desde los archivos XML.
     * 
     * <p>Este método se ejecuta automáticamente al iniciar Spring Boot mediante
     * @PostConstruct. Lee todos los archivos XML de la ruta configurada y carga
     * los items en memoria.
     */
    @PostConstruct
    public void init() {
        logger.info("🔄 Iniciando carga de items desde XMLs...");
        logger.info("📂 Ruta configurada: {}", itemsPath);
        loadItems();
    }
    
    /**
     * Carga todos los items desde los archivos XML de la ruta configurada.
     * 
     * <p>Procesa todos los archivos XML encontrados en el directorio, parsea cada
     * item y los almacena en un mapa. Al final construye un array indexado para
     * búsqueda rápida por ID.
     */
    private void loadItems() {
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(resourceLoader);
            String pattern = itemsPath.startsWith("classpath:") 
                ? itemsPath.replace("classpath:", "classpath:") + "/*.xml"
                : itemsPath + "/*.xml";
            
            Resource[] resources = resolver.getResources(pattern);
            
            if (resources.length == 0) {
                logger.warn("⚠️ No se encontraron archivos XML en: {}", itemsPath);
                return;
            }
            
            logger.info("📁 Encontrados {} archivos XML para procesar", resources.length);
            
            int totalItems = 0;
            int highestId = 0;
            
            // Parsear todos los archivos
            for (Resource resource : resources) {
                try {
                    String fileName = resource.getFilename() != null ? resource.getFilename() : "unknown.xml";
                    Map<Integer, ItemTemplate> parsedItems;
                    
                    // Intentar usar File si está disponible (desarrollo)
                    try {
                        File xmlFile = resource.getFile();
                        parsedItems = ItemXmlParser.parseFile(xmlFile);
                    } catch (IOException e) {
                        // Si está dentro del JAR, usar InputStream (producción)
                        try (InputStream inputStream = resource.getInputStream()) {
                            parsedItems = ItemXmlParser.parseFile(inputStream, fileName);
                        }
                    }
                    
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
                    
                } catch (Exception e) {
                    String fileName = resource.getFilename() != null ? resource.getFilename() : "unknown.xml";
                    logger.error("❌ Error procesando archivo {}: {}", fileName, e.getMessage());
                }
            }
            
            // Construir el array indexado para búsqueda O(1)
            buildFastLookupTable(highestId);
            
            logger.info("🎉 Carga de items completada:");
            logger.info("   📊 Total items cargados: {}", totalItems);
            logger.info("   🔢 ID más alto: {}", highestId);
            logger.info("   💾 Items en memoria: {}", _itemsMap.size());
        } catch (Exception e) {
            logger.error("❌ Error al cargar items: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Construye un array indexado por ID para búsqueda O(1).
     * 
     * @param maxId ID más alto encontrado en los items cargados
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
     * Obtiene un item por su ID mediante búsqueda O(1).
     * 
     * @param id ID del item a buscar
     * @return ItemTemplate si existe, null si no se encuentra o el ID está fuera de rango
     */
    public ItemTemplate getTemplate(int id) {
        if (id < 0 || id >= _allTemplates.length) {
            return null;
        }
        return _allTemplates[id];
    }
    
    /**
     * Obtiene todos los items cargados en memoria.
     * 
     * @return Colección con todos los ItemTemplate cargados
     */
    public Collection<ItemTemplate> getAllItems() {
        return _itemsMap.values();
    }
    
    /**
     * Obtiene el número total de items cargados en memoria.
     * 
     * @return Cantidad total de items cargados
     */
    public int getItemCount() {
        return _itemsMap.size();
    }
    
    /**
     * Recarga completamente todos los items desde los archivos XML.
     * 
     * <p>Limpia el catálogo actual y vuelve a cargar todos los items desde la ruta
     * configurada. Útil cuando se actualizan los archivos XML sin reiniciar la aplicación.
     */
    public void reload() {
        logger.info("🔄 Recargando items desde XMLs...");
        _itemsMap.clear();
        _allTemplates = null;
        loadItems();
    }
    
    /**
     * Busca items por nombre usando búsqueda parcial case-insensitive.
     * 
     * <p>Recorre todos los items cargados y retorna aquellos cuyo nombre contiene
     * la cadena especificada (sin distinguir mayúsculas/minúsculas).
     * 
     * @param name Nombre o fragmento del nombre a buscar
     * @return Colección de ItemTemplate que coinciden con la búsqueda
     */
    public Collection<ItemTemplate> searchByName(String name) {
        String searchLower = name.toLowerCase();
        return _itemsMap.values().stream()
                .filter(item -> item.getName().toLowerCase().contains(searchLower))
                .toList();
    }
}

