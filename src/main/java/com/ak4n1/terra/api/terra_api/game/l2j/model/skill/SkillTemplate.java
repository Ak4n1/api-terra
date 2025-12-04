package com.ak4n1.terra.api.terra_api.game.l2j.model.skill;

import com.ak4n1.terra.api.terra_api.game.l2j.model.StatSet;

/**
 * Modelo simplificado para representar un skill del catálogo XML.
 * 
 * <p>Contiene los campos esenciales necesarios para la API web.
 * Los skills se cargan desde archivos XML y se consultan para obtener
 * información como nombre, icono y tipo (active/passive).
 * 
 * @see StatSet
 * @author ak4n1
 * @since 1.0
 */
public class SkillTemplate {
    
    private int _skillId;
    private String _name;
    private String _icon;
    private String _operateType;  // "P" = Passive, "A1", "A2", etc = Active
    private int _toLevel;          // Nivel máximo del skill
    private int _isMagic;          // 0 = Physical, 1 = Magic, 2 = Static, 3 = Dance
    private boolean _isDebuff;     // Si es debuff o no
    
    /**
     * Constructor que inicializa el SkillTemplate desde un StatSet.
     * 
     * @param set StatSet con los datos del skill parseados del XML
     */
    public SkillTemplate(StatSet set) {
        _skillId = set.getInt("skill_id");
        _name = set.getString("name", "Unknown");
        _icon = set.getString("icon", "");
        _operateType = set.getString("operate_type", "");
        _toLevel = set.getInt("to_level", 1);
        _isMagic = set.getInt("is_magic", 0);
        _isDebuff = set.getBoolean("is_debuff", false);
    }
    
    // Getters
    
    public int getId() {
        return _skillId;
    }
    
    public String getName() {
        return _name;
    }
    
    /**
     * Retorna el nombre del icono limpio (sin prefijos UTX).
     * 
     * <p>Los iconos de skills vienen con prefijos como "icon.", "BranchSys2.icon.", etc.
     * Este método limpia todos los prefijos conocidos para retornar solo el nombre del archivo PNG.
     * 
     * @return String con el nombre del icono sin prefijos en lowercase
     */
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
    
    /**
     * Retorna el tipo de operación del skill.
     * 
     * <p>Tipos comunes:
     * <ul>
     *   <li>"P" = Passive (pasivo, siempre activo)</li>
     *   <li>"A1", "A2", etc = Active (activo, requiere click)</li>
     * </ul>
     * 
     * @return String con el tipo de operación
     */
    public String getOperateType() {
        return _operateType;
    }
    
    /**
     * Verifica si el skill es pasivo.
     * 
     * @return true si el skill es pasivo ("P"), false en caso contrario
     */
    public boolean isPassive() {
        return "P".equalsIgnoreCase(_operateType);
    }
    
    /**
     * Verifica si el skill es activo.
     * 
     * @return true si el skill es activo (empieza con "A"), false en caso contrario
     */
    public boolean isActive() {
        return _operateType != null && _operateType.startsWith("A");
    }
    
    /**
     * Verifica si el skill es toggle.
     * 
     * @return true si el skill es toggle (T, TG, AU), false en caso contrario
     */
    public boolean isToggle() {
        return _operateType != null && (_operateType.equals("T") || _operateType.equals("TG") || _operateType.equals("AU"));
    }
    
    /**
     * Verifica si el skill es transformación.
     * 
     * @return true si el skill es de transformación, false en caso contrario
     */
    public boolean isTransform() {
        return _operateType != null && _operateType.equals("T");
    }
    
    public int getIsMagic() {
        return _isMagic;
    }
    
    public boolean isMagic() {
        return _isMagic == 1;
    }
    
    public boolean isDebuff() {
        return _isDebuff;
    }
    
    public int getToLevel() {
        return _toLevel;
    }
    
    @Override
    public String toString() {
        return "SkillTemplate{" +
                "id=" + _skillId +
                ", name='" + _name + '\'' +
                ", icon='" + getIcon() + '\'' +
                ", operateType='" + _operateType + '\'' +
                ", toLevel=" + _toLevel +
                ", isMagic=" + _isMagic +
                ", isDebuff=" + _isDebuff +
                '}';
    }
}
