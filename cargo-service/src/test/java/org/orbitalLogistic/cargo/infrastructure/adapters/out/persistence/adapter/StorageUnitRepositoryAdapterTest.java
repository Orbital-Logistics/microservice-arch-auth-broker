package org.orbitalLogistic.cargo.infrastructure.adapters.out.persistence.adapter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.orbitalLogistic.cargo.domain.model.StorageUnit;
import org.orbitalLogistic.cargo.domain.model.enums.StorageTypeEnum;
import org.orbitalLogistic.cargo.infrastructure.adapters.out.persistence.StorageUnitRepositoryAdapter;
import org.orbitalLogistic.cargo.infrastructure.adapters.out.persistence.entity.StorageUnitEntity;
import org.orbitalLogistic.cargo.infrastructure.adapters.out.persistence.mapper.StorageUnitPersistenceMapper;
import org.orbitalLogistic.cargo.infrastructure.adapters.out.persistence.repository.StorageUnitJdbcRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StorageUnitRepositoryAdapterTest {

    @Mock
    private StorageUnitJdbcRepository jdbcRepository;

    @Mock
    private StorageUnitPersistenceMapper mapper;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private StorageUnitRepositoryAdapter adapter;

    private StorageUnit storageUnit;
    private StorageUnitEntity storageUnitEntity;

    @BeforeEach
    void setUp() {
        storageUnit = StorageUnit.builder()
                .id(1L)
                .unitCode("UNIT-001")
                .location("Warehouse A")
                .storageType(StorageTypeEnum.AMBIENT)
                .maxMass(BigDecimal.valueOf(1000))
                .maxVolume(BigDecimal.valueOf(50))
                .currentMass(BigDecimal.valueOf(100))
                .currentVolume(BigDecimal.valueOf(10))
                .isActive(true)
                .build();

        storageUnitEntity = new StorageUnitEntity();
        storageUnitEntity.setId(1L);
        storageUnitEntity.setUnitCode("UNIT-001");
        storageUnitEntity.setLocation("Warehouse A");
        storageUnitEntity.setStorageType("AMBIENT");
        storageUnitEntity.setTotalMassCapacity(BigDecimal.valueOf(1000));
        storageUnitEntity.setTotalVolumeCapacity(BigDecimal.valueOf(50));
        storageUnitEntity.setCurrentMass(BigDecimal.valueOf(100));
        storageUnitEntity.setCurrentVolume(BigDecimal.valueOf(10));
    }

    @Test
    void save_Success() {
        // Given
        when(mapper.toEntity(storageUnit)).thenReturn(storageUnitEntity);
        when(jdbcRepository.save(storageUnitEntity)).thenReturn(storageUnitEntity);
        when(mapper.toDomain(storageUnitEntity)).thenReturn(storageUnit);

        // When
        StorageUnit result = adapter.save(storageUnit);

        // Then
        assertNotNull(result);
        assertEquals("UNIT-001", result.getUnitCode());
        verify(jdbcRepository).save(storageUnitEntity);
    }

    @Test
    void findById_Success() {
        // Given
        when(jdbcRepository.findById(1L)).thenReturn(Optional.of(storageUnitEntity));
        when(mapper.toDomain(storageUnitEntity)).thenReturn(storageUnit);

        // When
        Optional<StorageUnit> result = adapter.findById(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals("UNIT-001", result.get().getUnitCode());
        verify(jdbcRepository).findById(1L);
    }

    @Test
    void findById_NotFound() {
        // Given
        when(jdbcRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<StorageUnit> result = adapter.findById(999L);

        // Then
        assertFalse(result.isPresent());
        verify(jdbcRepository).findById(999L);
        verify(mapper, never()).toDomain(any());
    }

    @Test
    void findAll_Success() {
        // Given
        when(jdbcRepository.findAll()).thenReturn(Arrays.asList(storageUnitEntity));
        when(mapper.toDomain(storageUnitEntity)).thenReturn(storageUnit);

        // When
        List<StorageUnit> result = adapter.findAll();

        // Then
        assertEquals(1, result.size());
        assertEquals("UNIT-001", result.get(0).getUnitCode());
        verify(jdbcRepository).findAll();
    }

    @Test
    void findByUnitCode_Success() {
        // Given
        when(jdbcRepository.findByUnitCode("UNIT-001")).thenReturn(Optional.of(storageUnitEntity));
        when(mapper.toDomain(storageUnitEntity)).thenReturn(storageUnit);

        // When
        Optional<StorageUnit> result = adapter.findByUnitCode("UNIT-001");

        // Then
        assertTrue(result.isPresent());
        assertEquals("UNIT-001", result.get().getUnitCode());
        verify(jdbcRepository).findByUnitCode("UNIT-001");
    }

    @Test
    void findByUnitCode_NotFound() {
        // Given
        when(jdbcRepository.findByUnitCode("INVALID")).thenReturn(Optional.empty());

        // When
        Optional<StorageUnit> result = adapter.findByUnitCode("INVALID");

        // Then
        assertFalse(result.isPresent());
        verify(jdbcRepository).findByUnitCode("INVALID");
    }

    @Test
    void existsByUnitCode_ReturnsTrue() {
        // Given
        when(jdbcRepository.existsByUnitCode("UNIT-001")).thenReturn(true);

        // When
        boolean result = adapter.existsByUnitCode("UNIT-001");

        // Then
        assertTrue(result);
        verify(jdbcRepository).existsByUnitCode("UNIT-001");
    }

    @Test
    void existsByUnitCode_ReturnsFalse() {
        // Given
        when(jdbcRepository.existsByUnitCode("INVALID")).thenReturn(false);

        // When
        boolean result = adapter.existsByUnitCode("INVALID");

        // Then
        assertFalse(result);
        verify(jdbcRepository).existsByUnitCode("INVALID");
    }

    @Test
    void findByLocation_Success() {
        // Given
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq("Warehouse A"), eq(10), eq(0)))
                .thenReturn(Arrays.asList(storageUnit));

        // When
        List<StorageUnit> result = adapter.findByLocation("Warehouse A", 10, 0);

        // Then
        assertEquals(1, result.size());
        assertEquals("Warehouse A", result.get(0).getLocation());
        verify(jdbcTemplate).query(anyString(), any(RowMapper.class), eq("Warehouse A"), eq(10), eq(0));
    }

    @Test
    void countByLocation_ReturnsCount() {
        // Given
        when(jdbcRepository.countByLocation("Warehouse A")).thenReturn(Integer.valueOf(5));

        // When
        long result = adapter.countByLocation("Warehouse A");

        // Then
        assertEquals(5L, result);
        verify(jdbcRepository).countByLocation("Warehouse A");
    }

    @Test
    void existsById_ReturnsTrue() {
        // Given
        when(jdbcRepository.existsById(1L)).thenReturn(true);

        // When
        boolean result = adapter.existsById(1L);

        // Then
        assertTrue(result);
        verify(jdbcRepository).existsById(1L);
    }

    @Test
    void existsById_ReturnsFalse() {
        // Given
        when(jdbcRepository.existsById(999L)).thenReturn(false);

        // When
        boolean result = adapter.existsById(999L);

        // Then
        assertFalse(result);
        verify(jdbcRepository).existsById(999L);
    }

    @Test
    void deleteById_Success() {
        // Given
        doNothing().when(jdbcRepository).deleteById(1L);

        // When
        adapter.deleteById(1L);

        // Then
        verify(jdbcRepository).deleteById(1L);
    }
}
