package com.ak4n1.terra.api.terra_api.streamer.entities;

import com.ak4n1.terra.api.terra_api.auth.entities.AccountMaster;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa una solicitud de streamer.
 * 
 * @author ak4n1
 * @since 1.0
 */
@Entity
@Table(name = "streamer_applications")
public class StreamerApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    @NotNull(message = "User is required")
    @JsonIgnoreProperties({"password", "verificationToken", "passwordResetToken"})
    private AccountMaster accountMaster;

    @Column(name = "first_name", nullable = false)
    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name cannot exceed 100 characters")
    private String firstName;

    @Column(name = "last_name", nullable = false)
    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name cannot exceed 100 characters")
    private String lastName;

    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<StreamerPlatform> platforms = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @NotNull
    private ApplicationStatus status = ApplicationStatus.PENDING;

    @Column(name = "submitted_at", nullable = false, updatable = false)
    private LocalDateTime submittedAt;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    @JsonIgnoreProperties({"password", "verificationToken", "passwordResetToken"})
    private AccountMaster reviewedBy;

    @Column(name = "rejection_reason", length = 1000)
    @Size(max = 1000, message = "Rejection reason cannot exceed 1000 characters")
    private String rejectionReason;

    @Column(name = "notes", length = 2000)
    @Size(max = 2000, message = "Notes cannot exceed 2000 characters")
    private String notes;

    // Lifecycle callbacks
    @PrePersist
    protected void onCreate() {
        submittedAt = LocalDateTime.now();
    }

    // Constructors
    public StreamerApplication() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AccountMaster getAccountMaster() {
        return accountMaster;
    }

    public void setAccountMaster(AccountMaster accountMaster) {
        this.accountMaster = accountMaster;
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

    public List<StreamerPlatform> getPlatforms() {
        return platforms;
    }

    public void setPlatforms(List<StreamerPlatform> platforms) {
        this.platforms = platforms;
        // Asegurar la relación bidireccional
        if (platforms != null) {
            platforms.forEach(platform -> platform.setApplication(this));
        }
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

    public AccountMaster getReviewedBy() {
        return reviewedBy;
    }

    public void setReviewedBy(AccountMaster reviewedBy) {
        this.reviewedBy = reviewedBy;
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

