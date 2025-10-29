package com.ak4n1.terra.api.terra_api.game.l2j.model.item;

import com.ak4n1.terra.api.terra_api.game.l2j.model.StatSet;

/**
 * Clase base simplificada para items del juego L2J
 * Contiene solo los campos esenciales para la API web
 */
public abstract class ItemTemplate {
    
    protected int _itemId;
    protected String _name;
    protected String _icon;
    protected int _weight;
    protected int _price;
    protected String _type;  // "Weapon", "Armor", "EtcItem"
    protected String _grade; // "None", "D", "C", "B", "A", "S"
    protected long _bodyPart;
    protected String _bodyPartName; // "rhand", "chest", "legs", etc.
    protected boolean _stackable;
    protected boolean _sellable;
    protected boolean _tradeable;
    protected boolean _dropable;
    protected int _crystalCount;
    protected String _crystalType;
    protected String _materialType;
    
    /**
     * Constructor protegido
     */
    protected ItemTemplate(StatSet set) {
        _itemId = set.getInt("item_id");
        _name = set.getString("name", "Unknown");
        _icon = set.getString("icon", "");
        _weight = set.getInt("weight", 0);
        _price = set.getInt("price", 0);
        _stackable = set.getBoolean("is_stackable", false);
        _sellable = set.getBoolean("is_sellable", true);
        _tradeable = set.getBoolean("is_tradable", true);
        _dropable = set.getBoolean("is_dropable", true);
        _crystalCount = set.getInt("crystal_count", 0);
        _crystalType = set.getString("crystal_type", "none");
        _materialType = set.getString("material", "");
        _bodyPart = set.getLong("bodypart", 0L);
        _bodyPartName = set.getString("bodypart_name", "");
    }
    
    // Getters esenciales
    public int getId() {
        return _itemId;
    }
    
    public String getName() {
        return _name;
    }
    
    public String getIcon() {
        return _icon;
    }
    
    public int getWeight() {
        return _weight;
    }
    
    public int getPrice() {
        return _price;
    }
    
    public String getType() {
        return _type;
    }
    
    public String getGrade() {
        return _grade;
    }
    
    public long getBodyPart() {
        return _bodyPart;
    }
    
    public String getBodyPartName() {
        return _bodyPartName;
    }
    
    public boolean isStackable() {
        return _stackable;
    }
    
    public boolean isSellable() {
        return _sellable;
    }
    
    public boolean isTradeable() {
        return _tradeable;
    }
    
    public boolean isDropable() {
        return _dropable;
    }
    
    public int getCrystalCount() {
        return _crystalCount;
    }
    
    public String getCrystalType() {
        return _crystalType;
    }
    
    public String getMaterialType() {
        return _materialType;
    }
    
    /**
     * Método abstracto para determinar el tipo de item
     */
    public abstract String getItemType();
    
    @Override
    public String toString() {
        return "ItemTemplate{" +
                "id=" + _itemId +
                ", name='" + _name + '\'' +
                ", type='" + getItemType() + '\'' +
                ", grade='" + _grade + '\'' +
                '}';
    }
}

