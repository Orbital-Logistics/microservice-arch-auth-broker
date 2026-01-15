package org.orbitalLogistic.cargo.application.ports.in;

import org.orbitalLogistic.cargo.domain.model.CargoStorage;

public interface CreateCargoStorageUseCase {
    CargoStorage createStorage(CargoStorage cargoStorage);
}
