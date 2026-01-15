package org.orbitalLogistic.cargo.application.usecases;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.orbitalLogistic.cargo.application.ports.out.CargoStorageRepository;
import org.orbitalLogistic.cargo.application.ports.out.StorageUnitRepository;
import org.orbitalLogistic.cargo.domain.exception.StorageUnitNotFoundException;
import org.orbitalLogistic.cargo.domain.model.CargoStorage;
import org.orbitalLogistic.cargo.domain.model.StorageUnit;
import org.orbitalLogistic.cargo.domain.model.enums.StorageTypeEnum;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteStorageUnitServiceTest {

    @Mock
    private StorageUnitRepository storageUnitRepository;

    @Mock
    private CargoStorageRepository cargoStorageRepository;

    @InjectMocks
    private DeleteStorageUnitService deleteStorageUnitService;

    private StorageUnit storageUnit;

    @BeforeEach
    void setUp() {
        storageUnit = StorageUnit.builder()
                .id(1L)
                .unitCode("UNIT-001")
                .storageType(StorageTypeEnum.AMBIENT)
                .location("Warehouse A")
                .maxMass(BigDecimal.valueOf(1000))
                .maxVolume(BigDecimal.valueOf(50))
                .currentMass(BigDecimal.ZERO)
                .currentVolume(BigDecimal.ZERO)
                .isActive(true)
                .build();
    }

    @Test
    void deleteUnit_Success() {
        // Given
        when(storageUnitRepository.existsById(1L)).thenReturn(true);
        when(cargoStorageRepository.findByStorageUnitId(1L)).thenReturn(List.of());
        doNothing().when(storageUnitRepository).deleteById(1L);

        // When
        assertDoesNotThrow(() -> deleteStorageUnitService.deleteUnit(1L));

        // Then
        verify(storageUnitRepository).existsById(1L);
        verify(cargoStorageRepository).findByStorageUnitId(1L);
        verify(storageUnitRepository).deleteById(1L);
    }

    @Test
    void deleteUnit_ThrowsException_WhenUnitNotFound() {
        // Given
        when(storageUnitRepository.existsById(999L)).thenReturn(false);

        // When & Then
        assertThrows(StorageUnitNotFoundException.class, () -> {
            deleteStorageUnitService.deleteUnit(999L);
        });

        verify(storageUnitRepository).existsById(999L);
        verify(cargoStorageRepository, never()).findByStorageUnitId(anyLong());
        verify(storageUnitRepository, never()).deleteById(anyLong());
    }

    @Test
    void deleteUnit_DeletesRelatedStorages() {
        // Given
        CargoStorage storage1 = CargoStorage.builder().id(1L).cargoId(1L).storageUnitId(1L).quantity(10).build();
        CargoStorage storage2 = CargoStorage.builder().id(2L).cargoId(2L).storageUnitId(1L).quantity(20).build();
        
        when(storageUnitRepository.existsById(1L)).thenReturn(true);
        when(cargoStorageRepository.findByStorageUnitId(1L)).thenReturn(List.of(storage1, storage2));
        doNothing().when(cargoStorageRepository).deleteById(anyLong());
        doNothing().when(storageUnitRepository).deleteById(1L);

        // When
        assertDoesNotThrow(() -> deleteStorageUnitService.deleteUnit(1L));

        // Then
        verify(storageUnitRepository).existsById(1L);
        verify(cargoStorageRepository).findByStorageUnitId(1L);
        verify(cargoStorageRepository).deleteById(1L);
        verify(cargoStorageRepository).deleteById(2L);
        verify(storageUnitRepository).deleteById(1L);
    }
}
