package org.orbitalLogistic.spacecraft.dto.response;

import org.orbitalLogistic.spacecraft.entities.enums.SpacecraftClassification;

public record SpacecraftTypeResponseDTO(
    Long id,
    String typeName,
    SpacecraftClassification classification,
    Integer maxCrewCapacity
) {}

