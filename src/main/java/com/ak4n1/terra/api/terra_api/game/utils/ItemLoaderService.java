package com.ak4n1.terra.api.terra_api.game.utils;

import com.ak4n1.terra.api.terra_api.game.entities.ItemXmlEntity;
import com.ak4n1.terra.api.terra_api.game.repositories.ItemXmlRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Servicio que permite cargar archivos XML de items del juego de forma dinámica,
 * indexarlos por rangos de IDs, cachear resultados, y exponer métodos para recuperar items por ID.
 * Además, guarda cada item en la base de datos en la tabla item_xml
 */
@Service
public class ItemLoaderService {
    private static final Logger logger = LoggerFactory.getLogger(ItemLoaderService.class);

    @Value("${terra.items.path}")
    private String itemFolderPath;

    @Autowired
    private ItemXmlRepository itemXmlRepository;

    private final Map<Integer, ItemData> cache = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Optional<ItemData> getItemById(int id) {
        return Optional.ofNullable(cache.get(id));
    }

    public String getItemFolderPath() {
        return itemFolderPath;
    }

    public void clearCache() {
        cache.clear();
        logger.info("🗑️ Cache de items limpiado");
    }

    public void debugItemLoader() {
        logger.info("📊 Items cargados en cache: {}", cache.size());
    }

    public Map<String, Object> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("cachedItems", cache.size());
        stats.put("cacheSize", cache.size());
        stats.put("cacheKeys", new ArrayList<>(cache.keySet()));
        return stats;
    }

    // Buscar item en los XML directamente (sin cache ni BD)
    public Optional<ItemData> searchItemInXmlFiles(int itemId) {
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            String pattern = itemFolderPath + "*.xml";
            Resource[] resources = resolver.getResources(pattern);

            logger.info("🔍 Buscando item {} en {} archivos XML", itemId, resources.length);

            for (Resource resource : resources) {
                File file = resource.getFile();
                Map<Integer, ItemData> parsed = ItemXmlParser.parse(file);
                if (parsed.containsKey(itemId)) {
                    ItemData found = parsed.get(itemId);
                    
                    // Verificar si el item tiene información útil
                    if (found.getName().isEmpty() && found.getType().isEmpty() && found.getAttributes().isEmpty()) {
                        logger.warn("⚠️ Item {} encontrado en archivo {}, pero sin información útil (name, type, attrs)", itemId, file.getName());
                        // Continuar buscando en otros archivos por una versión más completa
                        continue;
                    } else {
                        logger.info("🔎 Item {} encontrado en archivo: {}", itemId, file.getName());
                        logger.info("📦 Contenido: {}", found);
                        return Optional.of(found);
                    }
                }
            }
            
            logger.warn("❌ Item {} no encontrado en ningún archivo XML", itemId);
            
        } catch (IOException e) {
            logger.error("❌ Error buscando item {} en XMLs: {}", itemId, e.getMessage());
        }

        return Optional.empty();
    }

    // Método para forzar la recarga de un item específico desde XML
    public boolean forceReloadItemFromXml(int itemId) {
        logger.info("🔄 Forzando recarga del item {} desde XML", itemId);
        
        Optional<ItemData> itemOpt = searchItemInXmlFiles(itemId);

        if (itemOpt.isEmpty()) {
            logger.warn("❌ No se pudo recargar item {} desde XML (no encontrado)", itemId);
            return false;
        }

        ItemData item = itemOpt.get();
        logger.info("📦 Item {} encontrado: name='{}', type='{}', attrs={}", 
            itemId, item.getName(), item.getType(), item.getAttributes());
        
        ItemXmlEntity entity = new ItemXmlEntity();
        entity.setId(item.getId());
        entity.setName(item.getName());
        entity.setType(item.getType());
        
        try {
            entity.setAttributes(objectMapper.writeValueAsString(item.getAttributes()));
            entity.setStats(objectMapper.writeValueAsString(item.getStats()));
            logger.info("💾 Atributos JSON para item {}: {}", itemId, entity.getAttributes());
        } catch (JsonProcessingException e) {
            logger.error("❌ Error serializando JSON para item {}: {}", itemId, e.getMessage());
            entity.setAttributes(item.getAttributes().toString());
            entity.setStats(item.getStats().toString());
        }

        try {
            itemXmlRepository.save(entity);
            logger.info("✅ Item {} insertado/reinsertado correctamente en la base", itemId);
            
            // Actualizar también el cache
            cache.put(itemId, item);
            logger.info("🗄️ Cache actualizado para item {}", itemId);
            
            return true;
        } catch (Exception e) {
            logger.error("❌ Error guardando item {} en la base: {}", itemId, e.getMessage());
            return false;
        }
    }

    // Reinsertar en la BD desde el XML
    public boolean reloadItemFromXml(int itemId) {
        Optional<ItemData> itemOpt = searchItemInXmlFiles(itemId);

        if (itemOpt.isEmpty()) {
            logger.warn("❌ No se pudo recargar item {} desde XML (no encontrado)", itemId);
            return false;
        }

        ItemData item = itemOpt.get();
        ItemXmlEntity entity = new ItemXmlEntity();
        entity.setId(item.getId());
        entity.setName(item.getName());
        entity.setType(item.getType());
        
        try {
            entity.setAttributes(objectMapper.writeValueAsString(item.getAttributes()));
            entity.setStats(objectMapper.writeValueAsString(item.getStats()));
        } catch (JsonProcessingException e) {
            logger.error("❌ Error serializando JSON para item {}: {}", itemId, e.getMessage());
            entity.setAttributes(item.getAttributes().toString());
            entity.setStats(item.getStats().toString());
        }

        try {
            itemXmlRepository.save(entity);
            logger.info("✅ Item {} insertado/reinsertado correctamente en la base", itemId);
            return true;
        } catch (Exception e) {
            logger.error("❌ Error guardando item {} en la base: {}", itemId, e.getMessage());
            return false;
        }
    }

    // Método para recargar todos los items desde XML (útil para actualizaciones)
    public void reloadAllItemsFromXml() {
        logger.info("🔄 Iniciando recarga completa de items desde XML...");
        clearCache();
        
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            String pattern = itemFolderPath + "*.xml";
            Resource[] resources = resolver.getResources(pattern);

            logger.info("📁 Encontrados {} archivos XML para procesar", resources.length);

            int totalItems = 0;
            int savedItems = 0;
            
            // Procesar todos los archivos y acumular en cache
            for (Resource resource : resources) {
                try {
                    File file = resource.getFile();
                    logger.info("🔄 Procesando archivo: {}", file.getName());
                    
                    Map<Integer, ItemData> parsedItems = ItemXmlParser.parse(file);
                    
                    // Log específico para el item 29520 en cada archivo
                    if (parsedItems.containsKey(29520)) {
                        ItemData item29520 = parsedItems.get(29520);
                        logger.info("🔍 DEBUG 29520 en archivo {}: name='{}', type='{}', attrs={}", 
                            file.getName(), item29520.getName(), item29520.getType(), item29520.getAttributes());
                    }
                    
                    cache.putAll(parsedItems);
                    totalItems += parsedItems.size();
                    logger.info("✅ Archivo {} procesado: {} items parseados", file.getName(), parsedItems.size());

                } catch (IOException e) {
                    logger.error("❌ Error cargando archivo {}: {}", resource.getFilename(), e.getMessage());
                }
            }

            // Log del estado final del cache para el item 29520
            if (cache.containsKey(29520)) {
                ItemData finalItem29520 = cache.get(29520);
                logger.info("🎯 DEBUG 29520 FINAL en cache: name='{}', type='{}', attrs={}", 
                    finalItem29520.getName(), finalItem29520.getType(), finalItem29520.getAttributes());
            } else {
                logger.warn("⚠️ Item 29520 no encontrado en cache después de procesar todos los archivos");
            }

            // Ahora guardar todos los items del cache a la BD
            logger.info("💾 Guardando {} items del cache a la base de datos...", cache.size());
            
            for (ItemData item : cache.values()) {
                try {
                    // Log específico para debugging del item 29520
                    if (item.getId() == 29520) {
                        logger.info("🔍 DEBUG 29520 - Guardando a BD: name='{}', type='{}', attrs={}", 
                            item.getName(), item.getType(), item.getAttributes());
                    }

                    // Verificar si el item ya existe en la BD
                    boolean exists = itemXmlRepository.existsById(item.getId());
                    
                    ItemXmlEntity entity;
                    if (exists) {
                        // Si existe, actualizar en lugar de saltar
                        entity = itemXmlRepository.findById(item.getId()).orElse(new ItemXmlEntity());
                        logger.debug("🔄 Actualizando item existente: {}", item.getId());
                    } else {
                        entity = new ItemXmlEntity();
                        logger.debug("➕ Creando nuevo item: {}", item.getId());
                    }

                    entity.setId(item.getId());
                    entity.setName(item.getName());
                    entity.setType(item.getType());
                    
                    // Log detallado para items con atributos vacíos
                    if (item.getAttributes().isEmpty() && !item.getName().isEmpty()) {
                        logger.warn("⚠️ Item {} tiene nombre '{}' pero atributos vacíos", item.getId(), item.getName());
                    }
                    
                    // Serializar maps a JSON en lugar de toString()
                    try {
                        entity.setAttributes(objectMapper.writeValueAsString(item.getAttributes()));
                        entity.setStats(objectMapper.writeValueAsString(item.getStats()));
                        
                        // Log específico para el item 29520
                        if (item.getId() == 29520) {
                            logger.info("💾 DEBUG 29520 - Guardando atributos JSON: {}", entity.getAttributes());
                        }
                    } catch (JsonProcessingException e) {
                        logger.error("❌ Error serializando JSON para item {}: {}", item.getId(), e.getMessage());
                        // Fallback a toString() si falla JSON
                        entity.setAttributes(item.getAttributes().toString());
                        entity.setStats(item.getStats().toString());
                    }
                    
                    itemXmlRepository.save(entity);
                    savedItems++;
                    
                    // Log específico para el item 29520
                    if (item.getId() == 29520) {
                        logger.info("✅ DEBUG 29520 - Item guardado exitosamente en BD");
                    }
                    
                } catch (Exception ex) {
                    logger.error("❌ No se pudo guardar item {}: {}", item.getId(), ex.getMessage());
                }
            }

            logger.info("🎉 Recarga completa finalizada:");
            logger.info("   📊 Total items parseados: {}", totalItems);
            logger.info("   💾 Items guardados en BD: {}", savedItems);
            logger.info("   🗄️ Items en cache: {}", cache.size());
            
        } catch (Exception e) {
            logger.error("❌ Error en recarga completa: {}", e.getMessage());
        }
    }

    // Método para obtener estadísticas detalladas
    public Map<String, Object> getDetailedStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("cacheSize", cache.size());
        stats.put("databaseCount", itemXmlRepository.count());
        
        // Contar por tipos
        Map<String, Long> typeCounts = new HashMap<>();
        cache.values().stream()
            .map(ItemData::getType)
            .forEach(type -> typeCounts.merge(type, 1L, Long::sum));
        stats.put("typeDistribution", typeCounts);
        
        return stats;
    }
}
