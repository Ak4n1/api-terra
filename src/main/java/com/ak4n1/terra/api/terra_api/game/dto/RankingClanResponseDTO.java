package com.ak4n1.terra.api.terra_api.game.dto;

import java.util.List;

public class RankingClanResponseDTO {

    private List<ClanRanking> response;

    public RankingClanResponseDTO(List<ClanRanking> response) {
        this.response = response;
    }

    public List<ClanRanking> getResponse() {
        return response;
    }

    public void setResponse(List<ClanRanking> response) {
        this.response = response;
    }
}
