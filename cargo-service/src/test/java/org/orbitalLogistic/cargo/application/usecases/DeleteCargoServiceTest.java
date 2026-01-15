package org.orbitalLogistic.cargo.application.usecases;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.orbitalLogistic.cargo.application.ports.out.CargoRepository;
import org.orbitalLogistic.cargo.application.ports.out.CargoStorageRepository;
import org.orbitalLogistic.cargo.domain.exception.CargoInUseException;
import org.orbitalLogistic.cargo.domain.exception.CargoNotFoundException;
import org.orbitalLogistic.cargo.domain.model.Cargo;
import org.orbitalLogistic.cargo.domain.model.enums.CargoType;
import org.orbitalLogistic.cargo.domain.model.enums.HazardLevel;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteCargoServiceTest {

    @Mock
    private CargoRepository cargoRepository;

    @Mock
    private CargoStorageRepository cargoStorageRepository;

    @InjectMocks
    private DeleteCargoService deleteCargoService;

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
    void deleteCargo_Success() {
        // Given
        when(cargoRepository.existsById(1L)).thenReturn(true);
        when(cargoStorageRepository.sumQuantityByCargoId(1L)).thenReturn(0);
        doNothing().when(cargoRepository).deleteById(1L);

        // When
        assertDoesNotThrow(() -> deleteCargoService.deleteCargo(1L));

        // Then
        verify(cargoRepository).existsById(1L);
        verify(cargoStorageRepository).sumQuantityByCargoId(1L);
        verify(cargoRepository).deleteById(1L);
    }

    @Test
    void deleteCargo_ThrowsException_WhenCargoNotFound() {
        // Given
        when(cargoRepository.existsById(999L)).thenReturn(false);

        // When & Then
        assertThrows(CargoNotFoundException.class, () -> {
            deleteCargoService.deleteCargo(999L);
        });

        verify(cargoRepository).existsById(999L);
        verify(cargoStorageRepository, never()).sumQuantityByCargoId(anyLong());
        verify(cargoRepository, never()).deleteById(anyLong());
    }

    @Test
    void deleteCargo_ThrowsException_WhenCargoInUse() {
        // Given
        when(cargoRepository.existsById(1L)).thenReturn(true);
        when(cargoStorageRepository.sumQuantityByCargoId(1L)).thenReturn(10);

        // When & Then
        assertThrows(CargoInUseException.class, () -> {
            deleteCargoService.deleteCargo(1L);
        });

        verify(cargoRepository).existsById(1L);
        verify(cargoStorageRepository).sumQuantityByCargoId(1L);
        verify(cargoRepository, never()).deleteById(anyLong());
    }
}
