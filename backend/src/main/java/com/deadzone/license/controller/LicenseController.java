package com.deadzone.license.controller;

import com.deadzone.license.dto.LicenseRequest;
import com.deadzone.license.dto.LicenseResponse;
import com.deadzone.license.entity.License;
import com.deadzone.license.service.LicenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/licenses")
public class LicenseController {

    @Autowired
    private LicenseService licenseService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<LicenseResponse>> getAllLicenses() {
        List<License> licenses = licenseService.getAllLicenses();
        List<LicenseResponse> response = licenses.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LicenseResponse> getLicenseById(@PathVariable Long id) {
        Optional<License> license = licenseService.getLicenseById(id);
        if (license.isPresent()) {
            return ResponseEntity.ok(convertToResponse(license.get()));
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createLicense(@RequestBody LicenseRequest request) {
        try {
            License license = licenseService.createLicense(
                request.getLicenseKey(),
                request.getServerIp(),
                request.getMaxPlayers(),
                request.getExpirationDate()
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("license", convertToResponse(license));
            response.put("message", "License created successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to create license: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateLicense(@PathVariable Long id, @RequestBody LicenseRequest request) {
        try {
            Optional<License> existingLicense = licenseService.getLicenseById(id);
            if (!existingLicense.isPresent()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "License not found");
                return ResponseEntity.notFound().build();
            }

            License license = licenseService.updateLicense(
                id,
                request.getLicenseKey(),
                request.getServerIp(),
                request.getMaxPlayers(),
                request.getExpirationDate()
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("license", convertToResponse(license));
            response.put("message", "License updated successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to update license: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteLicense(@PathVariable Long id) {
        try {
            licenseService.deleteLicense(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "License deleted successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to delete license: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PutMapping("/{id}/toggle-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> toggleLicenseStatus(@PathVariable Long id) {
        try {
            License license = licenseService.toggleLicenseStatus(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("license", convertToResponse(license));
            response.put("message", "License status updated successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to update license status: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    private LicenseResponse convertToResponse(License license) {
        LicenseResponse response = new LicenseResponse();
        response.setId(license.getId());
        response.setLicenseKey(license.getLicenseKey());
        response.setServerIp(license.getServerIp());
        response.setMaxPlayers(license.getMaxPlayers());
        response.setActive(license.isActive());
        response.setCreatedAt(license.getCreatedAt());
        response.setExpirationDate(license.getExpirationDate());
        response.setLastValidation(license.getLastValidation());
        return response;
    }
}
