package com.ak4n1.terra.api.terra_api.auth.dto;

import java.sql.Timestamp;

/**
 * DTO para información de actividad reciente de un usuario.
 * 
 * @author ak4n1
 * @since 1.0
 */
public class RecentActivityDTO {
    private String action;
    private Timestamp timestamp;
    private String ip;

    public RecentActivityDTO(String action, Timestamp timestamp, String ip) {
        this.action = action;
        this.timestamp = timestamp;
        this.ip = ip;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
}
