package com.ak4n1.terra.api.terra_api.game.services;

import com.ak4n1.terra.api.terra_api.game.entities.CharacterOfflineTrade;
import com.ak4n1.terra.api.terra_api.game.entities.CharacterOfflineTradeItem;
import com.ak4n1.terra.api.terra_api.game.entities.Item;
import com.ak4n1.terra.api.terra_api.game.entities.ItemXmlEntity;
import com.ak4n1.terra.api.terra_api.game.repositories.CharacterRepository;
import com.ak4n1.terra.api.terra_api.game.repositories.CharacterOfflineTradeRepository;
import com.ak4n1.terra.api.terra_api.game.repositories.CharacterOfflineTradeItemRepository;
import com.ak4n1.terra.api.terra_api.game.repositories.ItemRepository;
import com.ak4n1.terra.api.terra_api.game.repositories.ItemXmlRepository;
import com.ak4n1.terra.api.terra_api.game.utils.ItemLoaderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.ak4n1.terra.api.terra_api.game.dto.OfflineStoreDTO;
import com.ak4n1.terra.api.terra_api.game.dto.OfflineStoreItemDTO;
import com.ak4n1.terra.api.terra_api.game.entities.Character;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class OfflineMarketServiceImpl implements OfflineMarketService {
    private static final Logger logger = LoggerFactory.getLogger(OfflineMarketServiceImpl.class);

    @Value("${terra.items.path}")
    private String resourceFolder;

    @Autowired
    private ItemLoaderService itemLoaderService;

    @Autowired
    private CharacterRepository characterRepository;

    @Autowired
    private CharacterOfflineTradeRepository tradeRepo;

    @Autowired
    private CharacterOfflineTradeItemRepository itemRepo;

    @Autowired
    private ItemXmlRepository itemXmlRepo;

    @Autowired
    private ItemRepository itemsRepo;

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

                    // Usar List para evitar error de duplicados
                    List<ItemXmlEntity> itemXmlList = itemXmlRepo.findAllByIdCustom(itemDTO.getItemId());
                    if (!itemXmlList.isEmpty()) {
                        ItemXmlEntity itemXml = itemXmlList.get(0);
                        itemDTO.setName(itemXml.getName());
                        itemDTO.setType(itemXml.getType());
                        itemDTO.setAttributes(itemXml.getAttributes());
                        itemDTO.setStats(itemXml.getStats());
                    } else {
                        logger.warn("ItemXmlEntity NO encontrada para itemId {}", itemDTO.getItemId());
                    }
                }
                if (trade.getType() == 3) {
                    itemDTO.setItemId(item.getItemId());

                    List<ItemXmlEntity> itemXmlList = itemXmlRepo.findAllByIdCustom(itemDTO.getItemId());
                    if (!itemXmlList.isEmpty()) {
                        ItemXmlEntity itemXml = itemXmlList.get(0);
                        itemDTO.setName(itemXml.getName());
                        itemDTO.setType(itemXml.getType());
                        itemDTO.setAttributes(itemXml.getAttributes());
                        itemDTO.setStats(itemXml.getStats());
                    } else {
                        logger.warn("ItemXmlEntity NO encontrada para itemId {}", item.getItemId());
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


    @Override
    public List<OfflineStoreDTO> getTest() {
        return new ArrayList<>();
    }
}
