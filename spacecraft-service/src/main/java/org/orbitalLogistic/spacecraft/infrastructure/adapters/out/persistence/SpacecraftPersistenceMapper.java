package org.orbitalLogistic.spacecraft.infrastructure.adapters.out.persistence;

import org.orbitalLogistic.spacecraft.domain.model.Spacecraft;
import org.orbitalLogistic.spacecraft.domain.model.enums.SpacecraftStatus;
import org.springframework.stereotype.Component;

@Component
public class SpacecraftPersistenceMapper {

    public SpacecraftEntity toEntity(Spacecraft spacecraft) {
        return SpacecraftEntity.builder()
                .id(spacecraft.getId())
                .registryCode(spacecraft.getRegistryCode())
                .name(spacecraft.getName())
                .spacecraftTypeId(spacecraft.getSpacecraftTypeId())
                .massCapacity(spacecraft.getMassCapacity())
                .volumeCapacity(spacecraft.getVolumeCapacity())
                .status(spacecraft.getStatus() != null ? spacecraft.getStatus().name() : null)
                .currentLocation(spacecraft.getCurrentLocation())
                .build();
    }

    public Spacecraft toDomain(SpacecraftEntity entity) {
        return Spacecraft.builder()
                .id(entity.getId())
                .registryCode(entity.getRegistryCode())
                .name(entity.getName())
                .spacecraftTypeId(entity.getSpacecraftTypeId())
                .massCapacity(entity.getMassCapacity())
                .volumeCapacity(entity.getVolumeCapacity())
                .status(entity.getStatus() != null ? SpacecraftStatus.valueOf(entity.getStatus()) : null)
                .currentLocation(entity.getCurrentLocation())
                .build();
    }
}

