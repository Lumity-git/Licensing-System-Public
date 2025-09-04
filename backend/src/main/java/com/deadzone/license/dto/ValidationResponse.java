package com.deadzone.license.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidationResponse {

    private boolean valid;
    private String message;
    private Integer maxPlayers;
    private LocalDateTime expirationDate;

    public static ValidationResponse success(String message, Integer maxPlayers, LocalDateTime expirationDate) {
        return new ValidationResponse(true, message, maxPlayers, expirationDate);
    }

    public static ValidationResponse failure(String message) {
        return new ValidationResponse(false, message, null, null);
    }
}
