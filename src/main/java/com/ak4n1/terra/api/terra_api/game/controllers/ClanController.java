package com.ak4n1.terra.api.terra_api.game.controllers;

import com.ak4n1.terra.api.terra_api.game.dto.ClanRequestDTO;
import com.ak4n1.terra.api.terra_api.game.dto.ClanResponseDTO;
import com.ak4n1.terra.api.terra_api.game.services.ClanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/game/clan")
public class ClanController {


    @Autowired
    private ClanService clanService;


    @PostMapping("/by-id") 
    public ResponseEntity<ClanResponseDTO> getClanById(@RequestBody ClanRequestDTO request) {
        ClanResponseDTO response = clanService.getClanById(request.getClanId());
        return ResponseEntity.ok(response);
    }


}
