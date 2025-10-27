package com.ak4n1.terra.api.terra_api.game.controllers;

import com.ak4n1.terra.api.terra_api.game.dto.PlayerRanking;
import com.ak4n1.terra.api.terra_api.game.dto.RankingClanResponseDTO;
import com.ak4n1.terra.api.terra_api.game.dto.RankingPKResponseDTO;
import com.ak4n1.terra.api.terra_api.game.dto.RankingPvPResponseDTO;
import com.ak4n1.terra.api.terra_api.game.services.RankingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/game/ranking")
public class RankingsController {

    @Autowired
    private RankingsService rankingsService;


    @GetMapping("/top-pvp")
    public RankingPvPResponseDTO getTopPvp() {
        RankingPvPResponseDTO response = this.rankingsService.getTopPvp();

        return response;
    }

    @GetMapping("/top-pk")
    public RankingPKResponseDTO getTopPk() {
        RankingPKResponseDTO response = this.rankingsService.getTopPk();
        return response;
    }

    @GetMapping("/top-clans")
    public RankingClanResponseDTO getTopClans() {
        return this.rankingsService.getTopClans();
    }

}
