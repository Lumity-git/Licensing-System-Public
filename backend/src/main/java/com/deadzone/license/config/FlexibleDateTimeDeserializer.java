package com.deadzone.license.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class FlexibleDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {

    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String text = p.getText();
        if (text == null || text.trim().isEmpty()) {
            return null;
        }

        text = text.trim();

        // Try to parse as LocalDateTime first (full datetime format)
        try {
            return LocalDateTime.parse(text, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (DateTimeParseException e) {
            // Ignore and try next format
        }

        // Try to parse as LocalDate and convert to LocalDateTime at start of day
        try {
            LocalDate date = LocalDate.parse(text, DateTimeFormatter.ISO_LOCAL_DATE);
            return date.atStartOfDay();
        } catch (DateTimeParseException e) {
            // Ignore and try next format
        }

        // Try common date formats
        String[] dateFormats = {
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd HH:mm",
            "MM/dd/yyyy",
            "dd/MM/yyyy",
            "yyyy/MM/dd"
        };

        for (String format : dateFormats) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
                if (format.contains("HH")) {
                    return LocalDateTime.parse(text, formatter);
                } else {
                    LocalDate date = LocalDate.parse(text, formatter);
                    return date.atStartOfDay();
                }
            } catch (DateTimeParseException e) {
                // Continue to next format
            }
        }

        throw new IOException("Unable to parse date/time: " + text + 
            ". Expected formats: yyyy-MM-dd, yyyy-MM-ddTHH:mm:ss, or other common date formats");
    }
}
