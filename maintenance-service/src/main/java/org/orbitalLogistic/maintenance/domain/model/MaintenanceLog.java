package org.orbitalLogistic.maintenance.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.orbitalLogistic.maintenance.domain.model.enums.MaintenanceStatus;
import org.orbitalLogistic.maintenance.domain.model.enums.MaintenanceType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
public class MaintenanceLog {
    private final Long id;
    private final Long spacecraftId;
    private final MaintenanceType maintenanceType;
    private final Long performedByUserId;
    private final Long supervisedByUserId;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private final MaintenanceStatus status;
    private final String description;
    private final BigDecimal cost;

    public void validate() {
        if (spacecraftId == null) {
            throw new IllegalArgumentException("Spacecraft ID is required");
        }
        if (maintenanceType == null) {
            throw new IllegalArgumentException("Maintenance type is required");
        }
        if (performedByUserId == null) {
            throw new IllegalArgumentException("Performed by user ID is required");
        }
        if (cost != null && cost.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Cost must be non-negative");
        }
        if (startTime != null && endTime != null && endTime.isBefore(startTime)) {
            throw new IllegalArgumentException("End time cannot be before start time");
        }
    }

    public boolean isCompleted() {
        return status == MaintenanceStatus.COMPLETED;
    }

    public boolean isInProgress() {
        return status == MaintenanceStatus.IN_PROGRESS;
    }
}
