package org.orbitalLogistic.cargo.application.ports.in;

import org.orbitalLogistic.cargo.domain.model.Cargo;
import org.orbitalLogistic.cargo.domain.model.enums.CargoType;
import org.orbitalLogistic.cargo.domain.model.enums.HazardLevel;

import java.util.List;
import java.util.Optional;

public interface GetCargoUseCase {
    Optional<Cargo> getCargoById(Long id);
    List<Cargo> getAllCargos(int page, int size);
    List<Cargo> searchCargos(String name, CargoType cargoType, HazardLevel hazardLevel, int page, int size);
    boolean cargoExists(Long id);
}
