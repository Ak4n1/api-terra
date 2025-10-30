package com.ak4n1.terra.api.terra_api.auth.dto;

/**
 * DTO para petición de verificación de código 2FA.
 * 
 * @author ak4n1
 * @since 1.0
 */
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
