package com.ak4n1.terra.api.terra_api.game.dto;

public class ClanRanking {

    private String clanName;
    private Integer hasCastle;
    private Integer reputationScore;

    public ClanRanking(String clanName, Integer hasCastle, Integer reputationScore) {
        this.clanName = clanName;
        this.hasCastle = hasCastle;
        this.reputationScore = reputationScore;
    }

    public String getClanName() {
        return clanName;
    }

    public void setClanName(String clanName) {
        this.clanName = clanName;
    }

    public Integer getHasCastle() {
        return hasCastle;
    }

    public void setHasCastle(Integer hasCastle) {
        this.hasCastle = hasCastle;
    }

    public Integer getReputationScore() {
        return reputationScore;
    }

    public void setReputationScore(Integer reputationScore) {
        this.reputationScore = reputationScore;
    }
}
