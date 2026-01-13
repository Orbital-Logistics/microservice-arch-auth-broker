package org.orbitalLogistic.spacecraft.infrastructure.adapters.in.rest.dto;

import org.orbitalLogistic.spacecraft.domain.model.enums.SpacecraftClassification;

public record SpacecraftTypeResponseDTO(
        Long id,
        String typeName,
        SpacecraftClassification classification,
        Integer maxCrewCapacity
) {
}

