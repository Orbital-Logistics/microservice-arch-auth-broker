package org.orbitalLogistic.inventory.domain.model;

import lombok.Builder;
import lombok.Value;
import org.orbitalLogistic.inventory.domain.model.enums.ManifestPriority;
import org.orbitalLogistic.inventory.domain.model.enums.ManifestStatus;

import java.time.LocalDateTime;

@Value
@Builder(toBuilder = true)
public class CargoManifest {
    Long id;
    Long spacecraftId;
    Long cargoId;
    Long storageUnitId;
    Integer quantity;
    LocalDateTime loadedAt;
    LocalDateTime unloadedAt;
    Long loadedByUserId;
    Long unloadedByUserId;
    ManifestStatus manifestStatus;
    ManifestPriority priority;

    public void validate() {
        if (spacecraftId == null) {
            throw new IllegalArgumentException("Spacecraft ID is required");
        }
        if (cargoId == null) {
            throw new IllegalArgumentException("Cargo ID is required");
        }
        if (storageUnitId == null) {
            throw new IllegalArgumentException("Storage unit ID is required");
        }
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (loadedByUserId == null) {
            throw new IllegalArgumentException("Loaded by user ID is required");
        }
        if (manifestStatus == null) {
            throw new IllegalArgumentException("Manifest status is required");
        }
        if (priority == null) {
            throw new IllegalArgumentException("Priority is required");
        }

        validateStatusTransition();
    }

    private void validateStatusTransition() {
        if (manifestStatus == ManifestStatus.UNLOADED) {
            if (unloadedByUserId == null) {
                throw new IllegalArgumentException("Unloaded by user must be provided when manifest status is UNLOADED");
            }
            if (unloadedAt == null) {
                throw new IllegalArgumentException("Unloaded at must be provided when manifest status is UNLOADED");
            }
        }

        // Reverse check: if unloaded by user is set, status must be UNLOADED
        if (unloadedByUserId != null && manifestStatus != ManifestStatus.UNLOADED) {
            throw new IllegalArgumentException("Manifest status must be UNLOADED when unloaded by user is provided");
        }

        if (manifestStatus == ManifestStatus.IN_TRANSIT || manifestStatus == ManifestStatus.LOADED) {
            if (loadedAt == null) {
                throw new IllegalArgumentException("Loaded at must be provided for " + manifestStatus + " status");
            }
        }

        if (unloadedAt != null && loadedAt != null && unloadedAt.isBefore(loadedAt)) {
            throw new IllegalArgumentException("Unloaded time cannot be before loaded time");
        }
    }

    public boolean isPending() {
        return manifestStatus == ManifestStatus.PENDING;
    }

    public boolean isLoaded() {
        return manifestStatus == ManifestStatus.LOADED;
    }

    public boolean isInTransit() {
        return manifestStatus == ManifestStatus.IN_TRANSIT;
    }

    public boolean isUnloaded() {
        return manifestStatus == ManifestStatus.UNLOADED;
    }

    public boolean isCritical() {
        return priority == ManifestPriority.CRITICAL;
    }

    public boolean isHighPriority() {
        return priority == ManifestPriority.HIGH || priority == ManifestPriority.CRITICAL;
    }
}
