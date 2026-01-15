package org.orbitalLogistic.cargo.infrastructure.adapters.out.persistence.mapper;

import org.orbitalLogistic.cargo.domain.model.StorageUnit;
import org.orbitalLogistic.cargo.domain.model.enums.StorageTypeEnum;
import org.orbitalLogistic.cargo.infrastructure.adapters.out.persistence.entity.StorageUnitEntity;
import org.springframework.stereotype.Component;

@Component
public class StorageUnitPersistenceMapper {

    public StorageUnitEntity toEntity(StorageUnit domain) {
        if (domain == null) {
            return null;
        }

        return StorageUnitEntity.builder()
                .id(domain.getId())
                .unitCode(domain.getUnitCode())
                .location(domain.getLocation())
                .storageType(domain.getStorageType() != null ? domain.getStorageType().name() : null)
                .totalMassCapacity(domain.getMaxMass())
                .totalVolumeCapacity(domain.getMaxVolume())
                .currentMass(domain.getCurrentMass())
                .currentVolume(domain.getCurrentVolume())
                .build();
    }

    public StorageUnit toDomain(StorageUnitEntity entity) {
        if (entity == null) {
            return null;
        }

        return StorageUnit.builder()
                .id(entity.getId())
                .unitCode(entity.getUnitCode())
                .location(entity.getLocation())
                .storageType(entity.getStorageType() != null ? StorageTypeEnum.valueOf(entity.getStorageType()) : null)
                .maxMass(entity.getTotalMassCapacity())
                .maxVolume(entity.getTotalVolumeCapacity())
                .currentMass(entity.getCurrentMass())
                .currentVolume(entity.getCurrentVolume())
                .build();
    }
}
