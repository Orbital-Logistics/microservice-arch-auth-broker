package org.orbitalLogistic.maintenance.infrastructure.adapters.in.rest.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MaintenanceLogRequestDTO(
        @NotNull(message = "Spacecraft ID is required")
        Long spacecraftId,

        @NotNull(message = "Maintenance type is required")
        MaintenanceType maintenanceType,

        @NotNull(message = "Performed by user ID is required")
        Long performedByUserId,

        Long supervisedByUserId,

        LocalDateTime startTime,

        LocalDateTime endTime,

        MaintenanceStatus status,

        String description,

        @DecimalMin(value = "0.0", message = "Cost must be non-negative")
        BigDecimal cost
) {
}
