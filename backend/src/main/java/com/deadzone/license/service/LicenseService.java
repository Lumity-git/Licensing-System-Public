package com.deadzone.license.service;

import com.deadzone.license.entity.License;
import com.deadzone.license.repository.LicenseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class LicenseService {

    @Autowired
    private LicenseRepository licenseRepository;

    public List<License> getAllLicenses() {
        return licenseRepository.findAll();
    }

    public Optional<License> getLicenseById(Long id) {
        return licenseRepository.findById(id);
    }

    public Optional<License> getLicenseByKey(String licenseKey) {
        return licenseRepository.findByLicenseKey(licenseKey);
    }

    public License createLicense(String licenseKey, String serverIp, Integer maxPlayers, LocalDateTime expirationDate) {
        // Generate a license key if not provided
        if (licenseKey == null || licenseKey.trim().isEmpty()) {
            licenseKey = generateLicenseKey();
        }

        // Check if license key already exists
        if (licenseRepository.findByLicenseKey(licenseKey).isPresent()) {
            throw new RuntimeException("License key already exists");
        }

        License license = new License();
        license.setLicenseKey(licenseKey);
        license.setServerIp(serverIp);
        license.setMaxPlayers(maxPlayers != null ? maxPlayers : 20); // Default max players
        license.setActive(true);
        license.setCreatedAt(LocalDateTime.now());
        license.setExpirationDate(expirationDate != null ? expirationDate : LocalDateTime.now().plusYears(1)); // Default 1 year

        return licenseRepository.save(license);
    }

    public License updateLicense(Long id, String licenseKey, String serverIp, Integer maxPlayers, LocalDateTime expirationDate) {
        Optional<License> existingLicense = licenseRepository.findById(id);
        if (!existingLicense.isPresent()) {
            throw new RuntimeException("License not found");
        }

        License license = existingLicense.get();
        
        // Check if new license key already exists (and it's not the same license)
        if (licenseKey != null && !licenseKey.equals(license.getLicenseKey())) {
            Optional<License> conflictingLicense = licenseRepository.findByLicenseKey(licenseKey);
            if (conflictingLicense.isPresent() && !conflictingLicense.get().getId().equals(id)) {
                throw new RuntimeException("License key already exists");
            }
            license.setLicenseKey(licenseKey);
        }

        if (serverIp != null) {
            license.setServerIp(serverIp);
        }
        if (maxPlayers != null) {
            license.setMaxPlayers(maxPlayers);
        }
        if (expirationDate != null) {
            license.setExpirationDate(expirationDate);
        }

        return licenseRepository.save(license);
    }

    public void deleteLicense(Long id) {
        if (!licenseRepository.existsById(id)) {
            throw new RuntimeException("License not found");
        }
        licenseRepository.deleteById(id);
    }

    public License toggleLicenseStatus(Long id) {
        Optional<License> existingLicense = licenseRepository.findById(id);
        if (!existingLicense.isPresent()) {
            throw new RuntimeException("License not found");
        }

        License license = existingLicense.get();
        license.setActive(!license.isActive());
        return licenseRepository.save(license);
    }

    public boolean isLicenseValid(String licenseKey, String serverIp) {
        Optional<License> license = licenseRepository.findByLicenseKey(licenseKey);
        
        if (!license.isPresent()) {
            return false;
        }

        License l = license.get();
        
        // Check if license is active
        if (!l.isActive()) {
            return false;
        }

        // Check if license has expired
        if (l.getExpirationDate() != null && l.getExpirationDate().isBefore(LocalDateTime.now())) {
            return false;
        }

        // Check if server IP matches (if specified)
        if (l.getServerIp() != null && !l.getServerIp().trim().isEmpty() && !l.getServerIp().equals(serverIp)) {
            return false;
        }

        return true;
    }

    public void updateLastValidation(String licenseKey) {
        Optional<License> license = licenseRepository.findByLicenseKey(licenseKey);
        if (license.isPresent()) {
            License l = license.get();
            l.setLastValidation(LocalDateTime.now());
            licenseRepository.save(l);
        }
    }

    private String generateLicenseKey() {
        // Generate a UUID-based license key
        String uuid = UUID.randomUUID().toString().replace("-", "").toUpperCase();
        // Format as XXXX-XXXX-XXXX-XXXX
        return String.format("%s-%s-%s-%s", 
            uuid.substring(0, 4),
            uuid.substring(4, 8),
            uuid.substring(8, 12),
            uuid.substring(12, 16)
        );
    }
}
