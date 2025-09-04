package com.deadzone.license.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "licenses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class License {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "license_key", unique = true, nullable = false, length = 64)
    private String licenseKey;

    @Column(name = "server_ip", length = 45)
    private String serverIp;

    @Column(name = "max_players", nullable = false)
    private Integer maxPlayers = 20;

    @Column(name = "is_active", nullable = false)
    private Boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "expiration_date")
    private LocalDateTime expirationDate;

    @Column(name = "last_validation")
    private LocalDateTime lastValidation;

    @OneToMany(mappedBy = "license", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<ServerInstance> serverInstances;

    // Helper methods
    public boolean isExpired() {
        return expirationDate != null && expirationDate.isBefore(LocalDateTime.now());
    }

    public boolean isActive() {
        return active != null && active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
