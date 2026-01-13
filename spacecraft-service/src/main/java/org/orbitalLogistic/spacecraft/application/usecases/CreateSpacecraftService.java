package org.orbitalLogistic.spacecraft.application.usecases;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.orbitalLogistic.spacecraft.application.ports.in.CreateSpacecraftCommand;
import org.orbitalLogistic.spacecraft.application.ports.in.CreateSpacecraftUseCase;
import org.orbitalLogistic.spacecraft.application.ports.out.SpacecraftRepository;
import org.orbitalLogistic.spacecraft.application.ports.out.SpacecraftTypeRepository;
import org.orbitalLogistic.spacecraft.domain.model.Spacecraft;
import org.orbitalLogistic.spacecraft.exceptions.SpacecraftAlreadyExistsException;
import org.orbitalLogistic.spacecraft.exceptions.DataNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateSpacecraftService implements CreateSpacecraftUseCase {

    private final SpacecraftRepository spacecraftRepository;
    private final SpacecraftTypeRepository spacecraftTypeRepository;

    @Override
    public Spacecraft createSpacecraft(CreateSpacecraftCommand command) {
        log.debug("Creating spacecraft with registry code: {}", command.registryCode());

        if (spacecraftRepository.existsByRegistryCode(command.registryCode())) {
            throw new SpacecraftAlreadyExistsException(
                "Spacecraft with registry code already exists: " + command.registryCode()
            );
        }

        if (!spacecraftTypeRepository.existsById(command.spacecraftTypeId())) {
            throw new DataNotFoundException("Spacecraft type not found with id: " + command.spacecraftTypeId());
        }

        Spacecraft spacecraft = Spacecraft.builder()
                .registryCode(command.registryCode())
                .name(command.name())
                .spacecraftTypeId(command.spacecraftTypeId())
                .massCapacity(command.massCapacity())
                .volumeCapacity(command.volumeCapacity())
                .status(command.status())
                .currentLocation(command.currentLocation())
                .build();

        spacecraft.validate();

        Spacecraft saved = spacecraftRepository.save(spacecraft);
        log.info("Created spacecraft with id: {}", saved.getId());

        return saved;
    }
}


