package com.ak4n1.terra.api.terra_api.game.entities;
import org.hibernate.annotations.Immutable;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "character_offline_trade")
@Immutable
public class CharacterOfflineTrade implements Serializable {

    @Id
    @Column(name = "charId")
    private int charId;

    @Column(name = "time")
    private long time;

    @Column(name = "type")
    private byte type;

    @Column(name = "title")
    private String title;

    public int getCharId() {
        return charId;
    }

    public long getTime() {
        return time;
    }

    public byte getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }
}