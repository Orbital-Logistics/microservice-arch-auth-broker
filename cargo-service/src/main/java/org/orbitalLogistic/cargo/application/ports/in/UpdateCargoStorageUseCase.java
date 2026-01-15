package org.orbitalLogistic.cargo.application.ports.in;

import org.orbitalLogistic.cargo.domain.model.CargoStorage;

public interface UpdateCargoStorageUseCase {
    CargoStorage updateInventory(Long id, Integer quantity);
    CargoStorage checkInventory(Long id, Long userId);
}
