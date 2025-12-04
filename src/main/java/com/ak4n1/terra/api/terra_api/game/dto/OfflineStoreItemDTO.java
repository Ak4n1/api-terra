package com.ak4n1.terra.api.terra_api.game.dto;

public class OfflineStoreItemDTO {
    private int itemId;
    private long count;
    private long price;
    private Integer enchantLevel;
    private Long time;
    private String name;
    private String type;
    private String icon; // Icono del item (directamente en el nivel raíz)
    private String grade; // Crystal type/Grade (D, C, B, A, S) - directamente en el nivel raíz

    // Constructor vacío por claridad
    public OfflineStoreItemDTO() {}

    // Setters
    public void setItemId(int itemId) { this.itemId = itemId; }
    public void setCount(long count) { this.count = count; }
    public void setPrice(long price) { this.price = price; }
    public void setEnchantLevel(Integer enchantLevel) { this.enchantLevel = enchantLevel; }
    public void setTime(Long time) { this.time = time; }
    public void setName(String name) { this.name = name; }
    public void setType(String type) { this.type = type; }
    public void setIcon(String icon) { this.icon = icon; }
    public void setGrade(String grade) { this.grade = grade; }

    // Getters
    public int getItemId() { return itemId; }
    public long getCount() { return count; }
    public long getPrice() { return price; }
    public Integer getEnchantLevel() { return enchantLevel; }
    public Long getTime() { return time; }
    public String getName() { return name != null ? name : ""; }
    public String getType() { return type != null ? type : ""; }
    public String getIcon() { return icon != null ? icon : ""; }
    public String getGrade() { return grade != null ? grade : ""; }
}
