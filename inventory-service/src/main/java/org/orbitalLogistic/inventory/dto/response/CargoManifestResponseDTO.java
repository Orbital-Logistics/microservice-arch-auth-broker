package org.orbitalLogistic.inventory.dto.response;

import org.orbitalLogistic.inventory.entities.enums.ManifestPriority;
import org.orbitalLogistic.inventory.entities.enums.ManifestStatus;

import java.time.LocalDateTime;

public record CargoManifestResponseDTO(
    Long id,
    Long spacecraftId,
    String spacecraftName,
    Long cargoId,
    String cargoName,
    Long storageUnitId,
    String storageUnitCode,
    Integer quantity,
    LocalDateTime loadedAt,
    LocalDateTime unloadedAt,
    Long loadedByUserId,
    String loadedByUserName,
    Long unloadedByUserId,
    String unloadedByUserName,
    ManifestStatus manifestStatus,
    ManifestPriority priority
) {}

