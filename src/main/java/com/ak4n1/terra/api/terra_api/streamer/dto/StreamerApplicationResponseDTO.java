package com.ak4n1.terra.api.terra_api.streamer.dto;

import com.ak4n1.terra.api.terra_api.streamer.entities.ApplicationStatus;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para representar una solicitud de streamer en las respuestas.
 * 
 * @author ak4n1
 * @since 1.0
 */
public class StreamerApplicationResponseDTO {

    private Long id;
    private String firstName;
    private String lastName;
    private List<PlatformDTO> platforms;
    private ApplicationStatus status;
    private LocalDateTime submittedAt;
    private LocalDateTime reviewedAt;
    private String reviewedByEmail;
    private String rejectionReason;
    private String notes;

    // Constructors
    public StreamerApplicationResponseDTO() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public String getReviewedByEmail() {
        return reviewedByEmail;
    }

    public void setReviewedByEmail(String reviewedByEmail) {
        this.reviewedByEmail = reviewedByEmail;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}

