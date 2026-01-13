package org.orbitalLogistic.spacecraft.application.ports.in;

import org.orbitalLogistic.spacecraft.domain.model.Spacecraft;
import org.orbitalLogistic.spacecraft.domain.model.enums.SpacecraftStatus;

public interface UpdateSpacecraftStatusUseCase {
    Spacecraft updateStatus(Long id, SpacecraftStatus status);
    Spacecraft changeLocation(Long id, String newLocation);
}

