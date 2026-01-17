package org.orbitalLogistic.file.application.ports.in;

import org.orbitalLogistic.file.domain.model.enums.ManifestPriority;
import org.orbitalLogistic.file.domain.model.enums.ManifestStatus;

import java.time.LocalDateTime;

public record UpdateCargoManifestCommand(
        Long id,
        Long spacecraftId,
        Long cargoId,
        Long storageUnitId,
        Integer quantity,
        LocalDateTime loadedAt,
        LocalDateTime unloadedAt,
        Long loadedByUserId,
        Long unloadedByUserId,
        ManifestStatus manifestStatus,
        ManifestPriority priority
) {
}
