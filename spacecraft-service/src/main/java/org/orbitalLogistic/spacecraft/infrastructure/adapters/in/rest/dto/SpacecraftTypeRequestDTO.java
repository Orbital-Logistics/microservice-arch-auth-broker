package org.orbitalLogistic.spacecraft.infrastructure.adapters.in.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.orbitalLogistic.spacecraft.domain.model.enums.SpacecraftClassification;

public record SpacecraftTypeRequestDTO(
        @NotBlank
        @Size(max = 50)
        String typeName,

        @NotNull
        SpacecraftClassification classification,

        @Positive
        Integer maxCrewCapacity
) {
}

