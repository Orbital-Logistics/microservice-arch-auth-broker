package org.orbitalLogistic.cargo.application.usecases;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.orbitalLogistic.cargo.application.ports.in.CreateCargoStorageUseCase;
import org.orbitalLogistic.cargo.application.ports.out.CargoRepository;
import org.orbitalLogistic.cargo.application.ports.out.CargoStorageRepository;
import org.orbitalLogistic.cargo.application.ports.out.StorageUnitRepository;
import org.orbitalLogistic.cargo.application.ports.out.UserServicePort;
import org.orbitalLogistic.cargo.domain.exception.*;
import org.orbitalLogistic.cargo.domain.model.Cargo;
import org.orbitalLogistic.cargo.domain.model.CargoStorage;
import org.orbitalLogistic.cargo.domain.model.StorageUnit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreateCargoStorageService implements CreateCargoStorageUseCase {

    private final CargoStorageRepository cargoStorageRepository;
    private final CargoRepository cargoRepository;
    private final StorageUnitRepository storageUnitRepository;
    private final UserServicePort userServicePort;

    @Override
    @Transactional
    public CargoStorage createStorage(CargoStorage storage) {
        log.debug("Creating cargo storage for cargo id: {}, storage unit id: {}", 
                storage.getCargoId(), storage.getStorageUnitId());

        Cargo cargo = cargoRepository.findById(storage.getCargoId())
                .orElseThrow(() -> new CargoNotFoundException("Cargo not found with id: " + storage.getCargoId()));

        StorageUnit storageUnit = storageUnitRepository.findById(storage.getStorageUnitId())
                .orElseThrow(() -> new StorageUnitNotFoundException("Storage unit not found with id: " + storage.getStorageUnitId()));

        if (storage.getLastCheckedByUserId() != null && !userServicePort.userExists(storage.getLastCheckedByUserId())) {
            throw new UserNotFoundException("User not found with id: " + storage.getLastCheckedByUserId());
        }

        BigDecimal requiredMass = cargo.getMassPerUnit()
                .multiply(BigDecimal.valueOf(storage.getQuantity()));
        BigDecimal requiredVolume = cargo.getVolumePerUnit()
                .multiply(BigDecimal.valueOf(storage.getQuantity()));

        BigDecimal availableMass = storageUnit.getMaxMass().subtract(storageUnit.getCurrentMass());
        BigDecimal availableVolume = storageUnit.getMaxVolume().subtract(storageUnit.getCurrentVolume());

        if (requiredMass.compareTo(availableMass) > 0) {
            throw new InsufficientCapacityException(
                    "Insufficient mass capacity. Required: " + requiredMass + ", available: " + availableMass
            );
        }

        if (requiredVolume.compareTo(availableVolume) > 0) {
            throw new InsufficientCapacityException(
                    "Insufficient volume capacity. Required: " + requiredVolume + ", available: " + availableVolume
            );
        }

        storageUnit.setCurrentMass(storageUnit.getCurrentMass().add(requiredMass));
        storageUnit.setCurrentVolume(storageUnit.getCurrentVolume().add(requiredVolume));
        storageUnitRepository.save(storageUnit);

        CargoStorage saved = cargoStorageRepository.save(storage);
        log.info("Cargo storage created with id: {}", saved.getId());
        return saved;
    }
}
