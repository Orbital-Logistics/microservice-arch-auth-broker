package org.orbitalLogistic.inventory.application.usecases;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.orbitalLogistic.inventory.application.ports.in.CreateCargoManifestCommand;
import org.orbitalLogistic.inventory.application.ports.in.CreateCargoManifestUseCase;
import org.orbitalLogistic.inventory.application.ports.out.CargoManifestRepository;
import org.orbitalLogistic.inventory.application.ports.out.CargoValidationPort;
import org.orbitalLogistic.inventory.application.ports.out.SpacecraftValidationPort;
import org.orbitalLogistic.inventory.application.ports.out.UserValidationPort;
import org.orbitalLogistic.inventory.domain.model.CargoManifest;
import org.orbitalLogistic.inventory.domain.model.enums.ManifestPriority;
import org.orbitalLogistic.inventory.domain.model.enums.ManifestStatus;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateCargoManifestService implements CreateCargoManifestUseCase {

    private final CargoManifestRepository cargoManifestRepository;
    private final CargoValidationPort cargoValidationPort;
    private final SpacecraftValidationPort spacecraftValidationPort;
    private final UserValidationPort userValidationPort;

    @Override
    public CargoManifest createManifest(CreateCargoManifestCommand command) {
        log.debug("Creating cargo manifest for spacecraft: {}, cargo: {}", command.spacecraftId(), command.cargoId());

        if (!spacecraftValidationPort.spacecraftExists(command.spacecraftId())) {
            throw new IllegalArgumentException("Spacecraft not found with id: " + command.spacecraftId());
        }

        if (!cargoValidationPort.cargoExists(command.cargoId())) {
            throw new IllegalArgumentException("Cargo not found with id: " + command.cargoId());
        }

        if (!cargoValidationPort.storageUnitExists(command.storageUnitId())) {
            throw new IllegalArgumentException("Storage unit not found with id: " + command.storageUnitId());
        }

        if (!userValidationPort.userExists(command.loadedByUserId())) {
            throw new IllegalArgumentException("Loaded by user not found with id: " + command.loadedByUserId());
        }

        if (command.unloadedByUserId() != null && !userValidationPort.userExists(command.unloadedByUserId())) {
            throw new IllegalArgumentException("Unloaded by user not found with id: " + command.unloadedByUserId());
        }

        CargoManifest manifest = CargoManifest.builder()
                .spacecraftId(command.spacecraftId())
                .cargoId(command.cargoId())
                .storageUnitId(command.storageUnitId())
                .quantity(command.quantity())
                .loadedAt(command.loadedAt())
                .unloadedAt(command.unloadedAt())
                .loadedByUserId(command.loadedByUserId())
                .unloadedByUserId(command.unloadedByUserId())
                .manifestStatus(command.manifestStatus() != null ? command.manifestStatus() : ManifestStatus.PENDING)
                .priority(command.priority() != null ? command.priority() : ManifestPriority.NORMAL)
                .build();

        manifest.validate();

        CargoManifest savedManifest = cargoManifestRepository.save(manifest);
        log.info("Created cargo manifest with id: {}", savedManifest.getId());

        return savedManifest;
    }
}
