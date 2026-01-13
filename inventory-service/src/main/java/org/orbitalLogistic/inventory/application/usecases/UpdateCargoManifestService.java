package org.orbitalLogistic.inventory.application.usecases;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.orbitalLogistic.inventory.application.ports.in.UpdateCargoManifestCommand;
import org.orbitalLogistic.inventory.application.ports.in.UpdateCargoManifestUseCase;
import org.orbitalLogistic.inventory.application.ports.out.CargoManifestRepository;
import org.orbitalLogistic.inventory.application.ports.out.CargoValidationPort;
import org.orbitalLogistic.inventory.application.ports.out.SpacecraftValidationPort;
import org.orbitalLogistic.inventory.application.ports.out.UserValidationPort;
import org.orbitalLogistic.inventory.domain.model.CargoManifest;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateCargoManifestService implements UpdateCargoManifestUseCase {

    private final CargoManifestRepository cargoManifestRepository;
    private final CargoValidationPort cargoValidationPort;
    private final SpacecraftValidationPort spacecraftValidationPort;
    private final UserValidationPort userValidationPort;

    @Override
    public CargoManifest updateManifest(UpdateCargoManifestCommand command) {
        log.debug("Updating cargo manifest with id: {}", command.id());

        CargoManifest existing = cargoManifestRepository.findById(command.id())
                .orElseThrow(() -> new IllegalArgumentException("Cargo manifest not found with id: " + command.id()));

        Long spacecraftId = command.spacecraftId() != null ? command.spacecraftId() : existing.getSpacecraftId();
        if (!spacecraftValidationPort.spacecraftExists(spacecraftId)) {
            throw new IllegalArgumentException("Spacecraft not found with id: " + spacecraftId);
        }

        Long cargoId = command.cargoId() != null ? command.cargoId() : existing.getCargoId();
        if (!cargoValidationPort.cargoExists(cargoId)) {
            throw new IllegalArgumentException("Cargo not found with id: " + cargoId);
        }

        Long storageUnitId = command.storageUnitId() != null ? command.storageUnitId() : existing.getStorageUnitId();
        if (!cargoValidationPort.storageUnitExists(storageUnitId)) {
            throw new IllegalArgumentException("Storage unit not found with id: " + storageUnitId);
        }

        Long loadedByUserId = command.loadedByUserId() != null ? command.loadedByUserId() : existing.getLoadedByUserId();
        if (!userValidationPort.userExists(loadedByUserId)) {
            throw new IllegalArgumentException("Loaded by user not found with id: " + loadedByUserId);
        }

        Long unloadedByUserId = command.unloadedByUserId() != null ? command.unloadedByUserId() : existing.getUnloadedByUserId();
        if (unloadedByUserId != null && !userValidationPort.userExists(unloadedByUserId)) {
            throw new IllegalArgumentException("Unloaded by user not found with id: " + unloadedByUserId);
        }

        CargoManifest updatedManifest = existing.toBuilder()
                .spacecraftId(spacecraftId)
                .cargoId(cargoId)
                .storageUnitId(storageUnitId)
                .quantity(command.quantity() != null ? command.quantity() : existing.getQuantity())
                .loadedAt(command.loadedAt() != null ? command.loadedAt() : existing.getLoadedAt())
                .unloadedAt(command.unloadedAt() != null ? command.unloadedAt() : existing.getUnloadedAt())
                .loadedByUserId(loadedByUserId)
                .unloadedByUserId(unloadedByUserId)
                .manifestStatus(command.manifestStatus() != null ? command.manifestStatus() : existing.getManifestStatus())
                .priority(command.priority() != null ? command.priority() : existing.getPriority())
                .build();

        updatedManifest.validate();

        CargoManifest savedManifest = cargoManifestRepository.save(updatedManifest);
        log.info("Updated cargo manifest with id: {}", savedManifest.getId());

        return savedManifest;
    }
}
