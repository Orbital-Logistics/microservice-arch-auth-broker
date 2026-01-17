package org.orbitalLogistic.file.application.ports.in;

import org.orbitalLogistic.file.domain.model.CargoManifest;

public interface CreateCargoManifestUseCase {
    CargoManifest createManifest(CreateCargoManifestCommand command);
}
