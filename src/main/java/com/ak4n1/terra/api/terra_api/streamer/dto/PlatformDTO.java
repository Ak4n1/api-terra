package com.ak4n1.terra.api.terra_api.streamer.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO para representar una plataforma de streaming.
 * 
 * @author ak4n1
 * @since 1.0
 */
public class PlatformDTO {

    @NotBlank(message = "Platform name is required")
    private String name;

    @NotBlank(message = "URL is required")
    private String url;

    // Constructors
    public PlatformDTO() {
    }

    public PlatformDTO(String name, String url) {
        this.name = name;
        this.url = url;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}

