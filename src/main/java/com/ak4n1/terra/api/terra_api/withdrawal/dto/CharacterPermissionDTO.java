package com.ak4n1.terra.api.terra_api.withdrawal.dto;

/**
 * DTO para mostrar personajes con su estado de permiso de retiro.
 * 
 * @author ak4n1
 * @since 1.0
 */
public class CharacterPermissionDTO {

    private Integer characterId;
    private String characterName;
    private Integer level;
    private Integer classId;
    private String className;
    private boolean hasPermission;

    // Constructors
    public CharacterPermissionDTO() {
    }

    public CharacterPermissionDTO(Integer characterId, String characterName, Integer level, 
                                   Integer classId, String className, boolean hasPermission) {
        this.characterId = characterId;
        this.characterName = characterName;
        this.level = level;
        this.classId = classId;
        this.className = className;
        this.hasPermission = hasPermission;
    }

    // Getters and Setters
    public Integer getCharacterId() {
        return characterId;
    }

    public void setCharacterId(Integer characterId) {
        this.characterId = characterId;
    }

    public String getCharacterName() {
        return characterName;
    }

    public void setCharacterName(String characterName) {
        this.characterName = characterName;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Integer getClassId() {
        return classId;
    }

    public void setClassId(Integer classId) {
        this.classId = classId;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public boolean isHasPermission() {
        return hasPermission;
    }

    public void setHasPermission(boolean hasPermission) {
        this.hasPermission = hasPermission;
    }
}

