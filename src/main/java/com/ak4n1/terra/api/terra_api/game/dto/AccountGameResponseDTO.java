package com.ak4n1.terra.api.terra_api.game.dto;


import java.sql.Timestamp;

public class AccountGameResponseDTO {

    private String login;
    private String email;
    private Timestamp createdTime;
    private Long lastActive;
    private String lastIP;
    private byte lastServer;
    private String pcIp;

    // Constructor vacío
    public AccountGameResponseDTO() {}

    // Constructor con todos los campos (sin password ni accessLevel)
    public AccountGameResponseDTO(String login, String email, Timestamp createdTime, Long lastActive,
                                  String lastIP, byte lastServer, String pcIp) {
        this.login = login;
        this.email = email;
        this.createdTime = createdTime;
        this.lastActive = lastActive;
        this.lastIP = lastIP;
        this.lastServer = lastServer;
        this.pcIp = pcIp;
    }

    // Getters y setters
    public String getLogin() {
        return login;
    }
    public void setLogin(String login) {
        this.login = login;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public Timestamp getCreatedTime() {
        return createdTime;
    }
    public void setCreatedTime(Timestamp createdTime) {
        this.createdTime = createdTime;
    }

    public Long getLastActive() {
        return lastActive;
    }
    public void setLastActive(Long lastActive) {
        this.lastActive = lastActive;
    }

    public String getLastIP() {
        return lastIP;
    }
    public void setLastIP(String lastIP) {
        this.lastIP = lastIP;
    }

    public byte getLastServer() {
        return lastServer;
    }
    public void setLastServer(byte lastServer) {
        this.lastServer = lastServer;
    }

    public String getPcIp() {
        return pcIp;
    }
    public void setPcIp(String pcIp) {
        this.pcIp = pcIp;
    }
}
