package com.deadzone.license.controller;

import com.deadzone.license.dto.ServerReportRequest;
import com.deadzone.license.dto.ServerReportResponse;
import com.deadzone.license.entity.ServerInstance;
import com.deadzone.license.service.ServerReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/report")
public class ServerReportController {

    @Autowired
    private ServerReportService serverReportService;

    /**
     * Public endpoint for servers to report their status
     * This endpoint accepts reports from both licensed and unlicensed servers
     */
    @PostMapping("/server")
    public ResponseEntity<ServerReportResponse> reportServerStatus(@RequestBody ServerReportRequest request) {
        try {
            ServerReportResponse response = serverReportService.processServerReport(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ServerReportResponse errorResponse = new ServerReportResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Failed to process server report: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Heartbeat endpoint for quick status updates
     */
    @PostMapping("/heartbeat")
    public ResponseEntity<ServerReportResponse> serverHeartbeat(
            @RequestParam String serverIp,
            @RequestParam(required = false) String licenseKey,
            @RequestParam(required = false) Integer currentPlayers) {
        
        try {
            ServerReportResponse response = serverReportService.processHeartbeat(serverIp, licenseKey, currentPlayers);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ServerReportResponse errorResponse = new ServerReportResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Heartbeat failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}
