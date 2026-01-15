package org.orbitalLogistic.cargo.application.ports.in;

import org.orbitalLogistic.cargo.domain.model.StorageUnit;

public interface UpdateStorageUnitUseCase {
    StorageUnit updateUnit(Long id, StorageUnit storageUnit);
    StorageUnit recalculateCapacity(Long id);
}
