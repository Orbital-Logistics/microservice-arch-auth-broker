package org.orbitalLogistic.file.adapters.exceptions.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.Map;

public record ErrorResponseDTO(
        @NotNull String message,
        @NotNull LocalDateTime timestamp,
        Map<String, String> details
) {}
