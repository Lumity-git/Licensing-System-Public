package com.deadzone.license.controller;

import com.deadzone.license.dto.ValidationRequest;
import com.deadzone.license.dto.ValidationResponse;
import com.deadzone.license.service.ValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/validate")
@CrossOrigin(origins = "*") // Allow all origins for validation endpoint
public class ValidationController {

    @Autowired
    private ValidationService validationService;

    @PostMapping("/license")
    public ResponseEntity<ValidationResponse> validateLicense(@RequestBody ValidationRequest request) {
        ValidationResponse response = validationService.validateLicense(
            request.getLicenseKey(),
            request.getServerIp(),
            request.getCurrentPlayers()
        );
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{licenseKey}")
    public ResponseEntity<ValidationResponse> getLicenseStatus(@PathVariable String licenseKey) {
        ValidationResponse response = validationService.getLicenseStatus(licenseKey);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/heartbeat")
    public ResponseEntity<?> heartbeat(@RequestBody ValidationRequest request) {
        try {
            validationService.updateServerHeartbeat(
                request.getLicenseKey(),
                request.getServerIp(),
                request.getCurrentPlayers()
            );
            
            ValidationResponse response = new ValidationResponse();
            response.setValid(true);
            response.setMessage("Heartbeat recorded successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ValidationResponse response = new ValidationResponse();
            response.setValid(false);
            response.setMessage("Failed to record heartbeat: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
}
