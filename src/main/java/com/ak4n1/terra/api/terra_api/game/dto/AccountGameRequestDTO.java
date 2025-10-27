package com.ak4n1.terra.api.terra_api.game.dto;

public class AccountGameRequestDTO {
    private String username;
    private String password;
    private String createCode;


    public AccountGameRequestDTO( String password, String username,String createCode) {
        this.password = password;
        this.username = username;
        this.createCode = createCode;
    }

    public String getCreateCode() {
        return createCode;
    }

    public void setCreateCode(String createCode) {
        this.createCode = createCode;
    }


    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        return "codigo " + this.createCode + "cuenta " + this.username;
    }
}
