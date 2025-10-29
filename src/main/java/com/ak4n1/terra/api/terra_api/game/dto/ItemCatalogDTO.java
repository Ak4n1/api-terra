package com.ak4n1.terra.api.terra_api.game.dto;

import com.ak4n1.terra.api.terra_api.game.l2j.model.item.Armor;
import com.ak4n1.terra.api.terra_api.game.l2j.model.item.ItemTemplate;
import com.ak4n1.terra.api.terra_api.game.l2j.model.item.Weapon;

/**
 * DTO para respuestas del catálogo de items
 * Representa la información de un item del juego (NO una instancia en inventario)
 */
public class ItemCatalogDTO {
    
    private int id;
    private String name;
    private String icon;
    private String type;          // "Weapon", "Armor", "EtcItem"
    private String subType;        // "SWORD", "LIGHT", "POTION", etc.
    private String grade;          // "D", "C", "B", "A", "S", "NONE"
    private int weight;
    private int price;
    private boolean stackable;
    private boolean sellable;
    private boolean tradeable;
    private boolean dropable;
    private String bodyPart;
    private String materialType;
    private int crystalCount;
    private String crystalType;
    
    // Stats específicos para armas
    private Integer pAtk;
    private Integer mAtk;
    private Integer soulshots;
    private Integer spiritshots;
    
    // Stats específicos para armaduras
    private Integer pDef;
    private Integer mDef;
    
    /**
     * Constructor desde ItemTemplate
     */
    public static ItemCatalogDTO fromItemTemplate(ItemTemplate template) {
        if (template == null) {
            return null;
        }
        
        ItemCatalogDTO dto = new ItemCatalogDTO();
        
        // Campos comunes
        dto.id = template.getId();
        dto.name = template.getName();
        dto.icon = template.getIcon();
        dto.type = template.getItemType();
        dto.grade = template.getGrade();
        dto.weight = template.getWeight();
        dto.price = template.getPrice();
        dto.stackable = template.isStackable();
        dto.sellable = template.isSellable();
        dto.tradeable = template.isTradeable();
        dto.dropable = template.isDropable();
        dto.bodyPart = template.getBodyPartName();
        dto.materialType = template.getMaterialType();
        dto.crystalCount = template.getCrystalCount();
        dto.crystalType = template.getCrystalType();
        
        // Campos específicos según el tipo
        if (template instanceof Weapon) {
            Weapon weapon = (Weapon) template;
            dto.subType = weapon.getWeaponType();
            dto.pAtk = weapon.getPAtk();
            dto.mAtk = weapon.getMAtk();
            dto.soulshots = weapon.getSoulshots();
            dto.spiritshots = weapon.getSpiritshots();
        } else if (template instanceof Armor) {
            Armor armor = (Armor) template;
            dto.subType = armor.getArmorType();
            dto.pDef = armor.getPDef();
            dto.mDef = armor.getMDef();
        }
        
        return dto;
    }
    
    // Getters y Setters
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getIcon() {
        return icon;
    }
    
    public void setIcon(String icon) {
        this.icon = icon;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getSubType() {
        return subType;
    }
    
    public void setSubType(String subType) {
        this.subType = subType;
    }
    
    public String getGrade() {
        return grade;
    }
    
    public void setGrade(String grade) {
        this.grade = grade;
    }
    
    public int getWeight() {
        return weight;
    }
    
    public void setWeight(int weight) {
        this.weight = weight;
    }
    
    public int getPrice() {
        return price;
    }
    
    public void setPrice(int price) {
        this.price = price;
    }
    
    public boolean isStackable() {
        return stackable;
    }
    
    public void setStackable(boolean stackable) {
        this.stackable = stackable;
    }
    
    public boolean isSellable() {
        return sellable;
    }
    
    public void setSellable(boolean sellable) {
        this.sellable = sellable;
    }
    
    public boolean isTradeable() {
        return tradeable;
    }
    
    public void setTradeable(boolean tradeable) {
        this.tradeable = tradeable;
    }
    
    public boolean isDropable() {
        return dropable;
    }
    
    public void setDropable(boolean dropable) {
        this.dropable = dropable;
    }
    
    public String getBodyPart() {
        return bodyPart;
    }
    
    public void setBodyPart(String bodyPart) {
        this.bodyPart = bodyPart;
    }
    
    public String getMaterialType() {
        return materialType;
    }
    
    public void setMaterialType(String materialType) {
        this.materialType = materialType;
    }
    
    public int getCrystalCount() {
        return crystalCount;
    }
    
    public void setCrystalCount(int crystalCount) {
        this.crystalCount = crystalCount;
    }
    
    public String getCrystalType() {
        return crystalType;
    }
    
    public void setCrystalType(String crystalType) {
        this.crystalType = crystalType;
    }
    
    public Integer getPAtk() {
        return pAtk;
    }
    
    public void setPAtk(Integer pAtk) {
        this.pAtk = pAtk;
    }
    
    public Integer getMAtk() {
        return mAtk;
    }
    
    public void setMAtk(Integer mAtk) {
        this.mAtk = mAtk;
    }
    
    public Integer getSoulshots() {
        return soulshots;
    }
    
    public void setSoulshots(Integer soulshots) {
        this.soulshots = soulshots;
    }
    
    public Integer getSpiritshots() {
        return spiritshots;
    }
    
    public void setSpiritshots(Integer spiritshots) {
        this.spiritshots = spiritshots;
    }
    
    public Integer getPDef() {
        return pDef;
    }
    
    public void setPDef(Integer pDef) {
        this.pDef = pDef;
    }
    
    public Integer getMDef() {
        return mDef;
    }
    
    public void setMDef(Integer mDef) {
        this.mDef = mDef;
    }
}

