package org.orbitalLogistic.inventory.infrastructure.adapters.in.rest.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

public record CargoManifestRequestDTO(
        @NotNull(message = "Spacecraft ID is required")
        Long spacecraftId,

        @NotNull(message = "Cargo ID is required")
        Long cargoId,

        @NotNull(message = "Storage unit ID is required")
        Long storageUnitId,

        @NotNull
        @Min(value = 1, message = "Quantity must be positive")
        Integer quantity,

        LocalDateTime loadedAt,
        LocalDateTime unloadedAt,

        @NotNull(message = "Loaded by user ID is required")
        Long loadedByUserId,

        Long unloadedByUserId,

        ManifestStatus manifestStatus,
        ManifestPriority priority
) {
}
