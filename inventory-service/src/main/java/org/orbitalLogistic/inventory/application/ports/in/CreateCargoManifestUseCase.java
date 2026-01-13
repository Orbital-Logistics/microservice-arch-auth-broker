package org.orbitalLogistic.inventory.application.ports.in;

import org.orbitalLogistic.inventory.domain.model.CargoManifest;

public interface CreateCargoManifestUseCase {
    CargoManifest createManifest(CreateCargoManifestCommand command);
}
