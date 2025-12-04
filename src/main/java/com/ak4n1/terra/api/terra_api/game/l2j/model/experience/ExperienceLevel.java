package com.ak4n1.terra.api.terra_api.game.l2j.model.experience;

/**
 * Modelo para representar un nivel de experiencia.
 * 
 * @author ak4n1
 * @since 1.0
 */
public class ExperienceLevel {
    private int level;
    private long toLevel; // Exp total necesario para alcanzar este nivel
    
    public ExperienceLevel(int level, long toLevel) {
        this.level = level;
        this.toLevel = toLevel;
    }
    
    public int getLevel() {
        return level;
    }
    
    public long getToLevel() {
        return toLevel;
    }
}

