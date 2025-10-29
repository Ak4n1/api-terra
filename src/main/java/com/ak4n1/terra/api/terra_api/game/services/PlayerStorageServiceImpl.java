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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PlayerStorageServiceImpl implements PlayerStorageService {
    private static final Logger logger = LoggerFactory.getLogger(PlayerStorageServiceImpl.class);

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private CharacterRepository characterRepository;

    @Autowired
    private ItemTable itemTable;



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
            
            if (template != null) {
                name = template.getName();
                type = template.getItemType();
                icon = template.getIcon();
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
            
            // ✅ ARREGLADO: Enviar JSON correcto para que el frontend encuentre el icon
            // Frontend busca: item.attributes.icon
            String attributesJson = String.format("{\"icon\":\"%s\",\"name\":\"%s\"}", icon, name);
            String statsJson = String.format("{\"type\":\"%s\"}", type);
            
            dto.setRawAttributes(attributesJson);
            dto.setRawStats(statsJson);

            itemDTOList.add(dto);
        }

        logger.debug("✅ Obtenidos {} items para jugador {}", itemDTOList.size(), playerId);
        return itemDTOList;
    }



    @Override
    public List<ItemDTO> getTest(int itemId) {
        // ✅ SIMPLIFICADO: Ya no es necesario cargar items manualmente
        logger.info("ℹ️ Los items ahora se cargan automáticamente desde XMLs al iniciar la API");
        logger.info("📊 Total items en catálogo: {}", itemTable.getItemCount());
        
        ItemDTO responseDto = new ItemDTO();
        responseDto.setItemId(0);
        responseDto.setName("Items cargados automáticamente");
        responseDto.setType("SYSTEM_INFO");
        responseDto.setPlayer("SYSTEM");
        responseDto.setCount(itemTable.getItemCount());
        responseDto.setEnchantLevel(0);
        responseDto.setLocation("MEMORY");
        responseDto.setLocationData(0);
        responseDto.setRawAttributes("Items cargados en memoria desde XMLs");
        responseDto.setRawStats("Total: " + itemTable.getItemCount());
        
        return List.of(responseDto);
    }

    @Override
    public boolean forceReloadItemFromXml(int itemId) {
        // ✅ SIMPLIFICADO: Recarga todo el catálogo
        logger.info("🔄 Recargando catálogo completo de items...");
        itemTable.reload();
        return true;
    }
}
