package org.orbitalLogistic.mission.infrastructure.adapters.out.persistence.mapper;

import org.orbitalLogistic.mission.domain.model.Mission;
import org.orbitalLogistic.mission.infrastructure.adapters.out.persistence.entity.MissionJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class MissionPersistenceMapper {

    public MissionJpaEntity toEntity(Mission domain) {
        if (domain == null) {
            return null;
        }
        
        return MissionJpaEntity.builder()
                .id(domain.getId())
                .missionCode(domain.getMissionCode())
                .missionName(domain.getMissionName())
                .missionType(domain.getMissionType())
                .status(domain.getStatus())
                .priority(domain.getPriority())
                .commandingOfficerId(domain.getCommandingOfficerId())
                .spacecraftId(domain.getSpacecraftId())
                .scheduledDeparture(domain.getScheduledDeparture())
                .scheduledArrival(domain.getScheduledArrival())
                .build();
    }

    public Mission toDomain(MissionJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        
        return Mission.builder()
                .id(entity.getId())
                .missionCode(entity.getMissionCode())
                .missionName(entity.getMissionName())
                .missionType(entity.getMissionType())
                .status(entity.getStatus())
                .priority(entity.getPriority())
                .commandingOfficerId(entity.getCommandingOfficerId())
                .spacecraftId(entity.getSpacecraftId())
                .scheduledDeparture(entity.getScheduledDeparture())
                .scheduledArrival(entity.getScheduledArrival())
                .build();
    }
}
