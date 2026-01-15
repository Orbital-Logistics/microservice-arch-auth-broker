package org.orbitalLogistic.cargo.infrastructure.adapters.out.persistence.mapper;

import org.orbitalLogistic.cargo.domain.model.CargoCategory;
import org.orbitalLogistic.cargo.infrastructure.adapters.out.persistence.entity.CargoCategoryEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CargoCategoryPersistenceMapper {

    public CargoCategoryEntity toEntity(CargoCategory domain) {
        if (domain == null) {
            return null;
        }

        return CargoCategoryEntity.builder()
                .id(domain.getId())
                .name(domain.getName())
                .parentCategoryId(domain.getParentCategoryId())
                .description(domain.getDescription())
                .build();
    }

    public CargoCategory toDomain(CargoCategoryEntity entity) {
        if (entity == null) {
            return null;
        }

        return CargoCategory.builder()
                .id(entity.getId())
                .name(entity.getName())
                .parentCategoryId(entity.getParentCategoryId())
                .description(entity.getDescription())
                .build();
    }

    public List<CargoCategory> toDomainList(List<CargoCategoryEntity> entities) {
        if (entities == null) {
            return null;
        }

        return entities.stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }
}
