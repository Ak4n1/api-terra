package com.ak4n1.terra.api.terra_api.auth.dto;

public class Verify2FACodeRequest {
    private EmailRequestDTO emailRequest;
    private String code;

    public EmailRequestDTO getEmailRequest() {
        return emailRequest;
    }

    public void setEmailRequest(EmailRequestDTO emailRequest) {
        this.emailRequest = emailRequest;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
