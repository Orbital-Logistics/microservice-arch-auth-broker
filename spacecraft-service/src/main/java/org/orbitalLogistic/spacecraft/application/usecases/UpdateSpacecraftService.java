package org.orbitalLogistic.spacecraft.application.usecases;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.orbitalLogistic.spacecraft.application.ports.in.UpdateSpacecraftCommand;
import org.orbitalLogistic.spacecraft.application.ports.in.UpdateSpacecraftUseCase;
import org.orbitalLogistic.spacecraft.application.ports.out.SpacecraftRepository;
import org.orbitalLogistic.spacecraft.application.ports.out.SpacecraftTypeRepository;
import org.orbitalLogistic.spacecraft.domain.model.Spacecraft;
import org.orbitalLogistic.spacecraft.exceptions.DataNotFoundException;
import org.orbitalLogistic.spacecraft.exceptions.SpacecraftAlreadyExistsException;
import org.orbitalLogistic.spacecraft.exceptions.SpacecraftNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateSpacecraftService implements UpdateSpacecraftUseCase {

    private final SpacecraftRepository spacecraftRepository;
    private final SpacecraftTypeRepository spacecraftTypeRepository;

    @Override
    public Spacecraft updateSpacecraft(UpdateSpacecraftCommand command) {
        log.debug("Updating spacecraft with id: {}", command.id());

        Spacecraft existing = spacecraftRepository.findById(command.id())
                .orElseThrow(() -> new SpacecraftNotFoundException("Spacecraft not found with id: " + command.id()));

        if (!existing.getRegistryCode().equals(command.registryCode()) &&
            spacecraftRepository.existsByRegistryCode(command.registryCode())) {
            throw new SpacecraftAlreadyExistsException(
                "Spacecraft with registry code already exists: " + command.registryCode()
            );
        }

        if (!spacecraftTypeRepository.existsById(command.spacecraftTypeId())) {
            throw new DataNotFoundException("Spacecraft type not found with id: " + command.spacecraftTypeId());
        }

        Spacecraft updated = Spacecraft.builder()
                .id(command.id())
                .registryCode(command.registryCode())
                .name(command.name())
                .spacecraftTypeId(command.spacecraftTypeId())
                .massCapacity(command.massCapacity())
                .volumeCapacity(command.volumeCapacity())
                .status(command.status() != null ? command.status() : existing.getStatus())
                .currentLocation(command.currentLocation())
                .build();

        updated.validate();

        Spacecraft saved = spacecraftRepository.save(updated);
        log.info("Updated spacecraft with id: {}", saved.getId());

        return saved;
    }
}

