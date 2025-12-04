package com.ak4n1.terra.api.terra_api.streamer.services;

import com.ak4n1.terra.api.terra_api.auth.entities.AccountMaster;
import com.ak4n1.terra.api.terra_api.auth.repositories.AccountMasterRepository;
import com.ak4n1.terra.api.terra_api.streamer.dto.PlatformDTO;
import com.ak4n1.terra.api.terra_api.streamer.dto.StreamerApplicationDTO;
import com.ak4n1.terra.api.terra_api.streamer.dto.StreamerApplicationResponseDTO;
import com.ak4n1.terra.api.terra_api.streamer.entities.ApplicationStatus;
import com.ak4n1.terra.api.terra_api.streamer.entities.StreamerApplication;
import com.ak4n1.terra.api.terra_api.streamer.entities.StreamerPlatform;
import com.ak4n1.terra.api.terra_api.streamer.repositories.StreamerApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación del servicio para gestionar solicitudes de streamer.
 * 
 * @author ak4n1
 * @since 1.0
 */
@Service
@Transactional
public class StreamerApplicationServiceImpl implements StreamerApplicationService {

    private final StreamerApplicationRepository applicationRepository;
    private final AccountMasterRepository accountMasterRepository;

    @Autowired
    public StreamerApplicationServiceImpl(
            StreamerApplicationRepository applicationRepository,
            AccountMasterRepository accountMasterRepository) {
        this.applicationRepository = applicationRepository;
        this.accountMasterRepository = accountMasterRepository;
    }

    @Override
    public StreamerApplicationResponseDTO submitApplication(StreamerApplicationDTO dto, Long accountId) {
        // Get user
        AccountMaster accountMaster = accountMasterRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Verify user doesn't already have streamer role
        boolean hasStreamerRole = accountMaster.getRoles().stream()
                .anyMatch(role -> "ROLE_STREAMER".equals(role.getName()));
        if (hasStreamerRole) {
            throw new IllegalStateException("You already have the streamer role");
        }

        // Verify user doesn't have a pending application
        applicationRepository.findPendingByAccountMasterId(accountId, ApplicationStatus.PENDING)
                .ifPresent(application -> {
                    throw new IllegalStateException("You already have a pending application");
                });

        // Crear la solicitud
        StreamerApplication application = new StreamerApplication();
        application.setAccountMaster(accountMaster);
        application.setFirstName(dto.getFirstName());
        application.setLastName(dto.getLastName());
        application.setStatus(ApplicationStatus.PENDING);

        // Crear las plataformas
        List<StreamerPlatform> platforms = dto.getPlatforms().stream()
                .map(platformDTO -> {
                    StreamerPlatform platform = new StreamerPlatform();
                    platform.setPlatformName(platformDTO.getName());
                    platform.setUrl(platformDTO.getUrl());
                    platform.setApplication(application);
                    return platform;
                })
                .collect(Collectors.toList());

        application.setPlatforms(platforms);

        // Log para debugging
        System.out.println("[StreamerApplication] Saving application with " + platforms.size() + " platforms");
        platforms.forEach(p -> System.out.println("[StreamerPlatform] " + p.getPlatformName() + ": " + p.getUrl()));

        // Guardar
        StreamerApplication savedApplication = applicationRepository.save(application);
        
        // Verificar que se guardaron las plataformas
        System.out.println("[StreamerApplication] Saved application ID: " + savedApplication.getId());
        System.out.println("[StreamerApplication] Platforms count after save: " + savedApplication.getPlatforms().size());

        return convertToResponseDTO(savedApplication);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StreamerApplicationResponseDTO> getUserApplications(Long accountId) {
        List<StreamerApplication> applications = applicationRepository.findByAccountMasterId(accountId);
        return applications.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public StreamerApplicationResponseDTO getApplicationById(Long applicationId, Long accountId) {
        StreamerApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));

        // Verify the application belongs to the user
        if (!application.getAccountMaster().getId().equals(accountId)) {
            throw new IllegalArgumentException("You don't have permission to view this application");
        }

        return convertToResponseDTO(application);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StreamerApplicationResponseDTO> getApprovedApplications() {
        List<StreamerApplication> applications = applicationRepository.findByStatus(ApplicationStatus.APPROVED);
        return applications.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<StreamerApplicationResponseDTO> getPendingApplications() {
        List<StreamerApplication> applications = applicationRepository.findByStatus(ApplicationStatus.PENDING);
        return applications.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<StreamerApplicationResponseDTO> getRejectedApplications() {
        List<StreamerApplication> applications = applicationRepository.findByStatus(ApplicationStatus.REJECTED);
        return applications.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Convierte una entidad StreamerApplication a su DTO de respuesta.
     */
    private StreamerApplicationResponseDTO convertToResponseDTO(StreamerApplication application) {
        StreamerApplicationResponseDTO dto = new StreamerApplicationResponseDTO();
        dto.setId(application.getId());
        dto.setFirstName(application.getFirstName());
        dto.setLastName(application.getLastName());
        dto.setStatus(application.getStatus());
        dto.setSubmittedAt(application.getSubmittedAt());
        dto.setReviewedAt(application.getReviewedAt());
        dto.setRejectionReason(application.getRejectionReason());
        dto.setNotes(application.getNotes());

        // Convertir plataformas
        List<PlatformDTO> platformDTOs = application.getPlatforms().stream()
                .map(platform -> new PlatformDTO(platform.getPlatformName(), platform.getUrl()))
                .collect(Collectors.toList());
        dto.setPlatforms(platformDTOs);

        // Email del admin que revisó (si existe)
        if (application.getReviewedBy() != null) {
            dto.setReviewedByEmail(application.getReviewedBy().getEmail());
        }

        return dto;
    }
}

