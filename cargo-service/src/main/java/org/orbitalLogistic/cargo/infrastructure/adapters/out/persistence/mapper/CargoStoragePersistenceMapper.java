package org.orbitalLogistic.cargo.infrastructure.adapters.out.persistence.mapper;

import org.orbitalLogistic.cargo.domain.model.CargoStorage;
import org.orbitalLogistic.cargo.infrastructure.adapters.out.persistence.entity.CargoStorageEntity;
import org.springframework.stereotype.Component;

@Component
public class CargoStoragePersistenceMapper {

    public CargoStorageEntity toEntity(CargoStorage domain) {
        if (domain == null) {
            return null;
        }

        return CargoStorageEntity.builder()
                .id(domain.getId())
                .storageUnitId(domain.getStorageUnitId())
                .cargoId(domain.getCargoId())
                .quantity(domain.getQuantity())
                .storedAt(domain.getStoredAt())
                .lastInventoryCheck(domain.getLastInventoryCheck())
                .lastCheckedByUserId(domain.getLastCheckedByUserId())
                .build();
    }

    public CargoStorage toDomain(CargoStorageEntity entity) {
        if (entity == null) {
            return null;
        }

        return CargoStorage.builder()
                .id(entity.getId())
                .storageUnitId(entity.getStorageUnitId())
                .cargoId(entity.getCargoId())
                .quantity(entity.getQuantity())
                .storedAt(entity.getStoredAt())
                .lastInventoryCheck(entity.getLastInventoryCheck())
                .lastCheckedByUserId(entity.getLastCheckedByUserId())
                .build();
    }
}
