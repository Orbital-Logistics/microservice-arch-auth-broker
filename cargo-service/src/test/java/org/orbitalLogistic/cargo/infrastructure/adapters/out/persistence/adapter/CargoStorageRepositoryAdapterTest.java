package org.orbitalLogistic.cargo.infrastructure.adapters.out.persistence.adapter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.orbitalLogistic.cargo.domain.model.CargoStorage;
import org.orbitalLogistic.cargo.infrastructure.adapters.out.persistence.CargoStorageRepositoryAdapter;
import org.orbitalLogistic.cargo.infrastructure.adapters.out.persistence.entity.CargoStorageEntity;
import org.orbitalLogistic.cargo.infrastructure.adapters.out.persistence.mapper.CargoStoragePersistenceMapper;
import org.orbitalLogistic.cargo.infrastructure.adapters.out.persistence.repository.CargoStorageJdbcRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CargoStorageRepositoryAdapterTest {

    @Mock
    private CargoStorageJdbcRepository jdbcRepository;

    @Mock
    private CargoStoragePersistenceMapper mapper;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private CargoStorageRepositoryAdapter adapter;

    private CargoStorage cargoStorage;
    private CargoStorageEntity cargoStorageEntity;

    @BeforeEach
    void setUp() {
        cargoStorage = CargoStorage.builder()
                .id(1L)
                .cargoId(1L)
                .storageUnitId(1L)
                .quantity(100)
                .storedAt(LocalDateTime.now())
                .lastCheckedByUserId(1L)
                .lastInventoryCheck(LocalDateTime.now())
                .build();

        cargoStorageEntity = new CargoStorageEntity();
        cargoStorageEntity.setId(1L);
        cargoStorageEntity.setCargoId(1L);
        cargoStorageEntity.setStorageUnitId(1L);
        cargoStorageEntity.setQuantity(100);
    }

    @Test
    void save_Success() {
        // Given
        when(mapper.toEntity(cargoStorage)).thenReturn(cargoStorageEntity);
        when(jdbcRepository.save(cargoStorageEntity)).thenReturn(cargoStorageEntity);
        when(mapper.toDomain(cargoStorageEntity)).thenReturn(cargoStorage);

        // When
        CargoStorage result = adapter.save(cargoStorage);

        // Then
        assertNotNull(result);
        assertEquals(100, result.getQuantity());
        verify(jdbcRepository).save(cargoStorageEntity);
    }

    @Test
    void findById_Success() {
        // Given
        when(jdbcRepository.findById(1L)).thenReturn(Optional.of(cargoStorageEntity));
        when(mapper.toDomain(cargoStorageEntity)).thenReturn(cargoStorage);

        // When
        Optional<CargoStorage> result = adapter.findById(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals(100, result.get().getQuantity());
        verify(jdbcRepository).findById(1L);
    }

    @Test
    void findById_NotFound() {
        // Given
        when(jdbcRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<CargoStorage> result = adapter.findById(999L);

        // Then
        assertFalse(result.isPresent());
        verify(jdbcRepository).findById(999L);
        verify(mapper, never()).toDomain(any());
    }

    @Test
    void findAll_Success() {
        // Given
        when(jdbcRepository.findAll()).thenReturn(Arrays.asList(cargoStorageEntity));
        when(mapper.toDomain(cargoStorageEntity)).thenReturn(cargoStorage);

        // When
        List<CargoStorage> result = adapter.findAll();

        // Then
        assertEquals(1, result.size());
        assertEquals(100, result.get(0).getQuantity());
        verify(jdbcRepository).findAll();
    }

    @Test
    void findByStorageUnitId_Success() {
        // Given
        when(jdbcRepository.findByStorageUnitId(1L)).thenReturn(Arrays.asList(cargoStorageEntity));
        when(mapper.toDomain(cargoStorageEntity)).thenReturn(cargoStorage);

        // When
        List<CargoStorage> result = adapter.findByStorageUnitId(1L);

        // Then
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getStorageUnitId());
        verify(jdbcRepository).findByStorageUnitId(1L);
    }

    @Test
    void findByCargoId_Success() {
        // Given
        when(jdbcRepository.findByCargoId(1L)).thenReturn(Arrays.asList(cargoStorageEntity));
        when(mapper.toDomain(cargoStorageEntity)).thenReturn(cargoStorage);

        // When
        List<CargoStorage> result = adapter.findByCargoId(1L);

        // Then
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getCargoId());
        verify(jdbcRepository).findByCargoId(1L);
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

    @Test
    void findWithFilters_WithAllFilters() {
        // Given
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), any(), any(), any(), any(), any()))
                .thenReturn(Arrays.asList(cargoStorage));

        // When
        List<CargoStorage> result = adapter.findWithFilters(1L, 1L, 50, 10, 0);

        // Then
        assertEquals(1, result.size());
        verify(jdbcTemplate).query(anyString(), any(RowMapper.class), any(), any(), any(), any(), any());
    }

    @Test
    void findWithFilters_WithNoFilters() {
        // Given
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), any(), any()))
                .thenReturn(Arrays.asList(cargoStorage));

        // When
        List<CargoStorage> result = adapter.findWithFilters(null, null, null, 10, 0);

        // Then
        assertEquals(1, result.size());
        verify(jdbcTemplate).query(anyString(), any(RowMapper.class), any(), any());
    }
}
