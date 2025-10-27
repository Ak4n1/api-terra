package com.ak4n1.terra.api.terra_api.game.dto;

public class PlayerRanking {


    private String name;
    private Integer kills;


    public PlayerRanking( String name ,Integer kills) {
        this.kills = kills;
        this.name = name;
    }

    public Integer getKills() {
        return kills;
    }

    public void setKills(Integer kills) {
        this.kills = kills;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
