package com.ak4n1.terra.api.terra_api.game.services;

import java.util.List;

import com.ak4n1.terra.api.terra_api.game.dto.OfflineStoreDTO;

public interface OfflineMarketService {
    List<OfflineStoreDTO> getAllOfflineStores();

    List<OfflineStoreDTO> getTest();

}
