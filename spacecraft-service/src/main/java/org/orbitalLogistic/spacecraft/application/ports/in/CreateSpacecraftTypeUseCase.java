package org.orbitalLogistic.spacecraft.application.ports.in;

import org.orbitalLogistic.spacecraft.domain.model.SpacecraftType;

public interface CreateSpacecraftTypeUseCase {
    SpacecraftType createSpacecraftType(CreateSpacecraftTypeCommand command);
}

