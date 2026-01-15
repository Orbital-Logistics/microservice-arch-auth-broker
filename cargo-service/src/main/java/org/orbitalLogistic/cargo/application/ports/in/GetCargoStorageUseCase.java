package org.orbitalLogistic.cargo.application.ports.in;

import org.orbitalLogistic.cargo.domain.model.CargoStorage;

import java.util.List;
import java.util.Optional;

public interface GetCargoStorageUseCase {
    Optional<CargoStorage> getStorageById(Long id);
    List<CargoStorage> getAllStorages(int page, int size);
    List<CargoStorage> searchStorages(Long storageUnitId, Long cargoId, int page, int size);
}
