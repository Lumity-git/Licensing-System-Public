package com.deadzone.license.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidationRequest {

    @NotBlank(message = "License key is required")
    private String licenseKey;

    @NotBlank(message = "Server IP is required")
    private String serverIp;

    private Integer currentPlayers;
}
