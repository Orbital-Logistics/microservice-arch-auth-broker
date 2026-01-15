package org.orbitalLogistic.cargo.application.usecases;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.orbitalLogistic.cargo.application.ports.out.CargoCategoryRepository;
import org.orbitalLogistic.cargo.application.ports.out.CargoRepository;
import org.orbitalLogistic.cargo.domain.exception.CargoAlreadyExistsException;
import org.orbitalLogistic.cargo.domain.exception.CargoCategoryNotFoundException;
import org.orbitalLogistic.cargo.domain.model.Cargo;
import org.orbitalLogistic.cargo.domain.model.CargoCategory;
import org.orbitalLogistic.cargo.domain.model.enums.CargoType;
import org.orbitalLogistic.cargo.domain.model.enums.HazardLevel;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateCargoServiceTest {

    @Mock
    private CargoRepository cargoRepository;

    @Mock
    private CargoCategoryRepository cargoCategoryRepository;

    @InjectMocks
    private CreateCargoService createCargoService;

    private Cargo cargo;
    private CargoCategory category;

    @BeforeEach
    void setUp() {
        category = CargoCategory.builder()
                .id(1L)
                .name("Electronics")
                .build();

        cargo = Cargo.builder()
                .name("Laptop")
                .cargoCategoryId(1L)
                .massPerUnit(BigDecimal.valueOf(2.5))
                .volumePerUnit(BigDecimal.valueOf(0.05))
                .cargoType(CargoType.EQUIPMENT)
                .hazardLevel(HazardLevel.LOW)
                .build();
    }

    @Test
    void createCargo_Success() {
        // Given
        when(cargoRepository.existsByName("Laptop")).thenReturn(false);
        when(cargoCategoryRepository.existsById(1L)).thenReturn(true);
        
        Cargo savedCargo = Cargo.builder()
                .id(1L)
                .name("Laptop")
                .cargoCategoryId(1L)
                .cargoType(CargoType.EQUIPMENT)
                .hazardLevel(HazardLevel.LOW)
                .massPerUnit(BigDecimal.valueOf(2.5))
                .volumePerUnit(BigDecimal.valueOf(0.05))
                .build();
        
        when(cargoRepository.save(any(Cargo.class))).thenReturn(savedCargo);

        // When
        Cargo result = createCargoService.createCargo(cargo);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Laptop", result.getName());
        assertEquals(CargoType.EQUIPMENT, result.getCargoType());
        
        verify(cargoRepository).existsByName("Laptop");
        verify(cargoCategoryRepository).existsById(1L);
        verify(cargoRepository).save(any(Cargo.class));
    }

    @Test
    void createCargo_ThrowsException_WhenNameExists() {
        // Given
        when(cargoRepository.existsByName("Laptop")).thenReturn(true);

        // When & Then
        assertThrows(CargoAlreadyExistsException.class, () -> {
            createCargoService.createCargo(cargo);
        });

        verify(cargoRepository).existsByName("Laptop");
        verify(cargoCategoryRepository, never()).existsById(anyLong());
        verify(cargoRepository, never()).save(any(Cargo.class));
    }

    @Test
    void createCargo_ThrowsException_WhenCategoryNotFound() {
        // Given
        when(cargoRepository.existsByName("Laptop")).thenReturn(false);
        when(cargoCategoryRepository.existsById(1L)).thenReturn(false);

        // When & Then
        assertThrows(CargoCategoryNotFoundException.class, () -> {
            createCargoService.createCargo(cargo);
        });

        verify(cargoRepository).existsByName("Laptop");
        verify(cargoCategoryRepository).existsById(1L);
        verify(cargoRepository, never()).save(any(Cargo.class));
    }

}
