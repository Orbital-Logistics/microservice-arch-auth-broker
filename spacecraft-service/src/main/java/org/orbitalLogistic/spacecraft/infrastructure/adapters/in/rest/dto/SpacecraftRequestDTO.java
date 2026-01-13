package org.orbitalLogistic.spacecraft.infrastructure.adapters.in.rest.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.orbitalLogistic.spacecraft.domain.model.enums.SpacecraftStatus;

import java.math.BigDecimal;

public record SpacecraftRequestDTO(
        @NotBlank
        @Size(max = 20)
        String registryCode,

        @NotBlank
        @Size(max = 100)
        String name,

        @NotNull
        Long spacecraftTypeId,

        @NotNull
        @DecimalMin("0.01")
        BigDecimal massCapacity,

        @NotNull
        @DecimalMin("0.01")
        BigDecimal volumeCapacity,

        @NotNull
        SpacecraftStatus status,

        @Size(max = 100)
        String currentLocation
) {
}

