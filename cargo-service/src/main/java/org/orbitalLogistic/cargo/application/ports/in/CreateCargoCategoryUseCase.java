package org.orbitalLogistic.cargo.application.ports.in;

import org.orbitalLogistic.cargo.domain.model.CargoCategory;

public interface CreateCargoCategoryUseCase {
    CargoCategory createCategory(CargoCategory cargoCategory);
}
