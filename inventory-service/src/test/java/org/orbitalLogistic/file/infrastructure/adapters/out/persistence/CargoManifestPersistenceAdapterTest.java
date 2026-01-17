package org.orbitalLogistic.file.infrastructure.adapters.out.persistence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.orbitalLogistic.file.domain.model.CargoManifest;
import org.orbitalLogistic.file.domain.model.enums.ManifestPriority;
import org.orbitalLogistic.file.domain.model.enums.ManifestStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CargoManifestPersistenceAdapterTest {

    @Mock
    private CargoManifestJdbcRepository cargoManifestJdbcRepository;

    @Mock
    private CargoManifestPersistenceMapper cargoManifestPersistenceMapper;

    @InjectMocks
    private CargoManifestPersistenceAdapter cargoManifestPersistenceAdapter;

    private CargoManifest domainManifest;
    private CargoManifestEntity entity;

    @BeforeEach
    void setUp() {
        domainManifest = CargoManifest.builder()
                .id(1L)
                .spacecraftId(1L)
                .cargoId(1L)
                .storageUnitId(1L)
                .quantity(10)
                .loadedAt(LocalDateTime.now())
                .loadedByUserId(1L)
                .manifestStatus(ManifestStatus.PENDING)
                .priority(ManifestPriority.NORMAL)
                .build();

        entity = CargoManifestEntity.builder()
                .id(1L)
                .spacecraftId(1L)
                .cargoId(1L)
                .storageUnitId(1L)
                .quantity(10)
                .loadedAt(LocalDateTime.now())
                .loadedByUserId(1L)
                .manifestStatus("PENDING")
                .priority("NORMAL")
                .build();
    }

    @Test
    @DisplayName("Should save manifest successfully")
    void save_Success() {
        when(cargoManifestPersistenceMapper.toEntity(domainManifest)).thenReturn(entity);
        when(cargoManifestJdbcRepository.save(entity)).thenReturn(entity);
        when(cargoManifestPersistenceMapper.toDomain(entity)).thenReturn(domainManifest);
        CargoManifest result = cargoManifestPersistenceAdapter.save(domainManifest);
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(cargoManifestJdbcRepository).save(entity);
    }

    @Test
    @DisplayName("Should find manifest by id successfully")
    void findById_Success() {
        when(cargoManifestJdbcRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(cargoManifestPersistenceMapper.toDomain(entity)).thenReturn(domainManifest);
        Optional<CargoManifest> result = cargoManifestPersistenceAdapter.findById(1L);
        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
    }

    @Test
    @DisplayName("Should return empty when manifest not found")
    void findById_NotFound() {
        when(cargoManifestJdbcRepository.findById(999L)).thenReturn(Optional.empty());
        Optional<CargoManifest> result = cargoManifestPersistenceAdapter.findById(999L);
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Should find all manifests")
    void findAll_Success() {
        when(cargoManifestJdbcRepository.findAllPaginated(20, 0)).thenReturn(List.of(entity));
        when(cargoManifestPersistenceMapper.toDomain(entity)).thenReturn(domainManifest);
        List<CargoManifest> result = cargoManifestPersistenceAdapter.findAll(20, 0);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.getFirst().getId());
    }

    @Test
    @DisplayName("Should find manifests by spacecraft id")
    void findBySpacecraftId_Success() {
        when(cargoManifestJdbcRepository.findBySpacecraftIdPaginated(1L, 20, 0)).thenReturn(List.of(entity));
        when(cargoManifestPersistenceMapper.toDomain(entity)).thenReturn(domainManifest);
        List<CargoManifest> result = cargoManifestPersistenceAdapter.findBySpacecraftId(1L, 20, 0);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.getFirst().getSpacecraftId());
    }

    @Test
    @DisplayName("Should count all manifests")
    void countAll_Success() {
        when(cargoManifestJdbcRepository.countAll()).thenReturn(5L);
        long result = cargoManifestPersistenceAdapter.countAll();
        assertEquals(5L, result);
    }

    @Test
    @DisplayName("Should count manifests by spacecraft id")
    void countBySpacecraftId_Success() {
        when(cargoManifestJdbcRepository.countBySpacecraftId(1L)).thenReturn(3L);
        long result = cargoManifestPersistenceAdapter.countBySpacecraftId(1L);
        assertEquals(3L, result);
    }
}
