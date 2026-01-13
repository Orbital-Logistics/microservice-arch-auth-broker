package org.orbitalLogistic.spacecraft.application.ports.in;

import org.orbitalLogistic.spacecraft.domain.model.enums.SpacecraftClassification;

public record UpdateSpacecraftTypeCommand(
        Long id,
        String typeName,
        SpacecraftClassification classification,
        Integer maxCrewCapacity
) {
}

