package com.ak4n1.terra.api.terra_api.game.entities;

import org.hibernate.annotations.Immutable;

import jakarta.persistence.*;

@Entity
@Table(name = "items")
@Immutable
public class Item {

    @Id
    @Column(name = "object_id")
    private int objectId; // Este es el PK, que en offline_trade_items es el campo "item"

    @Column(name = "owner_id")
    private Integer ownerId;

    @Column(name = "item_id")
    private Integer itemId;  // El ID real del ítem (por ej: 57, 1001, etc)

    @Column(name = "count")
    private long count;

    @Column(name = "enchant_level")
    private Integer enchantLevel;

    @Column(name = "loc")
    private String loc;

    @Column(name = "loc_data")
    private Integer locData;

    @Column(name = "time_of_use")
    private Integer timeOfUse;

    @Column(name = "custom_type1")
    private Integer customType1;

    @Column(name = "custom_type2")
    private Integer customType2;

    @Column(name = "mana_left")
    private Integer manaLeft;

    @Column(name = "time")
    private Long time;

    // Getters y setters

    public int getObjectId() {
        return objectId;
    }

    public void setObjectId(int objectId) {
        this.objectId = objectId;
    }

    public Integer getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Integer ownerId) {
        this.ownerId = ownerId;
    }

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public Integer getEnchantLevel() {
        return enchantLevel;
    }

    public void setEnchantLevel(Integer enchantLevel) {
        this.enchantLevel = enchantLevel;
    }

    public String getLoc() {
        return loc;
    }

    public void setLoc(String loc) {
        this.loc = loc;
    }

    public Integer getLocData() {
        return locData;
    }

    public void setLocData(Integer locData) {
        this.locData = locData;
    }

    public Integer getTimeOfUse() {
        return timeOfUse;
    }

    public void setTimeOfUse(Integer timeOfUse) {
        this.timeOfUse = timeOfUse;
    }

    public Integer getCustomType1() {
        return customType1;
    }

    public void setCustomType1(Integer customType1) {
        this.customType1 = customType1;
    }

    public Integer getCustomType2() {
        return customType2;
    }

    public void setCustomType2(Integer customType2) {
        this.customType2 = customType2;
    }

    public Integer getManaLeft() {
        return manaLeft;
    }

    public void setManaLeft(Integer manaLeft) {
        this.manaLeft = manaLeft;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "Item{" +
                "objectId=" + objectId +
                ", ownerId=" + ownerId +
                ", itemId=" + itemId +
                ", count=" + count +
                ", enchantLevel=" + enchantLevel +
                ", loc='" + loc + '\'' +
                ", locData=" + locData +
                ", timeOfUse=" + timeOfUse +
                ", customType1=" + customType1 +
                ", customType2=" + customType2 +
                ", manaLeft=" + manaLeft +
                ", time=" + time +
                '}';
    }

}
