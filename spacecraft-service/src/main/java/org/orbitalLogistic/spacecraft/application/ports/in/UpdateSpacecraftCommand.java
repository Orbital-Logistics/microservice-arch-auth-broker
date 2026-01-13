package org.orbitalLogistic.spacecraft.application.ports.in;

import org.orbitalLogistic.spacecraft.domain.model.enums.SpacecraftStatus;

import java.math.BigDecimal;

public record UpdateSpacecraftCommand(
        Long id,
        String registryCode,
        String name,
        Long spacecraftTypeId,
        BigDecimal massCapacity,
        BigDecimal volumeCapacity,
        SpacecraftStatus status,
        String currentLocation
) {
}

