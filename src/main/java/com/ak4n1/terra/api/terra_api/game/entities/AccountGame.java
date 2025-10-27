package com.ak4n1.terra.api.terra_api.game.entities;

import jakarta.persistence.*;
import java.sql.Timestamp;
import java.util.Date;

@Entity
@Table(name = "accounts")
public class AccountGame {


    public AccountGame() {
        this.createdTime = new Timestamp(System.currentTimeMillis());
        this.lastActive = System.currentTimeMillis();
        this.accessLevel = 0;
        this.lastServer = 1;
    }

    @Id
    @Column(name = "login" )
    private String login;

    @Column(name = "password" )
    private String password;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "created_time")
    private Timestamp createdTime;

    @Column(name = "lastactive")
    private Long lastActive;

    @Column(name = "accessLevel")
    private byte accessLevel;

    @Column(name = "lastIP")
    private String lastIP;

    @Column(name = "lastServer")
    private byte lastServer;

    @Column(name = "pcIp")
    private String pcIp;

    @Column(name = "reset_code")
    private String resetCode;

    @Column(name = "reset_expire")
    private Timestamp resetExpire;

    @Column(name = "create_code")
    private String createCode;

    @Column(name = "create_code_expire")
    private Timestamp createCodeExpire;


    public String getCreateCode() {
        return createCode;
    }

    public void setCreateCode(String createCode) {
        this.createCode = createCode;
    }

    public Timestamp getCreateCodeExpire() {
        return createCodeExpire;
    }

    public void setCreateCodeExpire(Timestamp createCodeExpire) {
        this.createCodeExpire = createCodeExpire;
    }

    public Timestamp getResetExpire() {
        return resetExpire;
    }

    public void setResetExpire(Timestamp resetExpire) {
        this.resetExpire = resetExpire;
    }

    public String getResetCode() {
        return resetCode;
    }

    public void setResetCode(String resetCode) {
        this.resetCode = resetCode;
    }

    public byte getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(byte accessLevel) {
        this.accessLevel = accessLevel;
    }

    public Timestamp getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Timestamp createdTime) {
        this.createdTime = createdTime;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLastIP() {
        return lastIP;
    }

    public void setLastIP(String lastIP) {
        this.lastIP = lastIP;
    }

    public Long getLastActive() {
        return lastActive;
    }

    public void setLastActive(Long lastActive) {
        this.lastActive = lastActive;
    }

    public byte getLastServer() {
        return lastServer;
    }

    public void setLastServer(byte lastServer) {
        this.lastServer = lastServer;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPcIp() {
        return pcIp;
    }

    public void setPcIp(String pcIp) {
        this.pcIp = pcIp;
    }


}
