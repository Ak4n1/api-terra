package com.ak4n1.terra.api.terra_api.game.services;

import com.ak4n1.terra.api.terra_api.game.dto.ItemDTO;
import com.ak4n1.terra.api.terra_api.game.entities.Character;
import com.ak4n1.terra.api.terra_api.game.entities.Item;
import com.ak4n1.terra.api.terra_api.game.entities.ItemXmlEntity;
import com.ak4n1.terra.api.terra_api.game.repositories.CharacterRepository;
import com.ak4n1.terra.api.terra_api.game.repositories.ItemRepository;
import com.ak4n1.terra.api.terra_api.game.repositories.ItemXmlRepository;
import com.ak4n1.terra.api.terra_api.game.utils.ItemData;
import com.ak4n1.terra.api.terra_api.game.utils.ItemLoaderService;
import com.ak4n1.terra.api.terra_api.game.utils.ItemXmlParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.File;
import java.io.IOException;

@Service
public class PlayerStorageServiceImpl implements PlayerStorageService {
    private static final Logger logger = LoggerFactory.getLogger(PlayerStorageServiceImpl.class);

    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private ItemXmlRepository itemXmlRepo;

    @Autowired
    private CharacterRepository characterRepository;

    @Autowired
    private ItemLoaderService loaderItemService;



    @Override
    public List<ItemDTO> getItemsByPlayerId(int playerId) {
        List<Item> items = itemRepository.findByOwnerId(playerId);
        List<ItemDTO> itemDTOList = new ArrayList<>();

        for (Item item : items) {
            Optional<ItemXmlEntity> itemXmlOpt = itemXmlRepo.findById(item.getItemId());

            String name = "Unknown";
            String type = "Unknown";
            String rawAttributes = "";
            String rawStats = "";
            String playerName = "";
            if (itemXmlOpt.isPresent()) {
                ItemXmlEntity xml = itemXmlOpt.get();
                name = xml.getName();
                type = xml.getType();
                rawAttributes = xml.getAttributes();
                rawStats = xml.getStats();
            }

            Optional<Character> character = characterRepository.findById(item.getOwnerId());
            if (character.isPresent()) {
                playerName = character.get().getCharName();
            }

            ItemDTO dto = new ItemDTO();
            dto.setObjectId(item.getObjectId());
            dto.setItemId(item.getItemId());
            dto.setPlayer(playerName);
            dto.setCount(item.getCount());
            dto.setEnchantLevel(item.getEnchantLevel() != null ? item.getEnchantLevel() : 0);
            dto.setLocation(item.getLoc());
            dto.setLocationData(item.getLocData() != null ? item.getLocData() : 0);
            dto.setName(name);
            dto.setType(type);
            dto.setRawAttributes(rawAttributes);  // CORRECTO: usar setRawAttributes
            dto.setRawStats(rawStats);            // CORRECTO: usar setRawStats

            itemDTOList.add(dto);
        }


        return itemDTOList;
    }



    @Override
    public List<ItemDTO> getTest(int itemId) {
        logger.info("🚀 Iniciando script de carga completa de items XML a la base de datos...");
        logger.info("📝 Nota: El parámetro itemId ({}) se ignora, se ejecuta carga completa", itemId);
        
        List<ItemDTO> itemDTOList = new ArrayList<>();
        
        try {
            // Usar la lógica que SÍ funciona para todos los items, pero solo guardar la mejor versión
            logger.info("🔍 Buscando y guardando solo la mejor versión de cada item desde XML...");
            
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            String pattern = loaderItemService.getItemFolderPath() + "*.xml";
            Resource[] resources = resolver.getResources(pattern);
            
            logger.info("📁 Encontrados {} archivos XML para procesar", resources.length);

            int totalItems = 0;
            int savedItems = 0;
            
            // 1. Acumular la mejor versión de cada item
            Map<Integer, ItemData> bestItems = new HashMap<>();
            for (Resource resource : resources) {
                try {
                    File file = resource.getFile();
                    logger.info("🔄 Procesando archivo: {}", file.getName());
                    
                    Map<Integer, ItemData> parsedItems = ItemXmlParser.parse(file);
                    
                    for (ItemData item : parsedItems.values()) {
                        ItemData existing = bestItems.get(item.getId());
                        // Si no existe, o el nuevo tiene más info, reemplaza
                        if (existing == null
                            || (existing.getName().isEmpty() && !item.getName().isEmpty())
                            || (existing.getType().isEmpty() && !item.getType().isEmpty())) {
                            bestItems.put(item.getId(), item);
                        }
                    }
                    
                    totalItems += parsedItems.size();
                    logger.info("✅ Archivo {} procesado: {} items parseados", file.getName(), parsedItems.size());
                } catch (IOException e) {
                    logger.error("❌ Error cargando archivo {}: {}", resource.getFilename(), e.getMessage());
                }
            }
            
            logger.info("💾 Guardando {} items únicos (mejor versión) a la base de datos...", bestItems.size());
            
            // 2. Guardar solo la mejor versión de cada item
            ObjectMapper objectMapper = new ObjectMapper();
            for (ItemData item : bestItems.values()) {
                try {
                    // Log específico para debugging del item 29520
                    if (item.getId() == 29520) {
                        logger.info("🔍 DEBUG 29520 - Guardando a BD: name='{}', type='{}', attrs={}", 
                            item.getName(), item.getType(), item.getAttributes());
                    }

                    // Verificar si el item ya existe en la BD
                    boolean exists = itemXmlRepo.existsById(item.getId());
                    
                    ItemXmlEntity entity;
                    if (exists) {
                        // Si existe, actualizar en lugar de saltar
                        entity = itemXmlRepo.findById(item.getId()).orElse(new ItemXmlEntity());
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
                    
                    // Serializar maps a JSON usando ObjectMapper
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
                    
                    itemXmlRepo.save(entity);
                    savedItems++;
                    
                    // Log específico para el item 29520
                    if (item.getId() == 29520) {
                        logger.info("✅ DEBUG 29520 - Item guardado exitosamente en BD");
                    }
                    
                } catch (Exception ex) {
                    logger.error("❌ No se pudo guardar item {}: {}", item.getId(), ex.getMessage());
                }
            }
            
            // Obtener estadísticas después de la carga
            Map<String, Object> stats = new HashMap<>();
            stats.put("cacheSize", bestItems.size());
            stats.put("databaseCount", itemXmlRepo.count());
            
            // Verificar específicamente el item 29520 en la BD después de guardar
            Optional<ItemXmlEntity> item29520InDb = itemXmlRepo.findById(29520);
            if (item29520InDb.isPresent()) {
                ItemXmlEntity xmlItem = item29520InDb.get();
                logger.info("🎯 DEBUG 29520 en BD después de guardar: name='{}', type='{}', attrs='{}'", 
                    xmlItem.getName(), xmlItem.getType(), xmlItem.getAttributes());
            } else {
                logger.warn("⚠️ Item 29520 no encontrado en BD después de guardar");
            }
            
            // Crear un DTO de respuesta con las estadísticas
            ItemDTO responseDto = new ItemDTO();
            responseDto.setItemId(0); // ID especial para indicar que es una respuesta de script
            responseDto.setName("Script de Carga XML Completado");
            responseDto.setType("SYSTEM_RESPONSE");
            responseDto.setPlayer("SYSTEM");
            responseDto.setCount(0);
            responseDto.setEnchantLevel(0);
            responseDto.setLocation("COMPLETED");
            responseDto.setLocationData(0);
            
            // Convertir estadísticas a string para mostrar en rawAttributes
            StringBuilder statsBuilder = new StringBuilder();
            statsBuilder.append("📊 ESTADÍSTICAS DE CARGA:\n");
            statsBuilder.append("• Items únicos procesados: ").append(bestItems.size()).append("\n");
            statsBuilder.append("• Items guardados en BD: ").append(savedItems).append("\n");
            statsBuilder.append("• Total items en BD: ").append(stats.get("databaseCount")).append("\n");
            
            responseDto.setRawAttributes(statsBuilder.toString());
            responseDto.setRawStats("Script ejecutado exitosamente");
            
            itemDTOList.add(responseDto);
            
            logger.info("✅ Script de carga completado exitosamente");
            logger.info("📊 Estadísticas finales: {} items únicos procesados, {} guardados", bestItems.size(), savedItems);
            
        } catch (Exception e) {
            logger.error("❌ Error ejecutando script de carga: {}", e.getMessage(), e);
            
            // Crear DTO de error
            ItemDTO errorDto = new ItemDTO();
            errorDto.setItemId(-1); // ID especial para indicar error
            errorDto.setName("Error en Script de Carga");
            errorDto.setType("ERROR");
            errorDto.setPlayer("SYSTEM");
            errorDto.setCount(0);
            errorDto.setEnchantLevel(0);
            errorDto.setLocation("ERROR");
            errorDto.setLocationData(0);
            errorDto.setRawAttributes("Error: " + e.getMessage());
            errorDto.setRawStats("Script falló");
            
            itemDTOList.add(errorDto);
        }

        return itemDTOList;
    }

    @Override
    public boolean forceReloadItemFromXml(int itemId) {
        logger.info("🔄 Forzando recarga del item {} desde XML", itemId);
        return loaderItemService.forceReloadItemFromXml(itemId);
    }
}
