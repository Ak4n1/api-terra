package com.ak4n1.terra.api.terra_api.game.services;

import com.ak4n1.terra.api.terra_api.game.dto.ItemDTO;
import com.ak4n1.terra.api.terra_api.game.entities.Character;
import com.ak4n1.terra.api.terra_api.game.entities.Item;
import com.ak4n1.terra.api.terra_api.game.repositories.CharacterRepository;
import com.ak4n1.terra.api.terra_api.game.repositories.ItemRepository;
import com.ak4n1.terra.api.terra_api.game.l2j.data.ItemTable;
import com.ak4n1.terra.api.terra_api.game.l2j.model.item.ItemTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementación del servicio de inventario y almacenamiento de personajes.
 * 
 * <p>Este servicio proporciona acceso a los items que poseen los personajes,
 * incluyendo información completa del catálogo cargada en memoria. Los items
 * incluyen metadatos como nombre, tipo, icono y estadísticas del catálogo.
 * 
 * @see PlayerStorageService
 * @see ItemRepository
 * @see CharacterRepository
 * @see com.ak4n1.terra.api.terra_api.game.l2j.data.ItemTable
 * @author ak4n1
 * @since 1.0
 */
@Service
public class PlayerStorageServiceImpl implements PlayerStorageService {
    private static final Logger logger = LoggerFactory.getLogger(PlayerStorageServiceImpl.class);

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private CharacterRepository characterRepository;

    @Autowired
    private ItemTable itemTable;



    /**
     * {@inheritDoc}
     * 
     * <p>Este método es read-only y no modifica datos en la base de datos.
     * 
     * @param playerId ID del personaje (charId)
     * @return Lista de DTOs con todos los items del personaje
     */
    @Transactional(readOnly = true)
    @Override
    public List<ItemDTO> getItemsByPlayerId(int playerId) {
        List<Item> items = itemRepository.findByOwnerId(playerId);
        List<ItemDTO> itemDTOList = new ArrayList<>();

        for (Item item : items) {
            // ✅ NUEVO: Usar ItemTable en memoria en lugar de BD
            ItemTemplate template = itemTable.getTemplate(item.getItemId());

            String name = "Unknown";
            String type = "Unknown";
            String icon = "";
            String defaultAction = "";
            String bodyPart = "";
            
            if (template != null) {
                name = template.getName();
                type = template.getItemType();
                icon = template.getIcon();
                defaultAction = template.getDefaultAction();
                bodyPart = template.getBodyPart();
            } else {
                logger.warn("⚠️ Item {} no encontrado en catálogo", item.getItemId());
            }

            Optional<Character> character = characterRepository.findById(item.getOwnerId());
            String playerName = character.map(Character::getCharName).orElse("");

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
            dto.setDefaultAction(defaultAction);
            dto.setBodyPart(bodyPart);
            dto.setIcon(icon); // ✅ SIMPLIFICADO: Icon directamente en el nivel raíz

            itemDTOList.add(dto);
        }

        return itemDTOList;
    }



    /**
     * {@inheritDoc}
     * 
     * @param itemId ID del personaje (no utilizado, parámetro mantenido por compatibilidad)
     * @return Lista con un único DTO informativo sobre el estado del catálogo
     */
    @Override
    @Deprecated
    public List<ItemDTO> getTest(int itemId) {
        // ✅ SIMPLIFICADO: Ya no es necesario cargar items manualmente
        
        ItemDTO responseDto = new ItemDTO();
        responseDto.setItemId(0);
        responseDto.setName("Items cargados automáticamente");
        responseDto.setType("SYSTEM_INFO");
        responseDto.setPlayer("SYSTEM");
        responseDto.setCount(itemTable.getItemCount());
        responseDto.setEnchantLevel(0);
        responseDto.setLocation("MEMORY");
        responseDto.setLocationData(0);

        
        return List.of(responseDto);
    }

    /**
     * {@inheritDoc}
     * 
     * @param itemId ID del item (no utilizado, parámetro mantenido por compatibilidad)
     * @return true si la recarga se completó exitosamente
     */
    @Override
    public boolean forceReloadItemFromXml(int itemId) {
        // ✅ SIMPLIFICADO: Recarga todo el catálogo
        logger.info("🔄 Recargando catálogo completo de items...");
        itemTable.reload();
        return true;
    }
}
