package com.ak4n1.terra.api.terra_api.game.services;

import com.ak4n1.terra.api.terra_api.game.entities.CharacterOfflineTrade;
import com.ak4n1.terra.api.terra_api.game.entities.CharacterOfflineTradeItem;
import com.ak4n1.terra.api.terra_api.game.entities.Item;
import com.ak4n1.terra.api.terra_api.game.repositories.CharacterRepository;
import com.ak4n1.terra.api.terra_api.game.repositories.CharacterOfflineTradeRepository;
import com.ak4n1.terra.api.terra_api.game.repositories.CharacterOfflineTradeItemRepository;
import com.ak4n1.terra.api.terra_api.game.repositories.ItemRepository;
import com.ak4n1.terra.api.terra_api.game.l2j.data.ItemTable;
import com.ak4n1.terra.api.terra_api.game.l2j.data.MapRegionTable;
import com.ak4n1.terra.api.terra_api.game.l2j.model.item.ItemTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ak4n1.terra.api.terra_api.game.dto.OfflineStoreDTO;
import com.ak4n1.terra.api.terra_api.game.dto.OfflineStoreItemDTO;
import com.ak4n1.terra.api.terra_api.game.entities.Character;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
    private MapRegionTable mapRegionTable;

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
            // Filtrar solo tipos 1 (SELL), 3 (BUY) y 8 (PACKAGE_SELL)
            byte storeType = trade.getType();
            if (storeType != 1 && storeType != 3 && storeType != 8) {
                continue; // Saltar tipos que no queremos mostrar
            }

            List<CharacterOfflineTradeItem> tradeItems = itemRepo.findByCharId(trade.getCharId());
            List<OfflineStoreItemDTO> itemsDTO = new ArrayList<>();

            for (CharacterOfflineTradeItem item : tradeItems) {
                OfflineStoreItemDTO itemDTO = new OfflineStoreItemDTO();
                itemDTO.setCount(item.getCount());
                itemDTO.setPrice(item.getPrice());

                // Tipo 1 (SELL) y Tipo 8 (PACKAGE_SELL) - ambos tienen items físicos con enchant/time
                if (storeType == 1 || storeType == 8) {
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
                        itemDTO.setIcon(template.getIcon()); // ✅ SIMPLIFICADO: Icon directamente en el nivel raíz
                        itemDTO.setGrade(template.getGrade()); // ✅ SIMPLIFICADO: Grade directamente en el nivel raíz
                    } else {
                        logger.warn("Item {} no encontrado en catálogo", itemDTO.getItemId());
                    }
                }
                // Tipo 3 (BUY) - el jugador quiere comprar, no tiene items físicos
                else if (storeType == 3) {
                    itemDTO.setItemId(item.getItemId());

                    // ✅ NUEVO: Usar ItemTable en memoria
                    ItemTemplate template = itemTable.getTemplate(itemDTO.getItemId());
                    if (template != null) {
                        itemDTO.setName(template.getName());
                        itemDTO.setType(template.getItemType());
                        itemDTO.setIcon(template.getIcon()); // ✅ SIMPLIFICADO: Icon directamente en el nivel raíz
                        itemDTO.setGrade(template.getGrade()); // ✅ SIMPLIFICADO: Grade directamente en el nivel raíz
                    } else {
                        logger.warn("Item {} no encontrado en catálogo", item.getItemId());
                    }
                }

                itemsDTO.add(itemDTO);
            }

            Optional<Character> characterOpt = characterRepository.findByCharId(trade.getCharId());
            String characterName = characterOpt.map(Character::getCharName).orElse("Unknown");
            
            // Obtener la ciudad basándose en las coordenadas del personaje
            String city = "Unknown";
            if (characterOpt.isPresent()) {
                Character character = characterOpt.get();
                Integer x = character.getX();
                Integer y = character.getY();
                if (x != null && y != null) {
                    city = mapRegionTable.getClosestTownName(x, y);
                }
            }

            OfflineStoreDTO storeDTO = new OfflineStoreDTO();
            storeDTO.setChar_name(characterName);
            storeDTO.setTitle(trade.getTitle());
            storeDTO.setType(trade.getType());
            storeDTO.setTime(trade.getTime());
            storeDTO.setCity(city);
            storeDTO.setItems(itemsDTO);

            result.add(storeDTO);
        }

        return result;
    }

    /**
     * {@inheritDoc}
     * 
     * @param page Número de página (0-based)
     * @param size Elementos por página
     * @param filters Mapa con los filtros a aplicar
     * @return Respuesta paginada con tiendas offline filtradas
     */
    @Override
    public Map<String, Object> getOfflineStoresPaginated(int page, int size, Map<String, String> filters) {
        // Obtener todas las tiendas
        List<OfflineStoreDTO> allStores = getAllOfflineStores();
        
        // Aplicar filtros
        List<OfflineStoreDTO> filteredStores = applyFilters(allStores, filters);
        
        // Calcular totales
        int totalElements = filteredStores.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        
        // Paginar
        int startIndex = page * size;
        int endIndex = Math.min(startIndex + size, filteredStores.size());
        List<OfflineStoreDTO> pagedStores = filteredStores.subList(startIndex, endIndex);
        
        // Construir respuesta
        Map<String, Object> response = new HashMap<>();
        response.put("content", pagedStores);
        response.put("totalElements", totalElements);
        response.put("totalPages", totalPages);
        response.put("currentPage", page);
        response.put("size", size);
        response.put("hasNext", page < totalPages - 1);
        response.put("hasPrevious", page > 0);
        
        
        return response;
    }

    /**
     * Aplica los filtros a la lista de tiendas.
     * 
     * @param stores Lista de tiendas a filtrar
     * @param filters Mapa con los filtros a aplicar
     * @return Lista de tiendas filtradas
     */
    private List<OfflineStoreDTO> applyFilters(List<OfflineStoreDTO> stores, Map<String, String> filters) {
        if (filters == null || filters.isEmpty()) {
            return stores;
        }
        
        List<OfflineStoreDTO> filtered = new ArrayList<>(stores);
        
        // Filtro por tipo de tienda (1=Sell, 3=Buy, 8=Pack)
        String storeTypeStr = filters.get("storeType");
        if (storeTypeStr != null && !storeTypeStr.trim().isEmpty()) {
            try {
                byte storeType = Byte.parseByte(storeTypeStr);
                filtered = filtered.stream()
                    .filter(store -> store.getType() == storeType)
                    .collect(Collectors.toList());
            } catch (NumberFormatException e) {
                logger.warn("Valor inválido para storeType: {}", storeTypeStr);
            }
        }
        
        // Filtro de búsqueda (nombre del personaje, título o nombre de item)
        String searchTerm = filters.get("searchTerm");
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            String search = searchTerm.toLowerCase().trim();
            filtered = filtered.stream()
                .filter(store -> 
                    (store.getChar_name() != null && store.getChar_name().toLowerCase().contains(search)) ||
                    (store.getTitle() != null && store.getTitle().toLowerCase().contains(search)) ||
                    store.getItems().stream().anyMatch(item -> 
                        item.getName() != null && item.getName().toLowerCase().contains(search)
                    )
                )
                .collect(Collectors.toList());
        }
        
        // Ordenamiento
        String sortBy = filters.getOrDefault("sortBy", "time");
        filtered.sort((a, b) -> {
            switch (sortBy) {
                case "time":
                    return Long.compare(b.getTime(), a.getTime()); // Más reciente primero
                case "oldest":
                    return Long.compare(a.getTime(), b.getTime()); // Más viejo primero
                case "price":
                    long priceA = a.getItems().stream().mapToLong(item -> item.getPrice()).max().orElse(0);
                    long priceB = b.getItems().stream().mapToLong(item -> item.getPrice()).max().orElse(0);
                    return Long.compare(priceB, priceA); // Mayor precio primero
                case "name":
                    return a.getChar_name().compareToIgnoreCase(b.getChar_name());
                case "enchant":
                    long enchantA = a.getItems().stream()
                        .mapToLong(item -> item.getEnchantLevel() != null ? item.getEnchantLevel() : 0)
                        .max().orElse(0);
                    long enchantB = b.getItems().stream()
                        .mapToLong(item -> item.getEnchantLevel() != null ? item.getEnchantLevel() : 0)
                        .max().orElse(0);
                    return Long.compare(enchantB, enchantA); // Mayor enchant primero
                default:
                    return Long.compare(b.getTime(), a.getTime());
            }
        });
        
        return filtered;
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
