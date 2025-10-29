package com.ak4n1.terra.api.terra_api.game.l2j.model.item;

import com.ak4n1.terra.api.terra_api.game.l2j.model.StatSet;

/**
 * Representa items misceláneos (consumibles, quest items, etc.)
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

