package com.ak4n1.terra.api.terra_api.streamer.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * DTO para crear una nueva solicitud de streamer.
 * 
 * @author ak4n1
 * @since 1.0
 */
public class StreamerApplicationDTO {

    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name cannot exceed 100 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name cannot exceed 100 characters")
    private String lastName;

    @NotEmpty(message = "At least one platform must be selected")
    @Valid
    private List<PlatformDTO> platforms;

    // Constructors
    public StreamerApplicationDTO() {
    }

    public StreamerApplicationDTO(String firstName, String lastName, List<PlatformDTO> platforms) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.platforms = platforms;
    }

    // Getters and Setters
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public List<PlatformDTO> getPlatforms() {
        return platforms;
    }

    public void setPlatforms(List<PlatformDTO> platforms) {
        this.platforms = platforms;
    }
}

