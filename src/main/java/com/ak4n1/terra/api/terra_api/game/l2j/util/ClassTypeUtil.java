package com.ak4n1.terra.api.terra_api.game.l2j.util;

import java.util.HashSet;
import java.util.Set;

/**
 * Utilidad para determinar el tipo de clase (Soldier/Warrior o Magician).
 * 
 * <p>Basado en la lógica del core de L2J Mobius, donde cada clase tiene
 * un flag `isMage` que indica si es una clase maga o una clase guerrera.
 * 
 * <p>Contiene TODAS las clases magician (isMage = true) y warrior (isMage = false)
 * del enum ClassId.java del core de L2J Mobius Classic 3.0.
 * 
 * @author ak4n1
 * @since 1.0
 */
public class ClassTypeUtil {
    
    /**
     * Set con TODOS los IDs de clases que son magicians (isMage = true).
     * Basado en ClassId.java del core de L2J Mobius Classic 3.0.
     * Incluye todas las clases base y third classes.
     */
    private static final Set<Integer> MAGE_CLASS_IDS = new HashSet<>();
    
    /**
     * Set con TODOS los IDs de clases que son warriors/soldiers (isMage = false).
     * Basado en ClassId.java del core de L2J Mobius Classic 3.0.
     * Incluye todas las clases base y third classes.
     */
    private static final Set<Integer> WARRIOR_CLASS_IDS = new HashSet<>();
    
    static {
        // Todas las clases magician del core L2J Mobius Classic 3.0
        // Extraídas directamente del enum ClassId.java donde isMage = true
        
        // Human Mages (Base Classes)
        MAGE_CLASS_IDS.add(10);  // MAGE
        MAGE_CLASS_IDS.add(11);  // WIZARD
        MAGE_CLASS_IDS.add(12);  // SORCERER
        MAGE_CLASS_IDS.add(13);  // NECROMANCER
        MAGE_CLASS_IDS.add(14);  // WARLOCK
        MAGE_CLASS_IDS.add(15);  // CLERIC
        MAGE_CLASS_IDS.add(16);  // BISHOP
        MAGE_CLASS_IDS.add(17);  // PROPHET
        
        // Elven Mages (Base Classes)
        MAGE_CLASS_IDS.add(25);  // ELVEN_MAGE
        MAGE_CLASS_IDS.add(26);  // ELVEN_WIZARD
        MAGE_CLASS_IDS.add(27);  // SPELLSINGER
        MAGE_CLASS_IDS.add(28);  // ELEMENTAL_SUMMONER
        MAGE_CLASS_IDS.add(29);  // ORACLE
        MAGE_CLASS_IDS.add(30);  // ELDER
        
        // Dark Elf Mages (Base Classes)
        MAGE_CLASS_IDS.add(38);  // DARK_MAGE
        MAGE_CLASS_IDS.add(39);  // DARK_WIZARD
        MAGE_CLASS_IDS.add(40);  // SPELLHOWLER
        MAGE_CLASS_IDS.add(41);  // PHANTOM_SUMMONER
        MAGE_CLASS_IDS.add(42);  // SHILLIEN_ORACLE
        MAGE_CLASS_IDS.add(43);  // SHILLIEN_ELDER
        
        // Orc Mages (Base Classes)
        MAGE_CLASS_IDS.add(49);  // ORC_MAGE
        MAGE_CLASS_IDS.add(50);  // ORC_SHAMAN
        MAGE_CLASS_IDS.add(51);  // OVERLORD
        MAGE_CLASS_IDS.add(52);  // WARCRYER
        
        // Third Class Mages - Human
        MAGE_CLASS_IDS.add(94);  // ARCHMAGE
        MAGE_CLASS_IDS.add(95);  // SOULTAKER
        MAGE_CLASS_IDS.add(96);  // ARCANA_LORD
        MAGE_CLASS_IDS.add(97);  // CARDINAL
        MAGE_CLASS_IDS.add(98);  // HIEROPHANT
        
        // Third Class Mages - Elven
        MAGE_CLASS_IDS.add(103); // MYSTIC_MUSE
        MAGE_CLASS_IDS.add(104); // ELEMENTAL_MASTER
        MAGE_CLASS_IDS.add(105); // EVA_SAINT
        
        // Third Class Mages - Dark Elf
        MAGE_CLASS_IDS.add(110); // STORM_SCREAMER
        MAGE_CLASS_IDS.add(111); // SPECTRAL_MASTER
        MAGE_CLASS_IDS.add(112); // SHILLIEN_SAINT
        
        // Third Class Mages - Orc
        MAGE_CLASS_IDS.add(115); // DOMINATOR
        MAGE_CLASS_IDS.add(116); // DOOMCRYER
        
        // Todas las clases warrior/soldier del core L2J Mobius Classic 3.0
        // Extraídas directamente del enum ClassId.java donde isMage = false
        
        // Human Warriors (Base Classes)
        WARRIOR_CLASS_IDS.add(0);   // FIGHTER
        WARRIOR_CLASS_IDS.add(1);   // WARRIOR
        WARRIOR_CLASS_IDS.add(2);   // GLADIATOR
        WARRIOR_CLASS_IDS.add(3);   // WARLORD
        WARRIOR_CLASS_IDS.add(4);   // KNIGHT
        WARRIOR_CLASS_IDS.add(5);   // PALADIN
        WARRIOR_CLASS_IDS.add(6);   // DARK_AVENGER
        WARRIOR_CLASS_IDS.add(7);   // ROGUE
        WARRIOR_CLASS_IDS.add(8);   // TREASURE_HUNTER
        WARRIOR_CLASS_IDS.add(9);   // HAWKEYE
        
        // Elven Warriors (Base Classes)
        WARRIOR_CLASS_IDS.add(18);  // ELVEN_FIGHTER
        WARRIOR_CLASS_IDS.add(19);  // ELVEN_KNIGHT
        WARRIOR_CLASS_IDS.add(20);  // TEMPLE_KNIGHT
        WARRIOR_CLASS_IDS.add(21);  // SWORDSINGER
        WARRIOR_CLASS_IDS.add(22);  // ELVEN_SCOUT
        WARRIOR_CLASS_IDS.add(23);  // PLAINS_WALKER
        WARRIOR_CLASS_IDS.add(24);  // SILVER_RANGER
        
        // Dark Elf Warriors (Base Classes)
        WARRIOR_CLASS_IDS.add(31);  // DARK_FIGHTER
        WARRIOR_CLASS_IDS.add(32);  // PALUS_KNIGHT
        WARRIOR_CLASS_IDS.add(33);  // SHILLIEN_KNIGHT
        WARRIOR_CLASS_IDS.add(34);  // BLADEDANCER
        WARRIOR_CLASS_IDS.add(35);  // ASSASSIN
        WARRIOR_CLASS_IDS.add(36);  // ABYSS_WALKER
        WARRIOR_CLASS_IDS.add(37);  // PHANTOM_RANGER
        
        // Orc Warriors (Base Classes)
        WARRIOR_CLASS_IDS.add(44);  // ORC_FIGHTER
        WARRIOR_CLASS_IDS.add(45);  // ORC_RAIDER
        WARRIOR_CLASS_IDS.add(46);  // DESTROYER
        WARRIOR_CLASS_IDS.add(47);  // ORC_MONK
        WARRIOR_CLASS_IDS.add(48);  // TYRANT
        
        // Dwarf Warriors (Base Classes)
        WARRIOR_CLASS_IDS.add(53);  // DWARVEN_FIGHTER
        WARRIOR_CLASS_IDS.add(54);  // SCAVENGER
        WARRIOR_CLASS_IDS.add(55);  // BOUNTY_HUNTER
        WARRIOR_CLASS_IDS.add(56);  // ARTISAN
        WARRIOR_CLASS_IDS.add(57);  // WARSMITH
        
        // Third Class Warriors - Human
        WARRIOR_CLASS_IDS.add(88);  // DUELIST
        WARRIOR_CLASS_IDS.add(89);  // DREADNOUGHT
        WARRIOR_CLASS_IDS.add(90);  // PHOENIX_KNIGHT
        WARRIOR_CLASS_IDS.add(91);  // HELL_KNIGHT
        WARRIOR_CLASS_IDS.add(92);  // SAGITTARIUS
        WARRIOR_CLASS_IDS.add(93);  // ADVENTURER
        
        // Third Class Warriors - Elven
        WARRIOR_CLASS_IDS.add(99);  // EVA_TEMPLAR
        WARRIOR_CLASS_IDS.add(100); // SWORD_MUSE
        WARRIOR_CLASS_IDS.add(101); // WIND_RIDER
        WARRIOR_CLASS_IDS.add(102); // MOONLIGHT_SENTINEL
        
        // Third Class Warriors - Dark Elf
        WARRIOR_CLASS_IDS.add(106); // SHILLIEN_TEMPLAR
        WARRIOR_CLASS_IDS.add(107); // SPECTRAL_DANCER
        WARRIOR_CLASS_IDS.add(108); // GHOST_HUNTER
        WARRIOR_CLASS_IDS.add(109); // GHOST_SENTINEL
        
        // Third Class Warriors - Orc
        WARRIOR_CLASS_IDS.add(113); // TITAN
        WARRIOR_CLASS_IDS.add(114); // GRAND_KHAVATARI
        
        // Third Class Warriors - Dwarf
        WARRIOR_CLASS_IDS.add(117); // FORTUNE_SEEKER
        WARRIOR_CLASS_IDS.add(118); // MAESTRO
        
        // Kamael Warriors (Base Classes)
        WARRIOR_CLASS_IDS.add(192); // KAMAEL_SOLDIER
        WARRIOR_CLASS_IDS.add(125); // TROOPER
        WARRIOR_CLASS_IDS.add(193); // SOUL_FINDER
        WARRIOR_CLASS_IDS.add(126); // WARDER
        
        // Third Class Warriors - Kamael
        WARRIOR_CLASS_IDS.add(127); // BERSERKER
        WARRIOR_CLASS_IDS.add(194); // SOUL_BREAKER
        WARRIOR_CLASS_IDS.add(130); // SOUL_RANGER
        WARRIOR_CLASS_IDS.add(131); // DOOMBRINGER
        WARRIOR_CLASS_IDS.add(195); // SOUL_HOUND
        WARRIOR_CLASS_IDS.add(134); // TRICKSTER
    }
    
    /**
     * Determina si una clase es magician.
     * 
     * <p>Verifica si el classId está en el set de clases magician.
     * Basado en el flag `isMage` del enum ClassId del core de L2J.
     * 
     * @param classId ID de la clase (puede ser baseClass o classid)
     * @return true si es magician, false en caso contrario
     */
    public static boolean isMage(Integer classId) {
        if (classId == null) {
            return false;
        }
        return MAGE_CLASS_IDS.contains(classId);
    }
    
    /**
     * Determina si una clase es warrior/soldier.
     * 
     * <p>Verifica si el classId está en el set de clases warrior.
     * Basado en el flag `isMage = false` del enum ClassId del core de L2J.
     * 
     * @param classId ID de la clase (puede ser baseClass o classid)
     * @return true si es warrior/soldier, false en caso contrario
     */
    public static boolean isWarrior(Integer classId) {
        if (classId == null) {
            return false;
        }
        return WARRIOR_CLASS_IDS.contains(classId);
    }
    
    /**
     * Obtiene el tipo de clase como string.
     * 
     * <p>Verifica explícitamente si es mage o warrior. Si no está en ninguna
     * lista, retorna null.
     * 
     * @param classId ID de la clase (puede ser baseClass o classid)
     * @return "magician" si es mage, "soldier" si es warrior, null si no está en ninguna lista
     */
    public static String getClassType(Integer classId) {
        if (classId == null) {
            return null;
        }
        if (isMage(classId)) {
            return "magician";
        }
        if (isWarrior(classId)) {
            return "soldier";
        }
        return null; // Clase desconocida
    }
}

