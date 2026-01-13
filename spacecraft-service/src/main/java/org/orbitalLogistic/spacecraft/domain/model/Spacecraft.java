package org.orbitalLogistic.spacecraft.domain.model;

import lombok.Builder;
import lombok.Value;
import org.orbitalLogistic.spacecraft.domain.model.enums.SpacecraftStatus;

import java.math.BigDecimal;

@Value
@Builder(toBuilder = true)
public class Spacecraft {
    Long id;
    String registryCode;
    String name;
    Long spacecraftTypeId;
    BigDecimal massCapacity;
    BigDecimal volumeCapacity;
    SpacecraftStatus status;
    String currentLocation;

    public void validate() {
        if (registryCode == null || registryCode.isBlank()) {
            throw new IllegalArgumentException("Registry code is required");
        }
        if (registryCode.length() > 20) {
            throw new IllegalArgumentException("Registry code must not exceed 20 characters");
        }
        if (!registryCode.matches("^[A-Z0-9-]+$")) {
            throw new IllegalArgumentException("Registry code can only contain uppercase letters, numbers and hyphens");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name is required");
        }
        if (name.length() > 100) {
            throw new IllegalArgumentException("Name must not exceed 100 characters");
        }
        if (spacecraftTypeId == null) {
            throw new IllegalArgumentException("Spacecraft type is required");
        }
        if (massCapacity == null || massCapacity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Mass capacity must be positive");
        }
        if (volumeCapacity == null || volumeCapacity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Volume capacity must be positive");
        }
        if (status == null) {
            throw new IllegalArgumentException("Status is required");
        }
        if (currentLocation != null && currentLocation.length() > 100) {
            throw new IllegalArgumentException("Location must not exceed 100 characters");
        }
    }

    public boolean isAvailableForMission() {
        return status == SpacecraftStatus.DOCKED || status == SpacecraftStatus.MAINTENANCE;
    }

    public boolean isInTransit() {
        return status == SpacecraftStatus.IN_TRANSIT;
    }

    public boolean isDocked() {
        return status == SpacecraftStatus.DOCKED;
    }

    public boolean isInMaintenance() {
        return status == SpacecraftStatus.MAINTENANCE;
    }
}

