package org.orbitalLogistic.cargo.application.ports.in;

import org.orbitalLogistic.cargo.domain.model.StorageUnit;

import java.util.List;
import java.util.Optional;

public interface GetStorageUnitUseCase {
    Optional<StorageUnit> getUnitById(Long id);
    List<StorageUnit> getAllUnits(int page, int size);
    Optional<StorageUnit> getByUnitCode(String code);
    List<StorageUnit> getByLocation(String location, int page, int size);
}
