package org.orbitalLogistic.cargo.application.usecases;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.orbitalLogistic.cargo.application.ports.in.CreateCargoUseCase;
import org.orbitalLogistic.cargo.application.ports.out.CargoRepository;
import org.orbitalLogistic.cargo.application.ports.out.CargoCategoryRepository;
import org.orbitalLogistic.cargo.domain.exception.CargoAlreadyExistsException;
import org.orbitalLogistic.cargo.domain.exception.CargoCategoryNotFoundException;
import org.orbitalLogistic.cargo.domain.model.Cargo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreateCargoService implements CreateCargoUseCase {

    private final CargoRepository cargoRepository;
    private final CargoCategoryRepository cargoCategoryRepository;

    @Override
    @Transactional
    public Cargo createCargo(Cargo cargo) {
        log.debug("Creating cargo with name: {}", cargo.getName());

        if (cargoRepository.existsByName(cargo.getName())) {
            throw new CargoAlreadyExistsException("Cargo with name already exists: " + cargo.getName());
        }

        if (!cargoCategoryRepository.existsById(cargo.getCargoCategoryId())) {
            throw new CargoCategoryNotFoundException("Cargo category not found with id: " + cargo.getCargoCategoryId());
        }

        Cargo saved = cargoRepository.save(cargo);
        log.info("Cargo created with id: {}", saved.getId());
        return saved;
    }
}
