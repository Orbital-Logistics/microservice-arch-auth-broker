package org.orbitalLogistic.cargo.application.ports.in;

import org.orbitalLogistic.cargo.domain.model.Cargo;

public interface CreateCargoUseCase {
    Cargo createCargo(Cargo cargo);
}
