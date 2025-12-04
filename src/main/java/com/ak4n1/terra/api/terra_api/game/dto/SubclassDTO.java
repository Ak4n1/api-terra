package com.ak4n1.terra.api.terra_api.game.dto;

/**
 * DTO para representar una subclase de un personaje.
 * 
 * <p>Contiene información básica de la subclase incluido el class_id y class_index.
 * Utilizado para transferir datos del backend al frontend.
 * 
 * @author ak4n1
 * @since 1.0
 */
public class SubclassDTO {
    
    private int classId;
    private int classIndex;
    private long exp;
    private long sp;
    private int level;
    private int vitalityPoints;
    private boolean dualClass;
    private String expPercent; 
    
    // Getters y setters
    
    public int getClassId() {
        return classId;
    }
    
    public void setClassId(int classId) {
        this.classId = classId;
    }
    
    public int getClassIndex() {
        return classIndex;
    }
    
    public void setClassIndex(int classIndex) {
        this.classIndex = classIndex;
    }
    
    public long getExp() {
        return exp;
    }
    
    public void setExp(long exp) {
        this.exp = exp;
    }
    
    public long getSp() {
        return sp;
    }
    
    public void setSp(long sp) {
        this.sp = sp;
    }
    
    public int getLevel() {
        return level;
    }
    
    public void setLevel(int level) {
        this.level = level;
    }
    
    public int getVitalityPoints() {
        return vitalityPoints;
    }
    
    public void setVitalityPoints(int vitalityPoints) {
        this.vitalityPoints = vitalityPoints;
    }
    
    public boolean isDualClass() {
        return dualClass;
    }
    
    public void setDualClass(boolean dualClass) {
        this.dualClass = dualClass;
    }
    
    public String getExpPercent() {
        return expPercent;
    }
    
    public void setExpPercent(String expPercent) {
        this.expPercent = expPercent;
    }
    
    @Override
    public String toString() {
        return "SubclassDTO{" +
                "classId=" + classId +
                ", classIndex=" + classIndex +
                ", exp=" + exp +
                ", sp=" + sp +
                ", level=" + level +
                ", vitalityPoints=" + vitalityPoints +
                ", dualClass=" + dualClass +
                '}';
    }
}

