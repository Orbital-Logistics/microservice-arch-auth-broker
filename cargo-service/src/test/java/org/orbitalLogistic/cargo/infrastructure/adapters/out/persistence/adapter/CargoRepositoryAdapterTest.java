package org.orbitalLogistic.cargo.infrastructure.adapters.out.persistence.adapter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.orbitalLogistic.cargo.domain.model.Cargo;
import org.orbitalLogistic.cargo.domain.model.enums.CargoType;
import org.orbitalLogistic.cargo.domain.model.enums.HazardLevel;
import org.orbitalLogistic.cargo.infrastructure.adapters.out.persistence.CargoRepositoryAdapter;
import org.orbitalLogistic.cargo.infrastructure.adapters.out.persistence.entity.CargoEntity;
import org.orbitalLogistic.cargo.infrastructure.adapters.out.persistence.mapper.CargoPersistenceMapper;
import org.orbitalLogistic.cargo.infrastructure.adapters.out.persistence.repository.CargoJdbcRepository;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CargoRepositoryAdapterTest {

    @Mock
    private CargoJdbcRepository jdbcRepository;

    @Mock
    private CargoPersistenceMapper mapper;

    @InjectMocks
    private CargoRepositoryAdapter cargoRepositoryAdapter;

    private Cargo cargo;
    private CargoEntity cargoEntity;

    @BeforeEach
    void setUp() {
        cargo = Cargo.builder()
                .id(1L)
                .name("Laptop")
                .cargoCategoryId(1L)
                .cargoType(CargoType.EQUIPMENT)
                .hazardLevel(HazardLevel.LOW)
                .massPerUnit(BigDecimal.valueOf(2.5))
                .volumePerUnit(BigDecimal.valueOf(0.05))
                .build();

        cargoEntity = new CargoEntity();
        cargoEntity.setId(1L);
        cargoEntity.setName("Laptop");
        cargoEntity.setCargoCategoryId(1L);
        cargoEntity.setCargoType("EQUIPMENT");
        cargoEntity.setHazardLevel("LOW");
        cargoEntity.setMassPerUnit(BigDecimal.valueOf(2.5));
        cargoEntity.setVolumePerUnit(BigDecimal.valueOf(0.05));
    }

    @Test
    void save_Success() {
        // Given
        when(mapper.toEntity(cargo)).thenReturn(cargoEntity);
        when(jdbcRepository.save(cargoEntity)).thenReturn(cargoEntity);
        when(mapper.toDomain(cargoEntity)).thenReturn(cargo);

        // When
        Cargo result = cargoRepositoryAdapter.save(cargo);

        // Then
        assertNotNull(result);
        assertEquals("Laptop", result.getName());
        verify(jdbcRepository).save(cargoEntity);
    }

    @Test
    void findById_Success() {
        // Given
        when(jdbcRepository.findById(1L)).thenReturn(Optional.of(cargoEntity));
        when(mapper.toDomain(cargoEntity)).thenReturn(cargo);

        // When
        Optional<Cargo> result = cargoRepositoryAdapter.findById(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals("Laptop", result.get().getName());
        verify(jdbcRepository).findById(1L);
    }

    @Test
    void findById_NotFound() {
        // Given
        when(jdbcRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<Cargo> result = cargoRepositoryAdapter.findById(999L);

        // Then
        assertFalse(result.isPresent());
        verify(jdbcRepository).findById(999L);
        verify(mapper, never()).toDomain(any());
    }

    @Test
    void findAll_Success() {
        // Given
        when(jdbcRepository.findAll()).thenReturn(Arrays.asList(cargoEntity));
        when(mapper.toDomain(cargoEntity)).thenReturn(cargo);

        // When
        List<Cargo> result = cargoRepositoryAdapter.findAll();

        // Then
        assertEquals(1, result.size());
        assertEquals("Laptop", result.get(0).getName());
        verify(jdbcRepository).findAll();
    }

    @Test
    void existsByName_ReturnsTrue() {
        // Given
        when(jdbcRepository.existsByName("Laptop")).thenReturn(true);

        // When
        boolean result = cargoRepositoryAdapter.existsByName("Laptop");

        // Then
        assertTrue(result);
        verify(jdbcRepository).existsByName("Laptop");
    }

    @Test
    void existsByName_ReturnsFalse() {
        // Given
        when(jdbcRepository.existsByName("Monitor")).thenReturn(false);

        // When
        boolean result = cargoRepositoryAdapter.existsByName("Monitor");

        // Then
        assertFalse(result);
        verify(jdbcRepository).existsByName("Monitor");
    }

    @Test
    void deleteById_Success() {
        // Given
        doNothing().when(jdbcRepository).deleteById(1L);

        // When
        cargoRepositoryAdapter.deleteById(1L);

        // Then
        verify(jdbcRepository).deleteById(1L);
    }
}
