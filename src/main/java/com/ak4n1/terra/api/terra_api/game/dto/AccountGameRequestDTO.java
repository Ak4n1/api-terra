package com.ak4n1.terra.api.terra_api.game.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class AccountGameRequestDTO {
    
    @NotBlank(message = "Username is required")
    @Size(min = 4, max = 14, message = "Username must be between 4 and 14 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers and underscores")
    private String username;
    
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 16, message = "Password must be between 8 and 16 characters")
    private String password;
    
    @NotBlank(message = "Creation code is required")
    @Size(min = 6, max = 10, message = "Creation code must be between 6 and 10 characters")
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
