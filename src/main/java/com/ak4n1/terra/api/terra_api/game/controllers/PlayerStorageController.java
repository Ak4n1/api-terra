package com.ak4n1.terra.api.terra_api.game.controllers;

import com.ak4n1.terra.api.terra_api.game.dto.ItemDTO;
import com.ak4n1.terra.api.terra_api.game.dto.PlayerRequestDTO;
import com.ak4n1.terra.api.terra_api.game.services.PlayerStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/game/storage")
public class PlayerStorageController {

    @Autowired
    private PlayerStorageService playerStorageService;
    
    @PostMapping("/inventory")
    public List<ItemDTO> getPlayerItems(@RequestBody PlayerRequestDTO request) {
        return playerStorageService.getItemsByPlayerId(request.getPlayerId());
    }

    @GetMapping("/inventory")
    public List<ItemDTO> getTest() {
        return playerStorageService.getTest(29520);
    }
    

}
