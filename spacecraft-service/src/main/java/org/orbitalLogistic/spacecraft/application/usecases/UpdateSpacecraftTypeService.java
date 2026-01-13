package org.orbitalLogistic.spacecraft.application.usecases;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.orbitalLogistic.spacecraft.application.ports.in.UpdateSpacecraftTypeCommand;
import org.orbitalLogistic.spacecraft.application.ports.in.UpdateSpacecraftTypeUseCase;
import org.orbitalLogistic.spacecraft.application.ports.out.SpacecraftTypeRepository;
import org.orbitalLogistic.spacecraft.domain.model.SpacecraftType;
import org.orbitalLogistic.spacecraft.exceptions.SpacecraftTypeNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateSpacecraftTypeService implements UpdateSpacecraftTypeUseCase {

    private final SpacecraftTypeRepository spacecraftTypeRepository;

    @Override
    public SpacecraftType updateSpacecraftType(UpdateSpacecraftTypeCommand command) {
        log.debug("Updating spacecraft type with id: {}", command.id());

        if (!spacecraftTypeRepository.existsById(command.id())) {
            throw new SpacecraftTypeNotFoundException("Spacecraft type not found with id: " + command.id());
        }

        SpacecraftType spacecraftType = SpacecraftType.builder()
                .id(command.id())
                .typeName(command.typeName())
                .classification(command.classification())
                .maxCrewCapacity(command.maxCrewCapacity())
                .build();

        spacecraftType.validate();

        SpacecraftType saved = spacecraftTypeRepository.save(spacecraftType);
        log.info("Updated spacecraft type with id: {}", saved.getId());

        return saved;
    }
}

