package org.orbitalLogistic.file.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.orbitalLogistic.file.domain.model.enums.ManifestPriority;
import org.orbitalLogistic.file.domain.model.enums.ManifestStatus;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CargoManifestTest {

    @Test
    @DisplayName("Should validate successfully with valid data")
    void validate_Success() {
        CargoManifest manifest = CargoManifest.builder()
                .spacecraftId(1L)
                .cargoId(1L)
                .storageUnitId(1L)
                .quantity(10)
                .loadedByUserId(1L)
                .manifestStatus(ManifestStatus.PENDING)
                .priority(ManifestPriority.NORMAL)
                .build();
        assertDoesNotThrow(manifest::validate);
    }

    @Test
    @DisplayName("Should throw exception when spacecraft ID is null")
    void validate_NullSpacecraftId() {
        CargoManifest manifest = CargoManifest.builder()
                .spacecraftId(null)
                .cargoId(1L)
                .storageUnitId(1L)
                .quantity(10)
                .loadedByUserId(1L)
                .manifestStatus(ManifestStatus.PENDING)
                .priority(ManifestPriority.NORMAL)
                .build();
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                manifest::validate
        );
        assertEquals("Spacecraft ID is required", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when quantity is not positive")
    void validate_NonPositiveQuantity() {
        CargoManifest manifest = CargoManifest.builder()
                .spacecraftId(1L)
                .cargoId(1L)
                .storageUnitId(1L)
                .quantity(0)
                .loadedByUserId(1L)
                .manifestStatus(ManifestStatus.PENDING)
                .priority(ManifestPriority.NORMAL)
                .build();
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                manifest::validate
        );
        assertEquals("Quantity must be positive", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when UNLOADED status but no unloadedByUserId")
    void validate_UnloadedStatusWithoutUser() {
        CargoManifest manifest = CargoManifest.builder()
                .spacecraftId(1L)
                .cargoId(1L)
                .storageUnitId(1L)
                .quantity(10)
                .loadedByUserId(1L)
                .loadedAt(LocalDateTime.now())
                .manifestStatus(ManifestStatus.UNLOADED)
                .priority(ManifestPriority.NORMAL)
                .build();
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                manifest::validate
        );
        assertEquals("Unloaded by user must be provided when manifest status is UNLOADED", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when unloadedByUserId provided but status is not UNLOADED")
    void validate_UnloadedUserWithoutUnloadedStatus() {
        CargoManifest manifest = CargoManifest.builder()
                .spacecraftId(1L)
                .cargoId(1L)
                .storageUnitId(1L)
                .quantity(10)
                .loadedByUserId(1L)
                .unloadedByUserId(2L)
                .manifestStatus(ManifestStatus.PENDING)
                .priority(ManifestPriority.NORMAL)
                .build();
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                manifest::validate
        );
        assertEquals("Manifest status must be UNLOADED when unloaded by user is provided", exception.getMessage());
    }

    @Test
    @DisplayName("Should return true for isPending when status is PENDING")
    void isPending_True() {
        CargoManifest manifest = CargoManifest.builder()
                .manifestStatus(ManifestStatus.PENDING)
                .build();
        assertTrue(manifest.isPending());
    }

    @Test
    @DisplayName("Should return true for isCritical when priority is CRITICAL")
    void isCritical_True() {
        CargoManifest manifest = CargoManifest.builder()
                .priority(ManifestPriority.CRITICAL)
                .build();
        assertTrue(manifest.isCritical());
    }
}
