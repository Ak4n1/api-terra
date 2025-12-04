package com.ak4n1.terra.api.terra_api.withdrawal.dto;

/**
 * DTO para solicitar cambios de permisos de retiro.
 * 
 * @author ak4n1
 * @since 1.0
 */
public class PermissionRequestDTO {

    private Integer characterId;
    private String characterName;

    // Constructors
    public PermissionRequestDTO() {
    }

    public PermissionRequestDTO(Integer characterId, String characterName) {
        this.characterId = characterId;
        this.characterName = characterName;
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
}

