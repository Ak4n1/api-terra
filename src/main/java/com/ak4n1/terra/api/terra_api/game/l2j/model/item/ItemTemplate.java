package com.ak4n1.terra.api.terra_api.game.l2j.model.item;

import com.ak4n1.terra.api.terra_api.game.l2j.model.StatSet;

/**
 * Clase base abstracta simplificada para items del juego L2J.
 * 
 * <p>Contiene solo los campos esenciales necesarios para la API web. Esta clase
 * sirve como base para los tipos específicos de items: Weapon, Armor y EtcItem.
 * Es la clase RECOMENDADA para representar items del catálogo.
 * 
 * @see Weapon
 * @see Armor
 * @see EtcItem
 * @see StatSet
 * @author ak4n1
 * @since 1.0
 */
public abstract class ItemTemplate {
    
    protected int _itemId;
    protected String _name;
    protected String _icon;
    protected int _weight;
    protected int _price;
    protected String _type;  // "Weapon", "Armor", "EtcItem"
    protected String _grade; // "None", "D", "C", "B", "A", "S"
    protected String _bodyPart; // "rhand", "chest", "legs", "talisman", "brooch_jewel", "lbracelet", etc.
    protected boolean _stackable;
    protected boolean _sellable;
    protected boolean _tradeable;
    protected boolean _dropable;
    protected int _crystalCount;
    protected String _crystalType;
    protected String _materialType;
    protected String _defaultAction; // "EQUIP", "SKILL_REDUCE", "PEEL", etc.
    
    /**
     * Constructor protegido que inicializa el ItemTemplate desde un StatSet.
     * 
     * <p>Extrae todos los campos comunes de items desde el StatSet parseado del XML.
     * 
     * @param set StatSet con los datos del item parseados del XML
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
        // bodypart siempre viene como string en los XMLs ("talisman", "brooch_jewel", "lbracelet", etc.)
        _bodyPart = set.getString("bodypart", "");
        _defaultAction = set.getString("default_action", "");
    }
    
    // Getters esenciales
    public int getId() {
        return _itemId;
    }
    
    public String getName() {
        return _name;
    }
    
    public String getIcon() {
        // Limpiar prefijos conocidos para retornar solo el nombre del archivo PNG
        if (_icon == null || _icon.isEmpty()) {
            return "";
        }
        
        // Remover prefijos y subcarpetas de todos los paquetes UTX
        // Patrón: Paquete.Subcarpeta.NombreIcono -> NombreIcono
        String cleanIcon = _icon;
        
        // BranchSys3: subcarpetas (Icon, icon, icon1, iconArmar, iconArmor, lcon)
        cleanIcon = cleanIcon.replaceFirst("^BranchSys3\\.Icon[A-Za-z]*\\.", "");
        cleanIcon = cleanIcon.replaceFirst("^BranchSys3\\.icon1\\.", "");
        cleanIcon = cleanIcon.replaceFirst("^BranchSys3\\.iconArmar\\.", "");
        cleanIcon = cleanIcon.replaceFirst("^BranchSys3\\.iconArmor\\.", "");
        cleanIcon = cleanIcon.replaceFirst("^BranchSys3\\.icon\\.", "");
        cleanIcon = cleanIcon.replaceFirst("^BranchSys3\\.lcon\\.", "");
        cleanIcon = cleanIcon.replaceFirst("^BranchSys3\\.", "");
        
        // BranchSys2: subcarpetas (Icon, icon, icon2, lcon)
        cleanIcon = cleanIcon.replaceFirst("^BranchSys2\\.Icon\\.", "");
        cleanIcon = cleanIcon.replaceFirst("^BranchSys2\\.icon2\\.", "");
        cleanIcon = cleanIcon.replaceFirst("^BranchSys2\\.icon\\.", "");
        cleanIcon = cleanIcon.replaceFirst("^BranchSys2\\.lcon\\.", "");
        cleanIcon = cleanIcon.replaceFirst("^BranchSys2\\.", "");
        
        // BranchSys: subcarpetas (Icon, icon)
        cleanIcon = cleanIcon.replaceFirst("^BranchSys\\.Icon\\.", "");
        cleanIcon = cleanIcon.replaceFirst("^BranchSys\\.icon\\.", "");
        cleanIcon = cleanIcon.replaceFirst("^BranchSys\\.", "");
        
        // BranchIcon: subcarpetas (Icon, icon)
        cleanIcon = cleanIcon.replaceFirst("^BranchIcon\\.Icon\\.", "");
        cleanIcon = cleanIcon.replaceFirst("^BranchIcon\\.icon\\.", "");
        cleanIcon = cleanIcon.replaceFirst("^BranchIcon\\.", "");
        
        // br_cashtex: subcarpeta (item)
        cleanIcon = cleanIcon.replaceFirst("^br_cashtex\\.item\\.", "");
        cleanIcon = cleanIcon.replaceFirst("^br_cashtex\\.", "");
        
        // icon: sin subcarpetas, solo remover prefijo
        cleanIcon = cleanIcon.replaceFirst("^icon\\.", "");
        
        // Convertir a lowercase para consistencia en sistemas case-sensitive
        return cleanIcon.toLowerCase();
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
    
    public String getBodyPart() {
        return _bodyPart != null ? _bodyPart : "";
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
    
    public String getDefaultAction() {
        return _defaultAction != null ? _defaultAction : "";
    }
    
    /**
     * Método abstracto para determinar el tipo específico de item.
     * 
     * <p>Cada subclase implementa este método para retornar su tipo: "Weapon", "Armor" o "EtcItem".
     * 
     * @return String con el tipo de item específico
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

