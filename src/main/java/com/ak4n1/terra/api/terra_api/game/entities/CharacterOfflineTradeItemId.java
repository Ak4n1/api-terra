package com.ak4n1.terra.api.terra_api.game.entities;

import java.io.Serializable;
import java.util.Objects;

public class CharacterOfflineTradeItemId implements Serializable {

    private int charId;
    private int itemId;

    public CharacterOfflineTradeItemId() {}

    public CharacterOfflineTradeItemId(int charId, int itemId) {
        this.charId = charId;
        this.itemId = itemId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CharacterOfflineTradeItemId that)) return false;
        return charId == that.charId && itemId == that.itemId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(charId, itemId);
    }
}