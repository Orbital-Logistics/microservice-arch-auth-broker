package org.orbitalLogistic.mission.infrastructure.adapters.out.persistence.mapper;

import org.orbitalLogistic.mission.domain.model.MissionAssignment;
import org.orbitalLogistic.mission.infrastructure.adapters.out.persistence.entity.MissionAssignmentJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class MissionAssignmentPersistenceMapper {

    public MissionAssignmentJpaEntity toEntity(MissionAssignment domain) {
        if (domain == null) {
            return null;
        }
        
        return MissionAssignmentJpaEntity.builder()
                .id(domain.getId())
                .missionId(domain.getMissionId())
                .userId(domain.getUserId())
                .assignedAt(domain.getAssignedAt())
                .assignmentRole(domain.getAssignmentRole())
                .responsibilityZone(domain.getResponsibilityZone())
                .build();
    }

    public MissionAssignment toDomain(MissionAssignmentJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        
        return MissionAssignment.builder()
                .id(entity.getId())
                .missionId(entity.getMissionId())
                .userId(entity.getUserId())
                .assignedAt(entity.getAssignedAt())
                .assignmentRole(entity.getAssignmentRole())
                .responsibilityZone(entity.getResponsibilityZone())
                .build();
    }
}
