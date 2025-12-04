package com.ak4n1.terra.api.terra_api.streamer.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Entidad que representa una plataforma de streaming asociada a una solicitud.
 * 
 * @author ak4n1
 * @since 1.0
 */
@Entity
@Table(name = "streamer_platforms")
public class StreamerPlatform {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    @JsonIgnore
    private StreamerApplication application;

    @Column(name = "platform_name", nullable = false)
    @NotBlank(message = "Platform name is required")
    private String platformName;

    @Column(name = "url", nullable = false, length = 500)
    @NotBlank(message = "URL is required")
    private String url;

    // Constructors
    public StreamerPlatform() {
    }

    public StreamerPlatform(String platformName, String url) {
        this.platformName = platformName;
        this.url = url;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public StreamerApplication getApplication() {
        return application;
    }

    public void setApplication(StreamerApplication application) {
        this.application = application;
    }

    public String getPlatformName() {
        return platformName;
    }

    public void setPlatformName(String platformName) {
        this.platformName = platformName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}

