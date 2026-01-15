package org.orbitalLogistic.cargo.application.usecases;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.orbitalLogistic.cargo.application.ports.out.CargoRepository;
import org.orbitalLogistic.cargo.domain.model.Cargo;
import org.orbitalLogistic.cargo.domain.model.enums.CargoType;
import org.orbitalLogistic.cargo.domain.model.enums.HazardLevel;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetCargoServiceTest {

    @Mock
    private CargoRepository cargoRepository;

    @InjectMocks
    private GetCargoService getCargoService;

    private Cargo cargo;

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
    }

    @Test
    void getCargoById_Success() {
        // Given
        when(cargoRepository.findById(1L)).thenReturn(Optional.of(cargo));

        // When
        Optional<Cargo> result = getCargoService.getCargoById(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals("Laptop", result.get().getName());
        
        verify(cargoRepository).findById(1L);
    }

    @Test
    void getCargoById_ReturnsEmpty_WhenNotFound() {
        // Given
        when(cargoRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<Cargo> result = getCargoService.getCargoById(999L);

        // Then
        assertFalse(result.isPresent());
        verify(cargoRepository).findById(999L);
    }

    @Test
    void getAllCargos_Success() {
        // Given
        Cargo cargo2 = Cargo.builder()
                .id(2L)
                .name("Monitor")
                .cargoCategoryId(1L)
                .cargoType(CargoType.EQUIPMENT)
                .hazardLevel(HazardLevel.LOW)
                .massPerUnit(BigDecimal.valueOf(5.0))
                .volumePerUnit(BigDecimal.valueOf(0.1))
                .build();
        
        when(cargoRepository.findWithFilters(null, null, null, 10, 0)).thenReturn(Arrays.asList(cargo, cargo2));

        // When
        List<Cargo> result = getCargoService.getAllCargos(0, 10);

        // Then
        assertEquals(2, result.size());
        assertEquals("Laptop", result.get(0).getName());
        assertEquals("Monitor", result.get(1).getName());
        
        verify(cargoRepository).findWithFilters(null, null, null, 10, 0);
    }

    @Test
    void searchCargos_Success() {
        // Given
        when(cargoRepository.findWithFilters("Laptop", "EQUIPMENT", null, 10, 0))
                .thenReturn(Arrays.asList(cargo));

        // When
        List<Cargo> result = getCargoService.searchCargos("Laptop", CargoType.EQUIPMENT, null, 0, 10);

        // Then
        assertEquals(1, result.size());
        assertEquals("Laptop", result.get(0).getName());
        assertEquals(CargoType.EQUIPMENT, result.get(0).getCargoType());
        
        verify(cargoRepository).findWithFilters("Laptop", "EQUIPMENT", null, 10, 0);
    }
}
