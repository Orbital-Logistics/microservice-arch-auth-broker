package org.orbitalLogistic.spacecraft.infrastructure.adapters.in.rest.dto;

import org.orbitalLogistic.spacecraft.domain.model.enums.SpacecraftStatus;

import java.math.BigDecimal;

public record SpacecraftResponseDTO(
        Long id,
        String registryCode,
        String name,
        Long spacecraftTypeId,
        String spacecraftTypeName,
        BigDecimal massCapacity,
        BigDecimal volumeCapacity,
        SpacecraftStatus status,
        String currentLocation,
        BigDecimal currentMassUsage,
        BigDecimal currentVolumeUsage
) {
}

