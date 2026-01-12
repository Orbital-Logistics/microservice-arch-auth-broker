package org.orbitalLogistic.maintenance.infrastructure.adapters.out.persistence;

import org.orbitalLogistic.maintenance.domain.model.MaintenanceLog;
import org.orbitalLogistic.maintenance.domain.model.enums.MaintenanceStatus;
import org.orbitalLogistic.maintenance.domain.model.enums.MaintenanceType;
import org.springframework.stereotype.Component;

@Component
public class MaintenanceLogPersistenceMapper {

    public MaintenanceLogEntity toEntity(MaintenanceLog domain) {
        if (domain == null) {
            return null;
        }

        return MaintenanceLogEntity.builder()
                .id(domain.getId())
                .spacecraftId(domain.getSpacecraftId())
                .maintenanceType(domain.getMaintenanceType() != null ? domain.getMaintenanceType().name() : null)
                .performedByUserId(domain.getPerformedByUserId())
                .supervisedByUserId(domain.getSupervisedByUserId())
                .startTime(domain.getStartTime())
                .endTime(domain.getEndTime())
                .status(domain.getStatus() != null ? domain.getStatus().name() : "SCHEDULED")
                .description(domain.getDescription())
                .cost(domain.getCost())
                .build();
    }

    public MaintenanceLog toDomain(MaintenanceLogEntity entity) {
        if (entity == null) {
            return null;
        }

        return MaintenanceLog.builder()
                .id(entity.getId())
                .spacecraftId(entity.getSpacecraftId())
                .maintenanceType(entity.getMaintenanceType() != null 
                        ? MaintenanceType.valueOf(entity.getMaintenanceType()) 
                        : null)
                .performedByUserId(entity.getPerformedByUserId())
                .supervisedByUserId(entity.getSupervisedByUserId())
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .status(entity.getStatus() != null 
                        ? MaintenanceStatus.valueOf(entity.getStatus()) 
                        : MaintenanceStatus.SCHEDULED)
                .description(entity.getDescription())
                .cost(entity.getCost())
                .build();
    }
}
