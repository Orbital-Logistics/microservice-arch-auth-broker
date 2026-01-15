package org.orbitalLogistic.cargo.application.usecases;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.orbitalLogistic.cargo.application.ports.out.CargoRepository;
import org.orbitalLogistic.cargo.application.ports.out.CargoStorageRepository;
import org.orbitalLogistic.cargo.application.ports.out.StorageUnitRepository;
import org.orbitalLogistic.cargo.application.ports.out.UserServicePort;
import org.orbitalLogistic.cargo.domain.exception.CargoStorageNotFoundException;
import org.orbitalLogistic.cargo.domain.model.Cargo;
import org.orbitalLogistic.cargo.domain.model.CargoStorage;
import org.orbitalLogistic.cargo.domain.model.StorageUnit;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateCargoStorageServiceTest {

    @Mock
    private CargoStorageRepository cargoStorageRepository;

    @Mock
    private CargoRepository cargoRepository;

    @Mock
    private StorageUnitRepository storageUnitRepository;

    @Mock
    private UserServicePort userServicePort;

    @InjectMocks
    private UpdateCargoStorageService updateCargoStorageService;

    private CargoStorage storage;

    @BeforeEach
    void setUp() {
        storage = CargoStorage.builder()
                .id(1L)
                .storageUnitId(1L)
                .cargoId(1L)
                .quantity(100)
                .storedAt(LocalDateTime.now())
                .lastCheckedByUserId(1L)
                .build();
    }

    @Test
    void updateInventory_Success() {
        // Given
        Cargo cargo = Cargo.builder()
                .id(1L)
                .massPerUnit(BigDecimal.valueOf(10))
                .volumePerUnit(BigDecimal.valueOf(1))
                .build();
        
        StorageUnit storageUnit = StorageUnit.builder()
                .id(1L)
                .maxMass(BigDecimal.valueOf(1000))
                .maxVolume(BigDecimal.valueOf(100))
                .currentMass(BigDecimal.valueOf(500))
                .currentVolume(BigDecimal.valueOf(50))
                .build();
        
        when(cargoStorageRepository.findById(1L)).thenReturn(Optional.of(storage));
        when(cargoRepository.findById(1L)).thenReturn(Optional.of(cargo));
        when(storageUnitRepository.findById(1L)).thenReturn(Optional.of(storageUnit));
        when(storageUnitRepository.save(any(StorageUnit.class))).thenReturn(storageUnit);
        
        CargoStorage updated = CargoStorage.builder()
                .id(1L)
                .storageUnitId(1L)
                .cargoId(1L)
                .quantity(150)
                .storedAt(storage.getStoredAt())
                .lastCheckedByUserId(1L)
                .build();
        
        when(cargoStorageRepository.save(any(CargoStorage.class))).thenReturn(updated);

        // When
        CargoStorage result = updateCargoStorageService.updateInventory(1L, 150);

        // Then
        assertNotNull(result);
        assertEquals(150, result.getQuantity());
        verify(cargoStorageRepository).findById(1L);
        verify(cargoStorageRepository).save(any(CargoStorage.class));
    }

    @Test
    void updateInventory_ThrowsException_WhenNotFound() {
        // Given
        when(cargoStorageRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(CargoStorageNotFoundException.class, () -> {
            updateCargoStorageService.updateInventory(999L, 150);
        });

        verify(cargoStorageRepository).findById(999L);
        verify(cargoStorageRepository, never()).save(any(CargoStorage.class));
    }

    @Test
    void checkInventory_Success() {
        // Given
        when(cargoStorageRepository.findById(1L)).thenReturn(Optional.of(storage));
        when(userServicePort.userExists(2L)).thenReturn(true);
        
        CargoStorage checked = CargoStorage.builder()
                .id(1L)
                .storageUnitId(1L)
                .cargoId(1L)
                .quantity(100)
                .storedAt(storage.getStoredAt())
                .lastCheckedByUserId(2L)
                .lastInventoryCheck(LocalDateTime.now())
                .build();
        
        when(cargoStorageRepository.save(any(CargoStorage.class))).thenReturn(checked);

        // When
        CargoStorage result = updateCargoStorageService.checkInventory(1L, 2L);

        // Then
        assertNotNull(result);
        assertEquals(2L, result.getLastCheckedByUserId());
        assertNotNull(result.getLastInventoryCheck());
        verify(cargoStorageRepository).findById(1L);
        verify(userServicePort).userExists(2L);
        verify(cargoStorageRepository).save(any(CargoStorage.class));
    }
}
