package org.orbitalLogistic.cargo.infrastructure.adapters.out.persistence.mapper;

import org.orbitalLogistic.cargo.domain.model.Cargo;
import org.orbitalLogistic.cargo.domain.model.enums.CargoType;
import org.orbitalLogistic.cargo.domain.model.enums.HazardLevel;
import org.orbitalLogistic.cargo.infrastructure.adapters.out.persistence.entity.CargoEntity;
import org.springframework.stereotype.Component;

@Component
public class CargoPersistenceMapper {

    public CargoEntity toEntity(Cargo domain) {
        if (domain == null) {
            return null;
        }

        return CargoEntity.builder()
                .id(domain.getId())
                .name(domain.getName())
                .cargoCategoryId(domain.getCargoCategoryId())
                .massPerUnit(domain.getMassPerUnit())
                .volumePerUnit(domain.getVolumePerUnit())
                .cargoType(domain.getCargoType() != null ? domain.getCargoType().name() : null)
                .hazardLevel(domain.getHazardLevel() != null ? domain.getHazardLevel().name() : null)
                .isActive(domain.getIsActive())
                .build();
    }

    public Cargo toDomain(CargoEntity entity) {
        if (entity == null) {
            return null;
        }

        return Cargo.builder()
                .id(entity.getId())
                .name(entity.getName())
                .cargoCategoryId(entity.getCargoCategoryId())
                .massPerUnit(entity.getMassPerUnit())
                .volumePerUnit(entity.getVolumePerUnit())
                .cargoType(entity.getCargoType() != null ? CargoType.valueOf(entity.getCargoType()) : null)
                .hazardLevel(entity.getHazardLevel() != null ? HazardLevel.valueOf(entity.getHazardLevel()) : null)
                .isActive(entity.getIsActive())
                .build();
    }
}
