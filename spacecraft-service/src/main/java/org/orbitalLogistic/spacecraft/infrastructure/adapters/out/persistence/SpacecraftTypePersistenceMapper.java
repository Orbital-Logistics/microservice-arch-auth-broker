package org.orbitalLogistic.spacecraft.infrastructure.adapters.out.persistence;

import org.orbitalLogistic.spacecraft.domain.model.SpacecraftType;
import org.orbitalLogistic.spacecraft.domain.model.enums.SpacecraftClassification;
import org.springframework.stereotype.Component;

@Component
public class SpacecraftTypePersistenceMapper {

    public SpacecraftTypeEntity toEntity(SpacecraftType spacecraftType) {
        return SpacecraftTypeEntity.builder()
                .id(spacecraftType.getId())
                .typeName(spacecraftType.getTypeName())
                .classification(spacecraftType.getClassification() != null ?
                        spacecraftType.getClassification().name() : null)
                .maxCrewCapacity(spacecraftType.getMaxCrewCapacity())
                .build();
    }

    public SpacecraftType toDomain(SpacecraftTypeEntity entity) {
        return SpacecraftType.builder()
                .id(entity.getId())
                .typeName(entity.getTypeName())
                .classification(entity.getClassification() != null ?
                        SpacecraftClassification.valueOf(entity.getClassification()) : null)
                .maxCrewCapacity(entity.getMaxCrewCapacity())
                .build();
    }
}

