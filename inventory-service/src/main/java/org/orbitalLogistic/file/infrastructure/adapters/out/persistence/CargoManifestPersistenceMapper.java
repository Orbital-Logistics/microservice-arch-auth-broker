package org.orbitalLogistic.file.infrastructure.adapters.out.persistence;

import org.orbitalLogistic.file.domain.model.CargoManifest;
import org.orbitalLogistic.file.domain.model.enums.ManifestPriority;
import org.orbitalLogistic.file.domain.model.enums.ManifestStatus;
import org.springframework.stereotype.Component;

@Component
public class CargoManifestPersistenceMapper {

    public CargoManifestEntity toEntity(CargoManifest manifest) {
        return CargoManifestEntity.builder()
                .id(manifest.getId())
                .spacecraftId(manifest.getSpacecraftId())
                .cargoId(manifest.getCargoId())
                .storageUnitId(manifest.getStorageUnitId())
                .quantity(manifest.getQuantity())
                .loadedAt(manifest.getLoadedAt())
                .unloadedAt(manifest.getUnloadedAt())
                .loadedByUserId(manifest.getLoadedByUserId())
                .unloadedByUserId(manifest.getUnloadedByUserId())
                .manifestStatus(manifest.getManifestStatus() != null ? manifest.getManifestStatus().name() : null)
                .priority(manifest.getPriority() != null ? manifest.getPriority().name() : null)
                .build();
    }

    public CargoManifest toDomain(CargoManifestEntity entity) {
        return CargoManifest.builder()
                .id(entity.getId())
                .spacecraftId(entity.getSpacecraftId())
                .cargoId(entity.getCargoId())
                .storageUnitId(entity.getStorageUnitId())
                .quantity(entity.getQuantity())
                .loadedAt(entity.getLoadedAt())
                .unloadedAt(entity.getUnloadedAt())
                .loadedByUserId(entity.getLoadedByUserId())
                .unloadedByUserId(entity.getUnloadedByUserId())
                .manifestStatus(entity.getManifestStatus() != null ? ManifestStatus.valueOf(entity.getManifestStatus()) : null)
                .priority(entity.getPriority() != null ? ManifestPriority.valueOf(entity.getPriority()) : null)
                .build();
    }
}
