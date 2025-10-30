package com.ak4n1.terra.api.terra_api.game.l2j.model.item;

import com.ak4n1.terra.api.terra_api.game.l2j.model.StatSet;

/**
 * Representa items misceláneos del juego L2J (consumibles, quest items, etc.).
 * 
 * <p>Subclase de ItemTemplate que contiene información específica de items misceláneos,
 * incluyendo tipo de item y si es un quest item.
 * 
 * @see ItemTemplate
 * @see StatSet
 * @author ak4n1
 * @since 1.0
 */
public class EtcItem extends ItemTemplate {
    
    private String _etcItemType; // "POTION", "SCROLL", "RECIPE", etc.
    private boolean _isQuestItem;
    
    public EtcItem(StatSet set) {
        super(set);
        _type = "EtcItem";
        _grade = set.getString("crystal_type", "none").toUpperCase();
        _etcItemType = set.getString("etcitem_type", "NONE");
        _isQuestItem = set.getBoolean("is_quest_item", false);
    }
    
    @Override
    public String getItemType() {
        return "EtcItem";
    }
    
    public String getEtcItemType() {
        return _etcItemType;
    }
    
    public boolean isQuestItem() {
        return _isQuestItem;
    }
}

