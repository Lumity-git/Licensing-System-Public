package com.deadzone.license.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServerReportResponse {
    private boolean success;
    private String message;
    private String licenseStatus; // VALID, INVALID, EXPIRED, UNLICENSED, UNKNOWN
    private boolean shouldShutdown; // If server should shutdown due to license violation
    private String shutdownReason;
    private Integer maxPlayersAllowed;
    private String expirationDate;
    private Long reportInterval; // How often server should report (in seconds)
    
    // Instructions for the server
    private boolean requiresLicenseCheck;
    private String licenseServerUrl;
    private String[] allowedFeatures; // Features the server is allowed to use
    private String[] restrictedFeatures; // Features that should be disabled
    
    // Additional configuration
    private boolean enableTelemetry;
    private String updateCheckUrl;
    private String supportContact;
}
