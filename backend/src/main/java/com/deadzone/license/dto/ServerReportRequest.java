package com.deadzone.license.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServerReportRequest {
    private String serverIp;
    private String licenseKey; // Can be null for unlicensed servers
    private Integer currentPlayers;
    private Integer maxPlayers;
    private String serverName;
    private String serverVersion;
    private String pluginVersion;
    private String cpuInfo;
    private Long memoryTotal; // in MB
    private Long memoryUsed; // in MB
    private Long diskTotal; // in MB
    private Long diskUsed; // in MB
    private String osName;
    private String osVersion;
    private String javaVersion;
    private String pluginStatus; // ACTIVE, INACTIVE, ERROR, STARTING, STOPPING
    private Boolean hasLicense;
    
    // Additional metadata
    private String minecraftVersion;
    private String[] installedPlugins;
    private Integer tps; // Server TPS if available
    private Long uptimeSeconds;
}
