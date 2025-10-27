package com.ak4n1.terra.api.terra_api.game.services;

import java.util.List;

import com.ak4n1.terra.api.terra_api.game.dto.ItemDTO;

public interface PlayerStorageService {

    List<ItemDTO> getItemsByPlayerId(int playerId);
    List<ItemDTO> getTest(int playerId);
    boolean forceReloadItemFromXml(int itemId);

}
