package com.ak4n1.terra.api.terra_api.game.dto;

/**
 * DTO simplificado para representar un skill de un personaje.
 * 
 * <p>Contiene información básica del skill y su nivel actual para el personaje.
 * Utilizado para transferir datos del backend al frontend.
 * 
 * @author ak4n1
 * @since 1.0
 */
public class SkillDTO {
    
    private int skillId;
    private String name;
    private String icon;
    private String operateType;  // "P" = Passive, "A1", "A2", etc = Active
    private int skillLevel;       // Nivel actual del skill que posee el personaje
    private int maxLevel;         // Nivel máximo disponible del skill
    private boolean passive;      // Si es pasivo o no
    private boolean isMagic;      // Si es skill mágico (isMagic=1)
    private boolean isDebuff;     // Si es debuff
    private boolean isToggle;     // Si es toggle (T, TG, AU)
    private boolean isTransform;  // Si es transformación
    private int classIndex;       // Índice de clase (0=base, 1-3=subclases)
    
    // Getters y setters
    
    public int getSkillId() {
        return skillId;
    }
    
    public void setSkillId(int skillId) {
        this.skillId = skillId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getIcon() {
        return icon;
    }
    
    public void setIcon(String icon) {
        this.icon = icon;
    }
    
    public String getOperateType() {
        return operateType;
    }
    
    public void setOperateType(String operateType) {
        this.operateType = operateType;
    }
    
    public int getSkillLevel() {
        return skillLevel;
    }
    
    public void setSkillLevel(int skillLevel) {
        this.skillLevel = skillLevel;
    }
    
    public int getMaxLevel() {
        return maxLevel;
    }
    
    public void setMaxLevel(int maxLevel) {
        this.maxLevel = maxLevel;
    }
    
    public boolean getPassive() {
        return passive;
    }
    
    public void setPassive(boolean passive) {
        this.passive = passive;
    }
    
    // Alias para compatibilidad
    public boolean isPassive() {
        return passive;
    }
    
    public boolean getIsMagic() {
        return isMagic;
    }
    
    public void setIsMagic(boolean isMagic) {
        this.isMagic = isMagic;
    }
    
    public boolean getIsDebuff() {
        return isDebuff;
    }
    
    public void setIsDebuff(boolean isDebuff) {
        this.isDebuff = isDebuff;
    }
    
    public boolean getIsToggle() {
        return isToggle;
    }
    
    public void setIsToggle(boolean isToggle) {
        this.isToggle = isToggle;
    }
    
    public boolean getIsTransform() {
        return isTransform;
    }
    
    public void setIsTransform(boolean isTransform) {
        this.isTransform = isTransform;
    }
    
    public int getClassIndex() {
        return classIndex;
    }
    
    public void setClassIndex(int classIndex) {
        this.classIndex = classIndex;
    }
    
    @Override
    public String toString() {
        return "SkillDTO{" +
                "skillId=" + skillId +
                ", name='" + name + '\'' +
                ", icon='" + icon + '\'' +
                ", operateType='" + operateType + '\'' +
                ", skillLevel=" + skillLevel +
                ", maxLevel=" + maxLevel +
                ", passive=" + passive +
                ", isMagic=" + isMagic +
                ", isDebuff=" + isDebuff +
                ", isToggle=" + isToggle +
                ", isTransform=" + isTransform +
                ", classIndex=" + classIndex +
                '}';
    }
}
