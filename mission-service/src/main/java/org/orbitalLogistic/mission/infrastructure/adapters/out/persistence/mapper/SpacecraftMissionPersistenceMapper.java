package org.orbitalLogistic.mission.infrastructure.adapters.out.persistence.mapper;

import org.orbitalLogistic.mission.domain.model.SpacecraftMission;
import org.orbitalLogistic.mission.infrastructure.adapters.out.persistence.entity.SpacecraftMissionJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class SpacecraftMissionPersistenceMapper {

    public SpacecraftMissionJpaEntity toEntity(SpacecraftMission domain) {
        if (domain == null) {
            return null;
        }
        
        return SpacecraftMissionJpaEntity.builder()
                .id(domain.getId())
                .spacecraftId(domain.getSpacecraftId())
                .missionId(domain.getMissionId())
                .build();
    }

    public SpacecraftMission toDomain(SpacecraftMissionJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        
        return SpacecraftMission.builder()
                .id(entity.getId())
                .spacecraftId(entity.getSpacecraftId())
                .missionId(entity.getMissionId())
                .build();
    }
}
