package org.orbitalLogistic.inventory.infrastructure.adapters.out.persistence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.orbitalLogistic.inventory.domain.model.CargoManifest;
import org.orbitalLogistic.inventory.domain.model.enums.ManifestPriority;
import org.orbitalLogistic.inventory.domain.model.enums.ManifestStatus;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CargoManifestPersistenceMapperTest {

    private CargoManifestPersistenceMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new CargoManifestPersistenceMapper();
    }

    @Test
    @DisplayName("Should map domain manifest to entity")
    void toEntity_Success() {
        LocalDateTime loadedAt = LocalDateTime.now();
        CargoManifest manifest = CargoManifest.builder()
                .id(1L)
                .spacecraftId(10L)
                .cargoId(20L)
                .storageUnitId(30L)
                .quantity(100)
                .loadedAt(loadedAt)
                .loadedByUserId(5L)
                .manifestStatus(ManifestStatus.PENDING)
                .priority(ManifestPriority.HIGH)
                .build();
        CargoManifestEntity entity = mapper.toEntity(manifest);
        assertNotNull(entity);
        assertEquals(1L, entity.getId());
        assertEquals(10L, entity.getSpacecraftId());
        assertEquals(20L, entity.getCargoId());
        assertEquals(30L, entity.getStorageUnitId());
        assertEquals(100, entity.getQuantity());
        assertEquals(loadedAt, entity.getLoadedAt());
        assertNull(entity.getUnloadedAt());
        assertEquals(5L, entity.getLoadedByUserId());
        assertNull(entity.getUnloadedByUserId());
        assertEquals("PENDING", entity.getManifestStatus());
        assertEquals("HIGH", entity.getPriority());
    }

    @Test
    @DisplayName("Should map entity to domain manifest")
    void toDomain_Success() {
        LocalDateTime loadedAt = LocalDateTime.now();
        CargoManifestEntity entity = CargoManifestEntity.builder()
                .id(1L)
                .spacecraftId(10L)
                .cargoId(20L)
                .storageUnitId(30L)
                .quantity(100)
                .loadedAt(loadedAt)
                .loadedByUserId(5L)
                .manifestStatus("PENDING")
                .priority("HIGH")
                .build();
        CargoManifest manifest = mapper.toDomain(entity);
        assertNotNull(manifest);
        assertEquals(1L, manifest.getId());
        assertEquals(10L, manifest.getSpacecraftId());
        assertEquals(20L, manifest.getCargoId());
        assertEquals(30L, manifest.getStorageUnitId());
        assertEquals(100, manifest.getQuantity());
        assertEquals(loadedAt, manifest.getLoadedAt());
        assertNull(manifest.getUnloadedAt());
        assertEquals(5L, manifest.getLoadedByUserId());
        assertNull(manifest.getUnloadedByUserId());
        assertEquals(ManifestStatus.PENDING, manifest.getManifestStatus());
        assertEquals(ManifestPriority.HIGH, manifest.getPriority());
    }

    @Test
    @DisplayName("Should map unloaded manifest to entity")
    void toEntity_UnloadedManifest() {
        LocalDateTime loadedAt = LocalDateTime.now().minusDays(1);
        LocalDateTime unloadedAt = LocalDateTime.now();
        CargoManifest manifest = CargoManifest.builder()
                .id(2L)
                .spacecraftId(10L)
                .cargoId(20L)
                .storageUnitId(30L)
                .quantity(50)
                .loadedAt(loadedAt)
                .unloadedAt(unloadedAt)
                .loadedByUserId(5L)
                .unloadedByUserId(6L)
                .manifestStatus(ManifestStatus.UNLOADED)
                .priority(ManifestPriority.NORMAL)
                .build();
        CargoManifestEntity entity = mapper.toEntity(manifest);
        assertNotNull(entity);
        assertEquals(2L, entity.getId());
        assertEquals(loadedAt, entity.getLoadedAt());
        assertEquals(unloadedAt, entity.getUnloadedAt());
        assertEquals(5L, entity.getLoadedByUserId());
        assertEquals(6L, entity.getUnloadedByUserId());
        assertEquals("UNLOADED", entity.getManifestStatus());
        assertEquals("NORMAL", entity.getPriority());
    }

    @Test
    @DisplayName("Should map all manifest statuses correctly")
    void toEntity_AllStatuses() {
        for (ManifestStatus status : ManifestStatus.values()) {
            CargoManifest manifest = CargoManifest.builder()
                    .id(1L)
                    .spacecraftId(10L)
                    .cargoId(20L)
                    .storageUnitId(30L)
                    .quantity(100)
                    .loadedByUserId(5L)
                    .manifestStatus(status)
                    .priority(ManifestPriority.NORMAL)
                    .build();
            CargoManifestEntity entity = mapper.toEntity(manifest);
            assertEquals(status.name(), entity.getManifestStatus());
        }
    }

    @Test
    @DisplayName("Should map all manifest priorities correctly")
    void toEntity_AllPriorities() {
        for (ManifestPriority priority : ManifestPriority.values()) {
            CargoManifest manifest = CargoManifest.builder()
                    .id(1L)
                    .spacecraftId(10L)
                    .cargoId(20L)
                    .storageUnitId(30L)
                    .quantity(100)
                    .loadedByUserId(5L)
                    .manifestStatus(ManifestStatus.PENDING)
                    .priority(priority)
                    .build();
            CargoManifestEntity entity = mapper.toEntity(manifest);
            assertEquals(priority.name(), entity.getPriority());
        }
    }

    @Test
    @DisplayName("Should handle null status in toEntity")
    void toEntity_NullStatus() {
        CargoManifest manifest = CargoManifest.builder()
                .id(1L)
                .spacecraftId(10L)
                .cargoId(20L)
                .storageUnitId(30L)
                .quantity(100)
                .loadedByUserId(5L)
                .manifestStatus(null)
                .priority(ManifestPriority.NORMAL)
                .build();
        CargoManifestEntity entity = mapper.toEntity(manifest);
        assertNotNull(entity);
        assertNull(entity.getManifestStatus());
    }

    @Test
    @DisplayName("Should handle null priority in toEntity")
    void toEntity_NullPriority() {
        CargoManifest manifest = CargoManifest.builder()
                .id(1L)
                .spacecraftId(10L)
                .cargoId(20L)
                .storageUnitId(30L)
                .quantity(100)
                .loadedByUserId(5L)
                .manifestStatus(ManifestStatus.PENDING)
                .priority(null)
                .build();
        CargoManifestEntity entity = mapper.toEntity(manifest);
        assertNotNull(entity);
        assertNull(entity.getPriority());
    }

    @Test
    @DisplayName("Should handle null status in toDomain")
    void toDomain_NullStatus() {
        CargoManifestEntity entity = CargoManifestEntity.builder()
                .id(1L)
                .spacecraftId(10L)
                .cargoId(20L)
                .storageUnitId(30L)
                .quantity(100)
                .loadedByUserId(5L)
                .manifestStatus(null)
                .priority("NORMAL")
                .build();
        CargoManifest manifest = mapper.toDomain(entity);
        assertNotNull(manifest);
        assertNull(manifest.getManifestStatus());
    }

    @Test
    @DisplayName("Should handle null priority in toDomain")
    void toDomain_NullPriority() {
        CargoManifestEntity entity = CargoManifestEntity.builder()
                .id(1L)
                .spacecraftId(10L)
                .cargoId(20L)
                .storageUnitId(30L)
                .quantity(100)
                .loadedByUserId(5L)
                .manifestStatus("PENDING")
                .priority(null)
                .build();
        CargoManifest manifest = mapper.toDomain(entity);
        assertNotNull(manifest);
        assertNull(manifest.getPriority());
    }

    @Test
    @DisplayName("Should round-trip conversion preserve all data")
    void roundTrip_PreservesData() {
        LocalDateTime loadedAt = LocalDateTime.now().minusDays(1);
        LocalDateTime unloadedAt = LocalDateTime.now();
        CargoManifest original = CargoManifest.builder()
                .id(1L)
                .spacecraftId(10L)
                .cargoId(20L)
                .storageUnitId(30L)
                .quantity(100)
                .loadedAt(loadedAt)
                .unloadedAt(unloadedAt)
                .loadedByUserId(5L)
                .unloadedByUserId(6L)
                .manifestStatus(ManifestStatus.UNLOADED)
                .priority(ManifestPriority.CRITICAL)
                .build();
        CargoManifestEntity entity = mapper.toEntity(original);
        CargoManifest result = mapper.toDomain(entity);
        assertEquals(original.getId(), result.getId());
        assertEquals(original.getSpacecraftId(), result.getSpacecraftId());
        assertEquals(original.getCargoId(), result.getCargoId());
        assertEquals(original.getStorageUnitId(), result.getStorageUnitId());
        assertEquals(original.getQuantity(), result.getQuantity());
        assertEquals(original.getLoadedAt(), result.getLoadedAt());
        assertEquals(original.getUnloadedAt(), result.getUnloadedAt());
        assertEquals(original.getLoadedByUserId(), result.getLoadedByUserId());
        assertEquals(original.getUnloadedByUserId(), result.getUnloadedByUserId());
        assertEquals(original.getManifestStatus(), result.getManifestStatus());
        assertEquals(original.getPriority(), result.getPriority());
    }

    @Test
    @DisplayName("Should map manifest with only required fields")
    void toEntity_OnlyRequiredFields() {
        CargoManifest manifest = CargoManifest.builder()
                .spacecraftId(10L)
                .cargoId(20L)
                .storageUnitId(30L)
                .quantity(100)
                .loadedByUserId(5L)
                .manifestStatus(ManifestStatus.PENDING)
                .priority(ManifestPriority.NORMAL)
                .build();
        CargoManifestEntity entity = mapper.toEntity(manifest);
        assertNotNull(entity);
        assertNull(entity.getId());
        assertEquals(10L, entity.getSpacecraftId());
        assertEquals(20L, entity.getCargoId());
        assertEquals(30L, entity.getStorageUnitId());
        assertEquals(100, entity.getQuantity());
    }

    @Test
    @DisplayName("Should preserve timestamps during mapping")
    void toDomain_PreservesTimestamps() {
        LocalDateTime loadedAt = LocalDateTime.of(2025, 1, 1, 10, 0);
        LocalDateTime unloadedAt = LocalDateTime.of(2025, 1, 2, 15, 30);
        
        CargoManifestEntity entity = CargoManifestEntity.builder()
                .id(1L)
                .spacecraftId(10L)
                .cargoId(20L)
                .storageUnitId(30L)
                .quantity(100)
                .loadedAt(loadedAt)
                .unloadedAt(unloadedAt)
                .loadedByUserId(5L)
                .unloadedByUserId(6L)
                .manifestStatus("UNLOADED")
                .priority("NORMAL")
                .build();
        CargoManifest manifest = mapper.toDomain(entity);
        assertEquals(loadedAt, manifest.getLoadedAt());
        assertEquals(unloadedAt, manifest.getUnloadedAt());
    }
}
