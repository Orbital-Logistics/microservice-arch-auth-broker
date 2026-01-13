package org.orbitalLogistic.spacecraft.application.ports.in;

import org.orbitalLogistic.spacecraft.domain.model.Spacecraft;

public interface UpdateSpacecraftUseCase {
    Spacecraft updateSpacecraft(UpdateSpacecraftCommand command);
}

