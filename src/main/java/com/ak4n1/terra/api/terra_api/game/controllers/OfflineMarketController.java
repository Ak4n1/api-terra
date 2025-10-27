package com.ak4n1.terra.api.terra_api.game.controllers;

import com.ak4n1.terra.api.terra_api.game.dto.OfflineStoreDTO;
import com.ak4n1.terra.api.terra_api.game.services.OfflineMarketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/game/offline-market")
public class OfflineMarketController {

    @Autowired
    private OfflineMarketService offlineMarketService;

    @GetMapping
    public List<OfflineStoreDTO> getOfflineStores() {
        return offlineMarketService.getAllOfflineStores();
    }

    @PostMapping
    public List<OfflineStoreDTO> testItems() {
        return offlineMarketService.getTest();
    }

}