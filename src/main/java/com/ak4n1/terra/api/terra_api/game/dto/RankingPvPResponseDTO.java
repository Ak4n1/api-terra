package com.ak4n1.terra.api.terra_api.game.dto;

import java.util.ArrayList;
import java.util.List;

public class RankingPvPResponseDTO {

    private List<PlayerRanking> topPvp;


    public RankingPvPResponseDTO(){
        this.topPvp = new ArrayList<>();
    }
    public void addPlayer(PlayerRanking p){
        if (!this.topPvp.contains(p)){
            this.topPvp.add(p);
        }
    }

    public List<PlayerRanking> getTopPvp() {
        return topPvp;
    }

    public void setTopPvp(List<PlayerRanking> topPvp) {
        this.topPvp = topPvp;
    }
}
