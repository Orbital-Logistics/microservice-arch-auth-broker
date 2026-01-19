package org.orbitalLogistic.file.application.ports.in;

import org.orbitalLogistic.file.domain.model.CargoManifest;

import java.util.List;

public interface GetCargoManifestsUseCase {
    List<CargoManifest> getAllManifests(int page, int size);
    List<CargoManifest> getManifestsBySpacecraft(Long spacecraftId, int page, int size);
    CargoManifest getManifestById(Long id);
    long countAllManifests();
    long countManifestsBySpacecraft(Long spacecraftId);
}
