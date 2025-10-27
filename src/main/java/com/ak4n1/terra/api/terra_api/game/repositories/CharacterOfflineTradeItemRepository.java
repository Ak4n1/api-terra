package com.ak4n1.terra.api.terra_api.game.repositories;

import com.ak4n1.terra.api.terra_api.game.entities.CharacterOfflineTradeItem;
import com.ak4n1.terra.api.terra_api.game.entities.CharacterOfflineTradeItemId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CharacterOfflineTradeItemRepository extends JpaRepository<CharacterOfflineTradeItem, CharacterOfflineTradeItemId> {
    List<CharacterOfflineTradeItem> findByCharId(int charId);
}