package org.orbitalLogistic.spacecraft.application.usecases;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.orbitalLogistic.spacecraft.application.ports.in.CreateSpacecraftTypeCommand;
import org.orbitalLogistic.spacecraft.application.ports.in.CreateSpacecraftTypeUseCase;
import org.orbitalLogistic.spacecraft.application.ports.out.SpacecraftTypeRepository;
import org.orbitalLogistic.spacecraft.domain.model.SpacecraftType;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateSpacecraftTypeService implements CreateSpacecraftTypeUseCase {

    private final SpacecraftTypeRepository spacecraftTypeRepository;

    @Override
    public SpacecraftType createSpacecraftType(CreateSpacecraftTypeCommand command) {
        log.debug("Creating spacecraft type: {}", command.typeName());

        SpacecraftType spacecraftType = SpacecraftType.builder()
                .typeName(command.typeName())
                .classification(command.classification())
                .maxCrewCapacity(command.maxCrewCapacity())
                .build();

        spacecraftType.validate();

        SpacecraftType saved = spacecraftTypeRepository.save(spacecraftType);
        log.info("Created spacecraft type with id: {}", saved.getId());

        return saved;
    }
}

