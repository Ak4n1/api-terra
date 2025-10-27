package com.ak4n1.terra.api.terra_api.game.dto;

import java.util.ArrayList;
import java.util.List;

public class RankingPKResponseDTO {

    private List<PlayerRanking> topPk;

    public RankingPKResponseDTO(){
        this.topPk = new ArrayList<>();
    }

    public void addPlayer(PlayerRanking p){
        if (!this.topPk.contains(p)){
        this.topPk.add(p);
        }
    }

    public void setTopPk(List<PlayerRanking> topPk) {
        this.topPk = topPk;
    }

    public List<PlayerRanking> getTopPk() {
        return topPk;
    }
}
