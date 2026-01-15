package org.orbitalLogistic.cargo.application.usecases;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.orbitalLogistic.cargo.application.ports.in.UpdateCargoStorageUseCase;
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
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class UpdateCargoStorageService implements UpdateCargoStorageUseCase {

    private final CargoStorageRepository cargoStorageRepository;
    private final CargoRepository cargoRepository;
    private final StorageUnitRepository storageUnitRepository;
    private final UserServicePort userServicePort;

    @Override
    @Transactional
    public CargoStorage updateInventory(Long id, Integer newQuantity) {
        log.debug("Updating inventory for storage id: {} to quantity: {}", id, newQuantity);

        CargoStorage storage = cargoStorageRepository.findById(id)
                .orElseThrow(() -> new CargoStorageNotFoundException("Cargo storage not found with id: " + id));

        Cargo cargo = cargoRepository.findById(storage.getCargoId())
                .orElseThrow(() -> new CargoNotFoundException("Cargo not found with id: " + storage.getCargoId()));

        StorageUnit storageUnit = storageUnitRepository.findById(storage.getStorageUnitId())
                .orElseThrow(() -> new StorageUnitNotFoundException("Storage unit not found with id: " + storage.getStorageUnitId()));

        int quantityDiff = newQuantity - storage.getQuantity();
        BigDecimal massDiff = cargo.getMassPerUnit().multiply(BigDecimal.valueOf(quantityDiff));
        BigDecimal volumeDiff = cargo.getVolumePerUnit().multiply(BigDecimal.valueOf(quantityDiff));

        if (quantityDiff > 0) {
            BigDecimal availableMass = storageUnit.getMaxMass().subtract(storageUnit.getCurrentMass());
            BigDecimal availableVolume = storageUnit.getMaxVolume().subtract(storageUnit.getCurrentVolume());

            if (massDiff.compareTo(availableMass) > 0) {
                throw new InsufficientCapacityException(
                        "Insufficient mass capacity. Required: " + massDiff + ", available: " + availableMass
                );
            }

            if (volumeDiff.compareTo(availableVolume) > 0) {
                throw new InsufficientCapacityException(
                        "Insufficient volume capacity. Required: " + volumeDiff + ", available: " + availableVolume
                );
            }
        }

        storageUnit.setCurrentMass(storageUnit.getCurrentMass().add(massDiff));
        storageUnit.setCurrentVolume(storageUnit.getCurrentVolume().add(volumeDiff));
        storageUnitRepository.save(storageUnit);

        storage.setQuantity(newQuantity);
        CargoStorage updated = cargoStorageRepository.save(storage);
        log.info("Inventory updated for storage id: {}", updated.getId());
        return updated;
    }

    @Override
    @Transactional
    public CargoStorage checkInventory(Long id, Long userId) {
        log.debug("Checking inventory for storage id: {} by user: {}", id, userId);

        CargoStorage storage = cargoStorageRepository.findById(id)
                .orElseThrow(() -> new CargoStorageNotFoundException("Cargo storage not found with id: " + id));

        if (!userServicePort.userExists(userId)) {
            throw new UserNotFoundException("User not found with id: " + userId);
        }

        storage.setLastInventoryCheck(LocalDateTime.now());
        storage.setResponsibleUserId(userId);
        CargoStorage updated = cargoStorageRepository.save(storage);
        log.info("Inventory checked for storage id: {}", updated.getId());
        return updated;
    }
}
