package org.orbitalLogistic.cargo.application.ports.in;

import org.orbitalLogistic.cargo.domain.model.StorageUnit;

public interface CreateStorageUnitUseCase {
    StorageUnit createUnit(StorageUnit storageUnit);
}
