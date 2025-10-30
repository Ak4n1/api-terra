package com.ak4n1.terra.api.terra_api.game.services;

import com.ak4n1.terra.api.terra_api.game.entities.CharacterOfflineTrade;
import com.ak4n1.terra.api.terra_api.game.entities.CharacterOfflineTradeItem;
import com.ak4n1.terra.api.terra_api.game.entities.Item;
import com.ak4n1.terra.api.terra_api.game.repositories.CharacterRepository;
import com.ak4n1.terra.api.terra_api.game.repositories.CharacterOfflineTradeRepository;
import com.ak4n1.terra.api.terra_api.game.repositories.CharacterOfflineTradeItemRepository;
import com.ak4n1.terra.api.terra_api.game.repositories.ItemRepository;
import com.ak4n1.terra.api.terra_api.game.l2j.data.ItemTable;
import com.ak4n1.terra.api.terra_api.game.l2j.model.item.ItemTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ak4n1.terra.api.terra_api.game.dto.OfflineStoreDTO;
import com.ak4n1.terra.api.terra_api.game.dto.OfflineStoreItemDTO;
import com.ak4n1.terra.api.terra_api.game.entities.Character;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementación del servicio de mercado offline.
 * 
 * <p>Este servicio proporciona acceso a las tiendas offline donde los jugadores
 * pueden vender items mientras están desconectados. Cada tienda incluye información
 * del vendedor, items disponibles con metadatos del catálogo y precios.
 * 
 * @see OfflineMarketService
 * @see com.ak4n1.terra.api.terra_api.game.l2j.data.ItemTable
 * @see CharacterOfflineTradeRepository
 * @author ak4n1
 * @since 1.0
 */
@Service
public class OfflineMarketServiceImpl implements OfflineMarketService {
    private static final Logger logger = LoggerFactory.getLogger(OfflineMarketServiceImpl.class);

    @Autowired
    private ItemTable itemTable;

    @Autowired
    private CharacterRepository characterRepository;

    @Autowired
    private CharacterOfflineTradeRepository tradeRepo;

    @Autowired
    private CharacterOfflineTradeItemRepository itemRepo;

    @Autowired
    private ItemRepository itemsRepo;

    /**
     * {@inheritDoc}
     * 
     * @return Lista de DTOs con todas las tiendas offline disponibles
     */
    @Override
    public List<OfflineStoreDTO> getAllOfflineStores() {
        List<CharacterOfflineTrade> trades = tradeRepo.findAllByOrderByTimeDesc();
        List<OfflineStoreDTO> result = new ArrayList<>();

        for (CharacterOfflineTrade trade : trades) {
            List<CharacterOfflineTradeItem> tradeItems = itemRepo.findByCharId(trade.getCharId());
            List<OfflineStoreItemDTO> itemsDTO = new ArrayList<>();

            for (CharacterOfflineTradeItem item : tradeItems) {
                OfflineStoreItemDTO itemDTO = new OfflineStoreItemDTO();
                itemDTO.setCount(item.getCount());
                itemDTO.setPrice(item.getPrice());

                if (trade.getType() == 1) {
                    Optional<Item> itemEntityOpt = itemsRepo.findById(item.getItemId());
                    if (itemEntityOpt.isPresent()) {
                        Item itemEntity = itemEntityOpt.get();
                        itemDTO.setItemId(itemEntity.getItemId());
                        itemDTO.setEnchantLevel(itemEntity.getEnchantLevel());
                        itemDTO.setTime(itemEntity.getTime());
                    } else {
                        logger.warn("Item entity NO encontrada para itemId {}, se usa DTO básico.", item.getItemId());
                        itemDTO.setItemId(item.getItemId()); // Setear igual para evitar null
                    }

                    // ✅ NUEVO: Usar ItemTable en memoria
                    ItemTemplate template = itemTable.getTemplate(itemDTO.getItemId());
                    if (template != null) {
                        itemDTO.setName(template.getName());
                        itemDTO.setType(template.getItemType());
                        // Formato JSON para que el frontend encuentre el icon
                        String attributesJson = String.format("{\"icon\":\"%s\",\"name\":\"%s\"}", 
                            template.getIcon(), template.getName());
                        itemDTO.setAttributes(attributesJson);
                        itemDTO.setStats(template.getGrade());
                    } else {
                        logger.warn("Item {} no encontrado en catálogo", itemDTO.getItemId());
                    }
                }
                if (trade.getType() == 3) {
                    itemDTO.setItemId(item.getItemId());

                    // ✅ NUEVO: Usar ItemTable en memoria
                    ItemTemplate template = itemTable.getTemplate(itemDTO.getItemId());
                    if (template != null) {
                        itemDTO.setName(template.getName());
                        itemDTO.setType(template.getItemType());
                        // Formato JSON para que el frontend encuentre el icon
                        String attributesJson = String.format("{\"icon\":\"%s\",\"name\":\"%s\"}", 
                            template.getIcon(), template.getName());
                        itemDTO.setAttributes(attributesJson);
                        itemDTO.setStats(template.getGrade());
                    } else {
                        logger.warn("Item {} no encontrado en catálogo", item.getItemId());
                    }
                }

                itemsDTO.add(itemDTO);
            }

            Optional<Character> characterOpt = characterRepository.findByCharId(trade.getCharId());
            String characterName = characterOpt.map(Character::getCharName).orElse("Unknown");

            OfflineStoreDTO storeDTO = new OfflineStoreDTO();
            storeDTO.setChar_name(characterName);
            storeDTO.setTitle(trade.getTitle());
            storeDTO.setType(trade.getType());
            storeDTO.setTime(trade.getTime());
            storeDTO.setItems(itemsDTO);

            result.add(storeDTO);
        }

        logger.debug("🏪 [OFFLINE MARKET] Total de tiendas offline encontradas: {}", result.size());
        return result;
    }


    /**
     * {@inheritDoc}
     * 
     * @return Lista vacía de tiendas offline
     */
    @Override
    @Deprecated
    public List<OfflineStoreDTO> getTest() {
        return new ArrayList<>();
    }
}
