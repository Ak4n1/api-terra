package com.ak4n1.terra.api.terra_api.game.repositories;

import com.ak4n1.terra.api.terra_api.game.entities.CharacterOfflineTrade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CharacterOfflineTradeRepository extends JpaRepository<CharacterOfflineTrade, Integer> {
    List<CharacterOfflineTrade> findAllByOrderByTimeDesc();
}