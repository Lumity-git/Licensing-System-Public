package com.deadzone.license.service;

import com.deadzone.license.dto.ValidationResponse;
import com.deadzone.license.entity.License;
import com.deadzone.license.entity.ServerInstance;
import com.deadzone.license.repository.LicenseRepository;
import com.deadzone.license.repository.ServerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ValidationService {

    @Autowired
    private LicenseRepository licenseRepository;

    @Autowired
    private ServerRepository serverRepository;

    @Autowired
    private LicenseService licenseService;

    public ValidationResponse validateLicense(String licenseKey, String serverIp, Integer currentPlayers) {
        ValidationResponse response = new ValidationResponse();
        
        try {
            // Check if license exists and is valid
            if (!licenseService.isLicenseValid(licenseKey, serverIp)) {
                response.setValid(false);
                response.setMessage("Invalid or expired license");
                return response;
            }

            Optional<License> licenseOpt = licenseRepository.findByLicenseKey(licenseKey);
            if (!licenseOpt.isPresent()) {
                response.setValid(false);
                response.setMessage("License not found");
                return response;
            }

            License license = licenseOpt.get();

            // Check player count limit
            if (currentPlayers != null && currentPlayers > license.getMaxPlayers()) {
                response.setValid(false);
                response.setMessage("Player count exceeds license limit of " + license.getMaxPlayers());
                return response;
            }

            // Update last validation timestamp
            licenseService.updateLastValidation(licenseKey);

            // Update or create server instance
            updateServerInstance(license, serverIp, currentPlayers);

            // License is valid
            response.setValid(true);
            response.setMessage("License validation successful");
            response.setMaxPlayers(license.getMaxPlayers());
            response.setExpirationDate(license.getExpirationDate());

        } catch (Exception e) {
            response.setValid(false);
            response.setMessage("Validation error: " + e.getMessage());
        }

        return response;
    }

    public ValidationResponse getLicenseStatus(String licenseKey) {
        ValidationResponse response = new ValidationResponse();
        
        try {
            Optional<License> licenseOpt = licenseRepository.findByLicenseKey(licenseKey);
            if (!licenseOpt.isPresent()) {
                response.setValid(false);
                response.setMessage("License not found");
                return response;
            }

            License license = licenseOpt.get();
            
            // Check if license is active
            if (!license.isActive()) {
                response.setValid(false);
                response.setMessage("License is deactivated");
                return response;
            }

            // Check if license has expired
            if (license.getExpirationDate() != null && license.getExpirationDate().isBefore(LocalDateTime.now())) {
                response.setValid(false);
                response.setMessage("License has expired");
                return response;
            }

            response.setValid(true);
            response.setMessage("License is active");
            response.setMaxPlayers(license.getMaxPlayers());
            response.setExpirationDate(license.getExpirationDate());

        } catch (Exception e) {
            response.setValid(false);
            response.setMessage("Status check error: " + e.getMessage());
        }

        return response;
    }

    public void updateServerHeartbeat(String licenseKey, String serverIp, Integer currentPlayers) {
        Optional<License> licenseOpt = licenseRepository.findByLicenseKey(licenseKey);
        if (!licenseOpt.isPresent()) {
            throw new RuntimeException("License not found");
        }

        License license = licenseOpt.get();
        updateServerInstance(license, serverIp, currentPlayers);
    }

    private void updateServerInstance(License license, String serverIp, Integer currentPlayers) {
        // Normalize serverIp to avoid null/empty values which violate DB constraints
        String normalizedIp = (serverIp == null || serverIp.trim().isEmpty()) ? "unknown" : serverIp.trim();

        // Find existing server instance or create new one using normalized IP
        Optional<ServerInstance> serverOpt = serverRepository.findByLicenseAndServerIp(license, normalizedIp);

        ServerInstance server;
        if (serverOpt.isPresent()) {
            server = serverOpt.get();
        } else {
            server = new ServerInstance();
            server.setLicense(license);
            server.setServerIp(normalizedIp);
            server.setFirstSeen(LocalDateTime.now());
        }

        server.setCurrentPlayers(currentPlayers != null ? currentPlayers : 0);
        server.setLastSeen(LocalDateTime.now());

        serverRepository.save(server);
    }
}
