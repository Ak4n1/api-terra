package com.ak4n1.terra.api.terra_api.game.l2j.model.item;

import com.ak4n1.terra.api.terra_api.game.l2j.model.StatSet;

/**
 * Representa una armadura del juego L2J.
 * 
 * <p>Subclase de ItemTemplate que contiene información específica de armaduras,
 * incluyendo tipo de armadura (ligera, pesada, mágica, sigilo) y defensa física/mágica.
 * 
 * @see ItemTemplate
 * @see StatSet
 * @author ak4n1
 * @since 1.0
 */
public class Armor extends ItemTemplate {
    
    private String _armorType; // "LIGHT", "HEAVY", "MAGIC", "SIGIL"
    private int _pDef;
    private int _mDef;
    
    public Armor(StatSet set) {
        super(set);
        _type = "Armor";
        _grade = set.getString("crystal_type", "none").toUpperCase();
        _armorType = set.getString("armor_type", "NONE");
        _pDef = set.getInt("pDef", 0);
        _mDef = set.getInt("mDef", 0);
    }
    
    @Override
    public String getItemType() {
        return "Armor";
    }
    
    public String getArmorType() {
        return _armorType;
    }
    
    public int getPDef() {
        return _pDef;
    }
    
    public int getMDef() {
        return _mDef;
    }
}

