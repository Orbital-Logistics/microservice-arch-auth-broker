package org.orbitalLogistic.maintenance.application.ports.in;

import lombok.Builder;
import org.orbitalLogistic.maintenance.domain.model.enums.MaintenanceStatus;
import org.orbitalLogistic.maintenance.domain.model.enums.MaintenanceType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record CreateMaintenanceLogCommand(
        Long spacecraftId,
        MaintenanceType maintenanceType,
        Long performedByUserId,
        Long supervisedByUserId,
        LocalDateTime startTime,
        LocalDateTime endTime,
        MaintenanceStatus status,
        String description,
        BigDecimal cost
) {
}
