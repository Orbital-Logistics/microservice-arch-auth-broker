package org.orbitalLogistic.spacecraft.dto.response;

import org.orbitalLogistic.spacecraft.entities.enums.SpacecraftStatus;
import org.orbitalLogistic.spacecraft.entities.enums.SpacecraftClassification;

import java.math.BigDecimal;

public record SpacecraftResponseDTO(
    Long id,
    String registryCode,
    String name,
    String spacecraftTypeName,
    SpacecraftClassification classification,
    BigDecimal massCapacity,
    BigDecimal volumeCapacity,
    SpacecraftStatus status,
    String currentLocation,
    BigDecimal currentMassUsage,
    BigDecimal currentVolumeUsage
) {}

