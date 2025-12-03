package org.orbitalLogistic.inventory.dto.request;

import jakarta.validation.constraints.*;
import org.orbitalLogistic.inventory.entities.enums.ManifestPriority;
import org.orbitalLogistic.inventory.entities.enums.ManifestStatus;

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
) {}

