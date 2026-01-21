package org.orbitalLogistic.cargo.application.usecases;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.orbitalLogistic.cargo.application.ports.out.*;
import org.orbitalLogistic.cargo.domain.exception.*;
import org.orbitalLogistic.cargo.domain.model.Cargo;
import org.orbitalLogistic.cargo.domain.model.CargoStorage;
import org.orbitalLogistic.cargo.domain.model.StorageUnit;
import org.orbitalLogistic.cargo.domain.model.enums.CargoType;
import org.orbitalLogistic.cargo.domain.model.enums.HazardLevel;
import org.orbitalLogistic.cargo.domain.model.enums.StorageTypeEnum;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateCargoStorageServiceTest {

    @Mock
    private CargoStorageRepository cargoStorageRepository;

    @Mock
    private CargoRepository cargoRepository;

    @Mock
    private StorageUnitRepository storageUnitRepository;

    @Mock
    private UserServicePort userServicePort;

    @Mock
    private ReportSender reportSender;

    @InjectMocks
    private CreateCargoStorageService createCargoStorageService;

    private Cargo cargo;
    private StorageUnit storageUnit;
    private CargoStorage cargoStorage;

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

        storageUnit = StorageUnit.builder()
                .id(1L)
                .unitCode("UNIT-001")
                .storageType(StorageTypeEnum.AMBIENT)
                .location("Warehouse A")
                .maxMass(BigDecimal.valueOf(1000))
                .maxVolume(BigDecimal.valueOf(50))
                .currentMass(BigDecimal.valueOf(100))
                .currentVolume(BigDecimal.valueOf(10))
                .isActive(true)
                .build();

        cargoStorage = CargoStorage.builder()
                .cargoId(1L)
                .storageUnitId(1L)
                .quantity(10)
                .storedAt(LocalDateTime.now())
                .lastCheckedByUserId(1L)
                .lastInventoryCheck(LocalDateTime.now())
                .build();
    }

    @Test
    void createStorage_Success() {
        // Given
        when(cargoRepository.findById(1L)).thenReturn(Optional.of(cargo));
        when(storageUnitRepository.findById(1L)).thenReturn(Optional.of(storageUnit));
        when(userServicePort.userExists(1L)).thenReturn(true);
        
        CargoStorage savedStorage = CargoStorage.builder()
                .id(1L)
                .cargoId(1L)
                .storageUnitId(1L)
                .quantity(10)
                .storedAt(cargoStorage.getStoredAt())
                .lastCheckedByUserId(1L)
                .lastInventoryCheck(cargoStorage.getLastInventoryCheck())
                .build();
        
        when(cargoStorageRepository.save(any(CargoStorage.class))).thenReturn(savedStorage);

        // When
        CargoStorage result = createCargoStorageService.createStorage(cargoStorage);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(1L, result.getCargoId());
        assertEquals(10, result.getQuantity());
        
        verify(cargoRepository).findById(1L);
        verify(storageUnitRepository).findById(1L);
        verify(userServicePort).userExists(1L);
        verify(storageUnitRepository).save(any(StorageUnit.class));
        verify(cargoStorageRepository).save(any(CargoStorage.class));
    }

    @Test
    void createStorage_ThrowsException_WhenCargoNotFound() {
        // Given
        when(cargoRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(CargoNotFoundException.class, () -> {
            createCargoStorageService.createStorage(cargoStorage);
        });

        verify(cargoRepository).findById(1L);
        verify(storageUnitRepository, never()).findById(anyLong());
        verify(cargoStorageRepository, never()).save(any(CargoStorage.class));
    }

    @Test
    void createStorage_ThrowsException_WhenStorageUnitNotFound() {
        // Given
        when(cargoRepository.findById(1L)).thenReturn(Optional.of(cargo));
        when(storageUnitRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(StorageUnitNotFoundException.class, () -> {
            createCargoStorageService.createStorage(cargoStorage);
        });

        verify(cargoRepository).findById(1L);
        verify(storageUnitRepository).findById(1L);
        verify(cargoStorageRepository, never()).save(any(CargoStorage.class));
    }

    @Test
    void createStorage_ThrowsException_WhenUserNotFound() {
        // Given
        when(cargoRepository.findById(1L)).thenReturn(Optional.of(cargo));
        when(storageUnitRepository.findById(1L)).thenReturn(Optional.of(storageUnit));
        when(userServicePort.userExists(1L)).thenReturn(false);

        // When & Then
        assertThrows(UserNotFoundException.class, () -> {
            createCargoStorageService.createStorage(cargoStorage);
        });

        verify(cargoRepository).findById(1L);
        verify(storageUnitRepository).findById(1L);
        verify(userServicePort).userExists(1L);
        verify(cargoStorageRepository, never()).save(any(CargoStorage.class));
    }

    @Test
    void createStorage_ThrowsException_WhenInsufficientMassCapacity() {
        // Given
        storageUnit.setCurrentMass(BigDecimal.valueOf(995)); // Only 5kg available
        when(cargoRepository.findById(1L)).thenReturn(Optional.of(cargo));
        when(storageUnitRepository.findById(1L)).thenReturn(Optional.of(storageUnit));
        when(userServicePort.userExists(1L)).thenReturn(true);

        // When & Then
        assertThrows(InsufficientCapacityException.class, () -> {
            createCargoStorageService.createStorage(cargoStorage);
        });

        verify(cargoRepository).findById(1L);
        verify(storageUnitRepository).findById(1L);
        verify(cargoStorageRepository, never()).save(any(CargoStorage.class));
    }

    @Test
    void createStorage_ThrowsException_WhenInsufficientVolumeCapacity() {
        // Given
        storageUnit.setCurrentVolume(BigDecimal.valueOf(49.8)); // Only 0.2m³ available
        when(cargoRepository.findById(1L)).thenReturn(Optional.of(cargo));
        when(storageUnitRepository.findById(1L)).thenReturn(Optional.of(storageUnit));
        when(userServicePort.userExists(1L)).thenReturn(true);

        // When & Then
        assertThrows(InsufficientCapacityException.class, () -> {
            createCargoStorageService.createStorage(cargoStorage);
        });

        verify(cargoRepository).findById(1L);
        verify(storageUnitRepository).findById(1L);
        verify(cargoStorageRepository, never()).save(any(CargoStorage.class));
    }

    @Test
    void createStorage_Success_WhenUserIdIsNull() {
        // Given
        cargoStorage.setLastCheckedByUserId(null);
        when(cargoRepository.findById(1L)).thenReturn(Optional.of(cargo));
        when(storageUnitRepository.findById(1L)).thenReturn(Optional.of(storageUnit));
        
        CargoStorage savedStorage = CargoStorage.builder()
                .id(1L)
                .cargoId(1L)
                .storageUnitId(1L)
                .quantity(10)
                .storedAt(cargoStorage.getStoredAt())
                .build();
        
        when(cargoStorageRepository.save(any(CargoStorage.class))).thenReturn(savedStorage);

        // When
        CargoStorage result = createCargoStorageService.createStorage(cargoStorage);

        // Then
        assertNotNull(result);
        verify(userServicePort, never()).userExists(anyLong());
        verify(cargoStorageRepository).save(any(CargoStorage.class));
    }

    @Test
    void createStorage_Success_WhenExactlyEnoughCapacity() {
        // Given
        storageUnit.setCurrentMass(BigDecimal.valueOf(975)); // Exactly 25kg available (10 * 2.5)
        storageUnit.setCurrentVolume(BigDecimal.valueOf(49.5)); // Exactly 0.5m³ available (10 * 0.05)
        
        when(cargoRepository.findById(1L)).thenReturn(Optional.of(cargo));
        when(storageUnitRepository.findById(1L)).thenReturn(Optional.of(storageUnit));
        when(userServicePort.userExists(1L)).thenReturn(true);
        
        CargoStorage savedStorage = CargoStorage.builder()
                .id(1L)
                .cargoId(1L)
                .storageUnitId(1L)
                .quantity(10)
                .storedAt(cargoStorage.getStoredAt())
                .lastCheckedByUserId(1L)
                .lastInventoryCheck(cargoStorage.getLastInventoryCheck())
                .build();
        
        when(cargoStorageRepository.save(any(CargoStorage.class))).thenReturn(savedStorage);

        // When
        CargoStorage result = createCargoStorageService.createStorage(cargoStorage);

        // Then
        assertNotNull(result);
        verify(cargoStorageRepository).save(any(CargoStorage.class));
    }
}
