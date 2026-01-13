package org.orbitalLogistic.spacecraft.application.ports.in;

import org.orbitalLogistic.spacecraft.domain.model.enums.SpacecraftClassification;

public record CreateSpacecraftTypeCommand(
        String typeName,
        SpacecraftClassification classification,
        Integer maxCrewCapacity
) {
}

