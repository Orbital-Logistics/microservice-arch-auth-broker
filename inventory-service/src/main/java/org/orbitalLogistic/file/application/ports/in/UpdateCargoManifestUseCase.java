package org.orbitalLogistic.file.application.ports.in;

import org.orbitalLogistic.file.domain.model.CargoManifest;

public interface UpdateCargoManifestUseCase {
    CargoManifest updateManifest(UpdateCargoManifestCommand command);
}
