package com.deadzone.license.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.deadzone.license.config.FlexibleDateTimeDeserializer;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LicenseRequest {

    private String licenseKey;

    @Size(max = 45, message = "Server IP cannot exceed 45 characters")
    private String serverIp;

    @Min(value = 1, message = "Max players must be at least 1")
    @Max(value = 1000, message = "Max players cannot exceed 1000")
    private Integer maxPlayers;

    @JsonDeserialize(using = FlexibleDateTimeDeserializer.class)
    private LocalDateTime expirationDate;
}
