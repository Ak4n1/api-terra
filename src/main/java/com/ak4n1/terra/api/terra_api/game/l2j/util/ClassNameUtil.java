package com.ak4n1.terra.api.terra_api.game.l2j.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Utilidad para obtener nombres de clases por ID.
 * 
 * <p>Basado en el enum ClassId del core de L2J Mobius Classic 3.0.
 * Sincronizado con ClassTypeUtil para tener todas las clases.
 * 
 * @author ak4n1
 * @since 1.0
 */
public class ClassNameUtil {
    
    private static final Map<Integer, String> CLASS_NAMES = new HashMap<>();
    
    static {
        // ========================================
        // Human Warriors (Base Classes)
        // ========================================
        CLASS_NAMES.put(0, "Human Fighter");
        CLASS_NAMES.put(1, "Warrior");
        CLASS_NAMES.put(2, "Gladiator");
        CLASS_NAMES.put(3, "Warlord");
        CLASS_NAMES.put(4, "Human Knight");
        CLASS_NAMES.put(5, "Paladin");
        CLASS_NAMES.put(6, "Dark Avenger");
        CLASS_NAMES.put(7, "Rogue");
        CLASS_NAMES.put(8, "Treasure Hunter");
        CLASS_NAMES.put(9, "Hawkeye");
        
        // ========================================
        // Human Mages (Base Classes)
        // ========================================
        CLASS_NAMES.put(10, "Human Mystic");
        CLASS_NAMES.put(11, "Human Wizard");
        CLASS_NAMES.put(12, "Sorcerer");
        CLASS_NAMES.put(13, "Necromancer");
        CLASS_NAMES.put(14, "Warlock");
        CLASS_NAMES.put(15, "Cleric");
        CLASS_NAMES.put(16, "Bishop");
        CLASS_NAMES.put(17, "Prophet");
        
        // ========================================
        // Elven Warriors (Base Classes)
        // ========================================
        CLASS_NAMES.put(18, "Elven Fighter");
        CLASS_NAMES.put(19, "Elven Knight");
        CLASS_NAMES.put(20, "Temple Knight");
        CLASS_NAMES.put(21, "Sword Singer");
        CLASS_NAMES.put(22, "Elven Scout");
        CLASS_NAMES.put(23, "Plains Walker");
        CLASS_NAMES.put(24, "Silver Ranger");
        
        // ========================================
        // Elven Mages (Base Classes)
        // ========================================
        CLASS_NAMES.put(25, "Elven Mystic");
        CLASS_NAMES.put(26, "Elven Wizard");
        CLASS_NAMES.put(27, "Spellsinger");
        CLASS_NAMES.put(28, "Elemental Summoner");
        CLASS_NAMES.put(29, "Elven Oracle");
        CLASS_NAMES.put(30, "Elven Elder");
        
        // ========================================
        // Dark Elf Warriors (Base Classes)
        // ========================================
        CLASS_NAMES.put(31, "Dark Fighter");
        CLASS_NAMES.put(32, "Palus Knight");
        CLASS_NAMES.put(33, "Shillien Knight");
        CLASS_NAMES.put(34, "Bladedancer");
        CLASS_NAMES.put(35, "Assassin");
        CLASS_NAMES.put(36, "Abyss Walker");
        CLASS_NAMES.put(37, "Phantom Ranger");
        
        // ========================================
        // Dark Elf Mages (Base Classes)
        // ========================================
        CLASS_NAMES.put(38, "Dark Mystic");
        CLASS_NAMES.put(39, "Dark Wizard");
        CLASS_NAMES.put(40, "Spellhowler");
        CLASS_NAMES.put(41, "Phantom Summoner");
        CLASS_NAMES.put(42, "Shillien Oracle");
        CLASS_NAMES.put(43, "Shillien Elder");
        
        // ========================================
        // Orc Warriors (Base Classes)
        // ========================================
        CLASS_NAMES.put(44, "Orc Fighter");
        CLASS_NAMES.put(45, "Orc Raider");
        CLASS_NAMES.put(46, "Destroyer");
        CLASS_NAMES.put(47, "Monk");
        CLASS_NAMES.put(48, "Tyrant");
        
        // ========================================
        // Orc Mages (Base Classes)
        // ========================================
        CLASS_NAMES.put(49, "Orc Mystic");
        CLASS_NAMES.put(50, "Orc Shaman");
        CLASS_NAMES.put(51, "Overlord");
        CLASS_NAMES.put(52, "Warcryer");
        
        // ========================================
        // Dwarf Warriors (Base Classes)
        // ========================================
        CLASS_NAMES.put(53, "Dwarf Fighter");
        CLASS_NAMES.put(54, "Scavenger");
        CLASS_NAMES.put(55, "Bounty Hunter");
        CLASS_NAMES.put(56, "Artisan");
        CLASS_NAMES.put(57, "Warsmith");
        
        // ========================================
        // Third Class Warriors - Human
        // ========================================
        CLASS_NAMES.put(88, "Duelist");
        CLASS_NAMES.put(89, "Dreadnought");
        CLASS_NAMES.put(90, "Phoenix Knight");
        CLASS_NAMES.put(91, "Hell Knight");
        CLASS_NAMES.put(92, "Sagittarius");
        CLASS_NAMES.put(93, "Adventurer");
        
        // ========================================
        // Third Class Mages - Human
        // ========================================
        CLASS_NAMES.put(94, "Archmage");
        CLASS_NAMES.put(95, "Soultaker");
        CLASS_NAMES.put(96, "Arcana Lord");
        CLASS_NAMES.put(97, "Cardinal");
        CLASS_NAMES.put(98, "Hierophant");
        
        // ========================================
        // Third Class Warriors - Elven
        // ========================================
        CLASS_NAMES.put(99, "Eva's Templar");
        CLASS_NAMES.put(100, "Sword Muse");
        CLASS_NAMES.put(101, "Wind Rider");
        CLASS_NAMES.put(102, "Moonlight Sentinel");
        
        // ========================================
        // Third Class Mages - Elven
        // ========================================
        CLASS_NAMES.put(103, "Mystic Muse");
        CLASS_NAMES.put(104, "Elemental Master");
        CLASS_NAMES.put(105, "Eva's Saint");
        
        // ========================================
        // Third Class Warriors - Dark Elf
        // ========================================
        CLASS_NAMES.put(106, "Shillien Templar");
        CLASS_NAMES.put(107, "Spectral Dancer");
        CLASS_NAMES.put(108, "Ghost Hunter");
        CLASS_NAMES.put(109, "Ghost Sentinel");
        
        // ========================================
        // Third Class Mages - Dark Elf
        // ========================================
        CLASS_NAMES.put(110, "Storm Screamer");
        CLASS_NAMES.put(111, "Spectral Master");
        CLASS_NAMES.put(112, "Shillien Saint");
        
        // ========================================
        // Third Class Warriors - Orc
        // ========================================
        CLASS_NAMES.put(113, "Titan");
        CLASS_NAMES.put(114, "Grand Khavatari");
        
        // ========================================
        // Third Class Mages - Orc
        // ========================================
        CLASS_NAMES.put(115, "Dominator");
        CLASS_NAMES.put(116, "Doom Cryer");
        
        // ========================================
        // Third Class Warriors - Dwarf
        // ========================================
        CLASS_NAMES.put(117, "Fortune Seeker");
        CLASS_NAMES.put(118, "Maestro");
        
        // ========================================
        // Kamael Warriors (Base Classes)
        // ========================================
        CLASS_NAMES.put(192, "Jin Kamael Soldier");
        CLASS_NAMES.put(125, "Trooper");
        CLASS_NAMES.put(193, "Soul Finder");
        CLASS_NAMES.put(126, "Warden");
        
        // ========================================
        // Third Class Warriors - Kamael
        // ========================================
        CLASS_NAMES.put(127, "Berserker");
        CLASS_NAMES.put(194, "Soul Breaker");
        CLASS_NAMES.put(130, "Arbalester");
        CLASS_NAMES.put(131, "Doombringer");
        CLASS_NAMES.put(195, "Soul Hound");
        CLASS_NAMES.put(134, "Trickster");
        CLASS_NAMES.put(135, "Inspector");
        CLASS_NAMES.put(136, "Judicator");
    }
    
    /**
     * Obtiene el nombre de una clase por su ID.
     * 
     * @param classId ID de la clase
     * @return Nombre de la clase o "Unknown" si no existe
     */
    public static String getClassName(Integer classId) {
        if (classId == null) {
            return "Unknown";
        }
        return CLASS_NAMES.getOrDefault(classId, "Unknown (" + classId + ")");
    }
}
