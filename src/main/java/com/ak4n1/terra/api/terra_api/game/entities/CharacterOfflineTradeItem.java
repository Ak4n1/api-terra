package com.ak4n1.terra.api.terra_api.game.entities;
import org.hibernate.annotations.Immutable;

import jakarta.persistence.*;

@Entity
@Table(name = "character_offline_trade_items")
@IdClass(CharacterOfflineTradeItemId.class)
@Immutable
public class CharacterOfflineTradeItem {

    @Id
    @Column(name = "charId")
    private int charId;

    @Id
    @Column(name = "item")
    private int itemId;

    @Column(name = "count")
    private long count;

    @Column(name = "price")
    private long price;

    public int getCharId() {
        return charId;
    }

    public int getItemId() {
        return itemId;
    }

    public long getCount() {
        return count;
    }

    public long getPrice() {
        return price;
    }
}