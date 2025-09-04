package com.deadzone.license.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import com.fasterxml.jackson.annotation.JsonBackReference;

import java.time.LocalDateTime;

@Entity
@Table(name = "server_instances", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"license_id", "server_ip"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServerInstance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "license_id", nullable = true) // Allow null for unlicensed servers
    @JsonBackReference
    private License license;

    @Column(name = "server_ip", nullable = false, length = 45)
    private String serverIp;

    @Column(name = "current_players")
    private Integer currentPlayers = 0;

    @Column(name = "max_players")
    private Integer maxPlayers;

    @Column(name = "server_name", length = 100)
    private String serverName;

    @Column(name = "server_version", length = 50)
    private String serverVersion;

    @Column(name = "plugin_version", length = 20)
    private String pluginVersion;

    @Column(name = "cpu_info", length = 255)
    private String cpuInfo;

    @Column(name = "memory_total")
    private Long memoryTotal; // in MB

    @Column(name = "memory_used")
    private Long memoryUsed; // in MB

    @Column(name = "disk_total")
    private Long diskTotal; // in MB

    @Column(name = "disk_used")
    private Long diskUsed; // in MB

    @Column(name = "os_name", length = 100)
    private String osName;

    @Column(name = "os_version", length = 50)
    private String osVersion;

    @Column(name = "java_version", length = 50)
    private String javaVersion;

    @Enumerated(EnumType.STRING)
    @Column(name = "license_status", nullable = false)
    private LicenseStatus licenseStatus = LicenseStatus.UNKNOWN;

    @Enumerated(EnumType.STRING)
    @Column(name = "plugin_status", nullable = false)
    private PluginStatus pluginStatus = PluginStatus.ACTIVE;

    @Column(name = "last_seen", nullable = false)
    private LocalDateTime lastSeen;

    @Column(name = "first_seen", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime firstSeen;

    @Column(name = "validation_count", nullable = false)
    private Long validationCount = 0L;

    @Column(name = "license_key_attempted", length = 100)
    private String licenseKeyAttempted; // Store attempted license key even if invalid

    // Enums
    public enum LicenseStatus {
        VALID,
        INVALID,
        EXPIRED,
        UNLICENSED,
        UNKNOWN
    }

    public enum PluginStatus {
        ACTIVE,
        INACTIVE,
        ERROR,
        STARTING,
        STOPPING
    }

    // Helper methods
    public void incrementValidationCount() {
        this.validationCount++;
    }

    public void updateLastSeen() {
        this.lastSeen = LocalDateTime.now();
    }
}
