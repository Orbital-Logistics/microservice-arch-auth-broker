package org.orbitalLogistic.maintenance.application.ports.in;

import lombok.Builder;
import org.orbitalLogistic.maintenance.domain.model.enums.MaintenanceStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record UpdateMaintenanceStatusCommand(
        Long id,
        MaintenanceStatus status,
        LocalDateTime endTime,
        BigDecimal cost,
        String description
) {
}
