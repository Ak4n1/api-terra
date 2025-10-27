package com.ak4n1.terra.api.terra_api.game.dto;

public class ChangePasswordGameDTO {

    private String login;
    private String newPassword;
    private String code;


    public ChangePasswordGameDTO(String code, String login, String newPassword) {
        this.code = code;
        this.login = login;
        this.newPassword = newPassword;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
