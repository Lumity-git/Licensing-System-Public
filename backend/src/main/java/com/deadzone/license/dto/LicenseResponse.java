package com.deadzone.license.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LicenseResponse {

    private Long id;
    private String licenseKey;
    private String serverIp;
    private Integer maxPlayers;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime expirationDate;
    private LocalDateTime lastValidation;
}
