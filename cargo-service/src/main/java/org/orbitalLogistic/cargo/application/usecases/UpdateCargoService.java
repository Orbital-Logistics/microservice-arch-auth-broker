package org.orbitalLogistic.cargo.application.usecases;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.orbitalLogistic.cargo.application.ports.in.UpdateCargoUseCase;
import org.orbitalLogistic.cargo.application.ports.out.CargoRepository;
import org.orbitalLogistic.cargo.application.ports.out.CargoCategoryRepository;
import org.orbitalLogistic.cargo.domain.exception.CargoAlreadyExistsException;
import org.orbitalLogistic.cargo.domain.exception.CargoCategoryNotFoundException;
import org.orbitalLogistic.cargo.domain.exception.CargoNotFoundException;
import org.orbitalLogistic.cargo.domain.model.Cargo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UpdateCargoService implements UpdateCargoUseCase {

    private final CargoRepository cargoRepository;
    private final CargoCategoryRepository cargoCategoryRepository;

    @Override
    @Transactional
    public Cargo updateCargo(Long id, Cargo cargo) {
        log.debug("Updating cargo with id: {}", id);

        Cargo existing = cargoRepository.findById(id)
                .orElseThrow(() -> new CargoNotFoundException("Cargo not found with id: " + id));

        if (!existing.getName().equals(cargo.getName()) && cargoRepository.existsByName(cargo.getName())) {
            throw new CargoAlreadyExistsException("Cargo with name already exists: " + cargo.getName());
        }

        if (!cargoCategoryRepository.existsById(cargo.getCargoCategoryId())) {
            throw new CargoCategoryNotFoundException("Cargo category not found with id: " + cargo.getCargoCategoryId());
        }

        existing.setName(cargo.getName());
        existing.setCargoCategoryId(cargo.getCargoCategoryId());
        existing.setMassPerUnit(cargo.getMassPerUnit());
        existing.setVolumePerUnit(cargo.getVolumePerUnit());
        existing.setCargoType(cargo.getCargoType());
        existing.setHazardLevel(cargo.getHazardLevel());
        existing.setIsActive(cargo.getIsActive());

        Cargo updated = cargoRepository.save(existing);
        log.info("Cargo updated with id: {}", updated.getId());
        return updated;
    }
}
