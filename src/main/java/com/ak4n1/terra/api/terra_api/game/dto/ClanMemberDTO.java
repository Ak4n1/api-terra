package com.ak4n1.terra.api.terra_api.game.dto;

public class ClanMemberDTO {
    private String charName;
    private boolean online;

    public ClanMemberDTO(String charName, boolean online) {
        this.charName = charName;
        this.online = online;
    }

    public String getCharName() {
        return charName;
    }

    public void setCharName(String charName) {
        this.charName = charName;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }
}
