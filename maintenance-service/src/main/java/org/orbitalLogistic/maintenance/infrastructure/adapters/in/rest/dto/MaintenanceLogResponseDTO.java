package org.orbitalLogistic.maintenance.infrastructure.adapters.in.rest.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MaintenanceLogResponseDTO(
        Long id,
        Long spacecraftId,
        String spacecraftName,
        MaintenanceType maintenanceType,
        Long performedByUserId,
        String performedByUserName,
        Long supervisedByUserId,
        String supervisedByUserName,
        LocalDateTime startTime,
        LocalDateTime endTime,
        MaintenanceStatus status,
        String description,
        BigDecimal cost
) {
}
