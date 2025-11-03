package com.deadzone.license.service;

import com.deadzone.license.dto.ServerReportRequest;
import com.deadzone.license.dto.ServerReportResponse;
import com.deadzone.license.entity.License;
import com.deadzone.license.entity.ServerInstance;
import com.deadzone.license.entity.ServerInstance.LicenseStatus;
import com.deadzone.license.entity.ServerInstance.PluginStatus;
import com.deadzone.license.repository.LicenseRepository;
import com.deadzone.license.repository.ServerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ServerReportService {

    @Autowired
    private ServerRepository serverRepository;

    @Autowired
    private LicenseRepository licenseRepository;

    @Autowired
    private LicenseService licenseService;

    @Value("${app.telemetry.license-server-url:https://api.your-domain.com}")
    private String licenseServerUrl;

    @Value("${app.telemetry.support-contact:support@example.com}")
    private String supportContact;

    public ServerReportResponse processServerReport(ServerReportRequest request) {
        ServerReportResponse response = new ServerReportResponse();
        
        try {
            // Find or create server instance
            ServerInstance server = findOrCreateServerInstance(request);
            
            // Update server information
            updateServerInfo(server, request);
            
            // Validate license if provided
            LicenseStatus licenseStatus = validateLicenseStatus(request.getLicenseKey(), request.getServerIp());
            server.setLicenseStatus(licenseStatus);
            server.setLicenseKeyAttempted(request.getLicenseKey());
            
            // Update plugin status
            PluginStatus pluginStatus = parsePluginStatus(request.getPluginStatus());
            server.setPluginStatus(pluginStatus);
            
            // Update timestamps
            server.setLastSeen(LocalDateTime.now());
            server.incrementValidationCount();
            
            // Save server instance
            serverRepository.save(server);
            
            // Build response
            buildResponse(response, server, licenseStatus);
            
        } catch (Exception e) {
            response.setSuccess(false);
            response.setMessage("Server report processing failed: " + e.getMessage());
        }
        
        return response;
    }

    public ServerReportResponse processHeartbeat(String serverIp, String licenseKey, Integer currentPlayers) {
        ServerReportResponse response = new ServerReportResponse();
        
        try {
            // Find existing server or create minimal entry
            Optional<ServerInstance> serverOpt = serverRepository.findFirstByServerIpOrderByLastSeenDesc(serverIp);
            ServerInstance server;
            
            if (serverOpt.isPresent()) {
                server = serverOpt.get();
            } else {
                server = new ServerInstance();
                server.setServerIp(serverIp);
                server.setFirstSeen(LocalDateTime.now());
            }
            
            // Update basic info
            if (currentPlayers != null) {
                server.setCurrentPlayers(currentPlayers);
            }
            server.setLastSeen(LocalDateTime.now());
            server.setLicenseKeyAttempted(licenseKey);
            
            // Validate license
            LicenseStatus licenseStatus = validateLicenseStatus(licenseKey, serverIp);
            server.setLicenseStatus(licenseStatus);
            
            serverRepository.save(server);
            buildResponse(response, server, licenseStatus);
            
        } catch (Exception e) {
            response.setSuccess(false);
            response.setMessage("Heartbeat processing failed: " + e.getMessage());
        }
        
        return response;
    }

    private ServerInstance findOrCreateServerInstance(ServerReportRequest request) {
        Optional<ServerInstance> existing = serverRepository.findFirstByServerIpOrderByLastSeenDesc(request.getServerIp());
        
        if (existing.isPresent()) {
            return existing.get();
        } else {
            ServerInstance newServer = new ServerInstance();
            newServer.setServerIp(request.getServerIp());
            newServer.setFirstSeen(LocalDateTime.now());
            return newServer;
        }
    }

    private void updateServerInfo(ServerInstance server, ServerReportRequest request) {
        if (request.getCurrentPlayers() != null) {
            server.setCurrentPlayers(request.getCurrentPlayers());
        }
        if (request.getMaxPlayers() != null) {
            server.setMaxPlayers(request.getMaxPlayers());
        }
        if (request.getServerName() != null) {
            server.setServerName(request.getServerName());
        }
        if (request.getServerVersion() != null) {
            server.setServerVersion(request.getServerVersion());
        }
        if (request.getPluginVersion() != null) {
            server.setPluginVersion(request.getPluginVersion());
        }
        if (request.getCpuInfo() != null) {
            server.setCpuInfo(request.getCpuInfo());
        }
        if (request.getMemoryTotal() != null) {
            server.setMemoryTotal(request.getMemoryTotal());
        }
        if (request.getMemoryUsed() != null) {
            server.setMemoryUsed(request.getMemoryUsed());
        }
        if (request.getDiskTotal() != null) {
            server.setDiskTotal(request.getDiskTotal());
        }
        if (request.getDiskUsed() != null) {
            server.setDiskUsed(request.getDiskUsed());
        }
        if (request.getOsName() != null) {
            server.setOsName(request.getOsName());
        }
        if (request.getOsVersion() != null) {
            server.setOsVersion(request.getOsVersion());
        }
        if (request.getJavaVersion() != null) {
            server.setJavaVersion(request.getJavaVersion());
        }
    }

    private LicenseStatus validateLicenseStatus(String licenseKey, String serverIp) {
        if (licenseKey == null || licenseKey.trim().isEmpty()) {
            return LicenseStatus.UNLICENSED;
        }
        
        try {
            Optional<License> licenseOpt = licenseRepository.findByLicenseKey(licenseKey);
            
            if (!licenseOpt.isPresent()) {
                return LicenseStatus.INVALID;
            }
            
            License license = licenseOpt.get();
            
            // Check if license is active
            if (!license.isActive()) {
                return LicenseStatus.INVALID;
            }
            
            // Check if license has expired
            if (license.getExpirationDate() != null && 
                license.getExpirationDate().isBefore(LocalDateTime.now())) {
                return LicenseStatus.EXPIRED;
            }
            
            // Check IP restriction if set
            if (license.getServerIp() != null && 
                !license.getServerIp().equals(serverIp)) {
                return LicenseStatus.INVALID;
            }
            
            return LicenseStatus.VALID;
            
        } catch (Exception e) {
            return LicenseStatus.UNKNOWN;
        }
    }

    private PluginStatus parsePluginStatus(String statusString) {
        if (statusString == null) {
            return PluginStatus.ACTIVE;
        }
        
        try {
            return PluginStatus.valueOf(statusString.toUpperCase());
        } catch (IllegalArgumentException e) {
            return PluginStatus.ACTIVE;
        }
    }

    private void buildResponse(ServerReportResponse response, ServerInstance server, LicenseStatus licenseStatus) {
        response.setSuccess(true);
        response.setLicenseStatus(licenseStatus.name());
        
        switch (licenseStatus) {
            case VALID:
                response.setMessage("Server registered successfully with valid license");
                response.setShouldShutdown(false);
                response.setRequiresLicenseCheck(false);
                if (server.getLicense() != null) {
                    response.setMaxPlayersAllowed(server.getLicense().getMaxPlayers());
                    response.setExpirationDate(server.getLicense().getExpirationDate().toString());
                }
                break;
                
            case INVALID:
                response.setMessage("Server registered with invalid license");
                response.setShouldShutdown(false); // Don't shutdown, just track
                response.setRequiresLicenseCheck(true);
                response.setShutdownReason("Invalid license key provided");
                break;
                
            case EXPIRED:
                response.setMessage("Server registered with expired license");
                response.setShouldShutdown(false); // Don't shutdown, just track
                response.setRequiresLicenseCheck(true);
                response.setShutdownReason("License has expired");
                break;
                
            case UNLICENSED:
                response.setMessage("Server registered without license");
                response.setShouldShutdown(false); // Don't shutdown, just track
                response.setRequiresLicenseCheck(true);
                response.setShutdownReason("No license provided");
                break;
                
            default:
                response.setMessage("Server registered with unknown license status");
                response.setShouldShutdown(false);
                response.setRequiresLicenseCheck(true);
                break;
        }
        
        // Set default reporting interval (5 minutes)
        response.setReportInterval(300L);
        response.setEnableTelemetry(true);
        response.setLicenseServerUrl(licenseServerUrl);
        response.setSupportContact(supportContact);
    }
}
