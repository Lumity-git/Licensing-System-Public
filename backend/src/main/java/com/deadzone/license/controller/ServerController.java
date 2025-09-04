package com.deadzone.license.controller;

import com.deadzone.license.entity.ServerInstance;
import com.deadzone.license.repository.ServerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/servers")
public class ServerController {

    @Autowired
    private ServerRepository serverRepository;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ServerInstance>> getAllServers() {
        List<ServerInstance> servers = serverRepository.findAllOrderByLastSeenDesc();
        return ResponseEntity.ok(servers);
    }

    @GetMapping("/licensed")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ServerInstance>> getLicensedServers() {
        List<ServerInstance> servers = serverRepository.findByLicenseStatusOrderByLastSeenDesc(ServerInstance.LicenseStatus.VALID);
        return ResponseEntity.ok(servers);
    }

    @GetMapping("/unlicensed")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ServerInstance>> getUnlicensedServers() {
        List<ServerInstance> servers = serverRepository.findByLicenseStatusOrderByLastSeenDesc(ServerInstance.LicenseStatus.UNLICENSED);
        return ResponseEntity.ok(servers);
    }

    @GetMapping("/invalid")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ServerInstance>> getInvalidLicenseServers() {
        List<ServerInstance> servers = serverRepository.findByLicenseStatusOrderByLastSeenDesc(ServerInstance.LicenseStatus.INVALID);
        return ResponseEntity.ok(servers);
    }

    @GetMapping("/expired")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ServerInstance>> getExpiredLicenseServers() {
        List<ServerInstance> servers = serverRepository.findByLicenseStatusOrderByLastSeenDesc(ServerInstance.LicenseStatus.EXPIRED);
        return ResponseEntity.ok(servers);
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ServerInstance>> getServersByLicenseStatus(@PathVariable String status) {
        try {
            ServerInstance.LicenseStatus licenseStatus = ServerInstance.LicenseStatus.valueOf(status.toUpperCase());
            List<ServerInstance> servers = serverRepository.findByLicenseStatusOrderByLastSeenDesc(licenseStatus);
            return ResponseEntity.ok(servers);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/license/{licenseId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ServerInstance>> getServersByLicense(@PathVariable Long licenseId) {
        List<ServerInstance> servers = serverRepository.findByLicenseId(licenseId);
        return ResponseEntity.ok(servers);
    }

    @GetMapping("/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ServerInstance>> getActiveServers() {
        // Consider servers active if they were seen in the last 5 minutes
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(5);
        List<ServerInstance> servers = serverRepository.findByLastSeenAfter(cutoff);
        return ResponseEntity.ok(servers);
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getServerStats() {
        Map<String, Object> stats = new HashMap<>();
        
        LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);
        LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
        
        // Overall counts
        stats.put("totalServers", serverRepository.count());
        stats.put("activeServers", serverRepository.countByLastSeenAfter(fiveMinutesAgo));
        
        // License status counts
        stats.put("validLicenseServers", serverRepository.findByLicenseStatusOrderByLastSeenDesc(ServerInstance.LicenseStatus.VALID).size());
        stats.put("invalidLicenseServers", serverRepository.findByLicenseStatusOrderByLastSeenDesc(ServerInstance.LicenseStatus.INVALID).size());
        stats.put("expiredLicenseServers", serverRepository.findByLicenseStatusOrderByLastSeenDesc(ServerInstance.LicenseStatus.EXPIRED).size());
        stats.put("unlicensedServers", serverRepository.findByLicenseStatusOrderByLastSeenDesc(ServerInstance.LicenseStatus.UNLICENSED).size());
        
        // Plugin status counts
        stats.put("activePlugins", serverRepository.findByPluginStatusOrderByLastSeenDesc(ServerInstance.PluginStatus.ACTIVE).size());
        stats.put("inactivePlugins", serverRepository.findByPluginStatusOrderByLastSeenDesc(ServerInstance.PluginStatus.INACTIVE).size());
        
        // Recent activity
        stats.put("recentlyActiveServers", serverRepository.findRecentlyActive(oneDayAgo).size());
        
        return ResponseEntity.ok(stats);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteServer(@PathVariable Long id) {
        try {
            if (!serverRepository.existsById(id)) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Server instance not found");
                return ResponseEntity.notFound().build();
            }

            serverRepository.deleteById(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Server instance deleted successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to delete server instance: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
}
