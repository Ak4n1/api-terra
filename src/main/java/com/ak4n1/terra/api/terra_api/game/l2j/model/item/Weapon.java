package com.ak4n1.terra.api.terra_api.game.l2j.model.item;

import com.ak4n1.terra.api.terra_api.game.l2j.model.StatSet;

/**
 * Representa un arma del juego
 */
public class Weapon extends ItemTemplate {
    
    private String _weaponType; // "SWORD", "BOW", "DAGGER", etc.
    private int _pAtk;
    private int _mAtk;
    private int _soulshots;
    private int _spiritshots;
    
    public Weapon(StatSet set) {
        super(set);
        _type = "Weapon";
        _grade = set.getString("crystal_type", "none").toUpperCase();
        _weaponType = set.getString("weapon_type", "NONE");
        _pAtk = set.getInt("pAtk", 0);
        _mAtk = set.getInt("mAtk", 0);
        _soulshots = set.getInt("soulshots", 0);
        _spiritshots = set.getInt("spiritshots", 0);
    }
    
    @Override
    public String getItemType() {
        return "Weapon";
    }
    
    public String getWeaponType() {
        return _weaponType;
    }
    
    public int getPAtk() {
        return _pAtk;
    }
    
    public int getMAtk() {
        return _mAtk;
    }
    
    public int getSoulshots() {
        return _soulshots;
    }
    
    public int getSpiritshots() {
        return _spiritshots;
    }
}

