package org.orbitalLogistic.file.application.ports.out;

import org.orbitalLogistic.file.domain.model.CargoManifest;

import java.util.List;
import java.util.Optional;

public interface CargoManifestRepository {
    CargoManifest save(CargoManifest manifest);
    Optional<CargoManifest> findById(Long id);
    List<CargoManifest> findAll(int limit, int offset);
    List<CargoManifest> findBySpacecraftId(Long spacecraftId, int limit, int offset);
    long countAll();
    long countBySpacecraftId(Long spacecraftId);
}
