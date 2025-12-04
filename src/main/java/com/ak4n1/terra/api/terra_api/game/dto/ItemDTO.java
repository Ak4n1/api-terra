package com.ak4n1.terra.api.terra_api.game.dto;

public class ItemDTO {
    private int objectId;
    private int itemId;
    private long count;
    private int enchantLevel;
    private String location;
    private int locationData;
    private String name = "item";
    private String type;
    private String player;
    private String defaultAction;
    private String bodyPart;
    private String icon; // Icono del item (directamente en el nivel raíz)

    public String getPlayer() {
        return player;
    }

    public void setPlayer(String player) {
        this.player = player;
    }

    public ItemDTO() {}

    // Getters y setters básicos
    public int getObjectId() { return objectId; }
    public void setObjectId(int objectId) { this.objectId = objectId; }

    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }

    public long getCount() { return count; }
    public void setCount(long count) { this.count = count; }

    public int getEnchantLevel() { return enchantLevel; }
    public void setEnchantLevel(int enchantLevel) { this.enchantLevel = enchantLevel; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public int getLocationData() { return locationData; }
    public void setLocationData(int locationData) { this.locationData = locationData; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getDefaultAction() { return defaultAction; }
    public void setDefaultAction(String defaultAction) { this.defaultAction = defaultAction; }

    public String getBodyPart() { return bodyPart != null ? bodyPart : ""; }
    public void setBodyPart(String bodyPart) { this.bodyPart = bodyPart; }

    public String getIcon() { return icon != null ? icon : ""; }
    public void setIcon(String icon) { this.icon = icon; }
}
