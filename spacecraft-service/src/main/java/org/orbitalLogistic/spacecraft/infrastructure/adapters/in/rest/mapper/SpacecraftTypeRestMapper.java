package org.orbitalLogistic.spacecraft.infrastructure.adapters.in.rest.mapper;

import org.orbitalLogistic.spacecraft.application.ports.in.CreateSpacecraftTypeCommand;
import org.orbitalLogistic.spacecraft.application.ports.in.UpdateSpacecraftTypeCommand;
import org.orbitalLogistic.spacecraft.domain.model.SpacecraftType;
import org.orbitalLogistic.spacecraft.infrastructure.adapters.in.rest.dto.SpacecraftTypeRequestDTO;
import org.orbitalLogistic.spacecraft.infrastructure.adapters.in.rest.dto.SpacecraftTypeResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class SpacecraftTypeRestMapper {

    public CreateSpacecraftTypeCommand toCreateCommand(SpacecraftTypeRequestDTO dto) {
        return new CreateSpacecraftTypeCommand(
                dto.typeName(),
                dto.classification(),
                dto.maxCrewCapacity()
        );
    }

    public UpdateSpacecraftTypeCommand toUpdateCommand(Long id, SpacecraftTypeRequestDTO dto) {
        return new UpdateSpacecraftTypeCommand(
                id,
                dto.typeName(),
                dto.classification(),
                dto.maxCrewCapacity()
        );
    }

    public SpacecraftTypeResponseDTO toResponseDTO(SpacecraftType spacecraftType) {
        return new SpacecraftTypeResponseDTO(
                spacecraftType.getId(),
                spacecraftType.getTypeName(),
                spacecraftType.getClassification(),
                spacecraftType.getMaxCrewCapacity()
        );
    }
}

