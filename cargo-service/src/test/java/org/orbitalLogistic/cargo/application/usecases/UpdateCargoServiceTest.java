package org.orbitalLogistic.cargo.application.usecases;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.orbitalLogistic.cargo.application.ports.out.CargoCategoryRepository;
import org.orbitalLogistic.cargo.application.ports.out.CargoRepository;
import org.orbitalLogistic.cargo.domain.exception.CargoCategoryNotFoundException;
import org.orbitalLogistic.cargo.domain.exception.CargoNotFoundException;
import org.orbitalLogistic.cargo.domain.model.Cargo;
import org.orbitalLogistic.cargo.domain.model.CargoCategory;
import org.orbitalLogistic.cargo.domain.model.enums.CargoType;
import org.orbitalLogistic.cargo.domain.model.enums.HazardLevel;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateCargoServiceTest {

    @Mock
    private CargoRepository cargoRepository;

    @Mock
    private CargoCategoryRepository cargoCategoryRepository;

    @InjectMocks
    private UpdateCargoService updateCargoService;

    private Cargo existingCargo;
    private Cargo updatedCargo;
    private CargoCategory category;

    @BeforeEach
    void setUp() {
        category = CargoCategory.builder()
                .id(1L)
                .name("Electronics")
                .build();

        existingCargo = Cargo.builder()
                .id(1L)
                .name("Laptop")
                .cargoCategoryId(1L)
                .cargoType(CargoType.EQUIPMENT)
                .hazardLevel(HazardLevel.LOW)
                .massPerUnit(BigDecimal.valueOf(2.5))
                .volumePerUnit(BigDecimal.valueOf(0.05))
                .build();

        updatedCargo = Cargo.builder()
                .id(1L)
                .name("Updated Laptop")
                .cargoCategoryId(1L)
                .cargoType(CargoType.EQUIPMENT)
                .hazardLevel(HazardLevel.MEDIUM)
                .massPerUnit(BigDecimal.valueOf(3.0))
                .volumePerUnit(BigDecimal.valueOf(0.06))
                .build();
    }

    @Test
    void updateCargo_Success() {
        // Given
        when(cargoRepository.findById(1L)).thenReturn(Optional.of(existingCargo));
        when(cargoRepository.existsByName("Updated Laptop")).thenReturn(false);
        when(cargoCategoryRepository.existsById(1L)).thenReturn(true);
        when(cargoRepository.save(any(Cargo.class))).thenReturn(updatedCargo);

        // When
        Cargo result = updateCargoService.updateCargo(1L, updatedCargo);

        // Then
        assertNotNull(result);
        assertEquals("Updated Laptop", result.getName());
        assertEquals(HazardLevel.MEDIUM, result.getHazardLevel());
        
        verify(cargoRepository).findById(1L);
        verify(cargoCategoryRepository).existsById(1L);
        verify(cargoRepository).save(any(Cargo.class));
    }

    @Test
    void updateCargo_ThrowsException_WhenCargoNotFound() {
        // Given
        when(cargoRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(CargoNotFoundException.class, () -> {
            updateCargoService.updateCargo(999L, updatedCargo);
        });

        verify(cargoRepository).findById(999L);
        verify(cargoRepository, never()).save(any(Cargo.class));
    }

    @Test
    void updateCargo_ThrowsException_WhenCategoryNotFound() {
        // Given
        updatedCargo.setCargoCategoryId(999L);
        when(cargoRepository.findById(1L)).thenReturn(Optional.of(existingCargo));
        when(cargoRepository.existsByName("Updated Laptop")).thenReturn(false);
        when(cargoCategoryRepository.existsById(999L)).thenReturn(false);

        // When & Then
        assertThrows(CargoCategoryNotFoundException.class, () -> {
            updateCargoService.updateCargo(1L, updatedCargo);
        });

        verify(cargoRepository).findById(1L);
        verify(cargoCategoryRepository).existsById(999L);
        verify(cargoRepository, never()).save(any(Cargo.class));
    }

}
