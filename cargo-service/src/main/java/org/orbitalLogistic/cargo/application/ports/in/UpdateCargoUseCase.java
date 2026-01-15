package org.orbitalLogistic.cargo.application.ports.in;

import org.orbitalLogistic.cargo.domain.model.Cargo;

public interface UpdateCargoUseCase {
    Cargo updateCargo(Long id, Cargo cargo);
}
