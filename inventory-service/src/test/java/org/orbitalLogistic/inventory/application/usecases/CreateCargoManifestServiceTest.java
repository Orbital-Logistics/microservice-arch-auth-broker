package org.orbitalLogistic.inventory.application.usecases;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.orbitalLogistic.inventory.application.ports.in.CreateCargoManifestCommand;
import org.orbitalLogistic.inventory.application.ports.out.CargoManifestRepository;
import org.orbitalLogistic.inventory.application.ports.out.CargoValidationPort;
import org.orbitalLogistic.inventory.application.ports.out.SpacecraftValidationPort;
import org.orbitalLogistic.inventory.application.ports.out.UserValidationPort;
import org.orbitalLogistic.inventory.domain.model.CargoManifest;
import org.orbitalLogistic.inventory.domain.model.enums.ManifestPriority;
import org.orbitalLogistic.inventory.domain.model.enums.ManifestStatus;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateCargoManifestServiceTest {

    @Mock
    private CargoManifestRepository cargoManifestRepository;

    @Mock
    private CargoValidationPort cargoValidationPort;

    @Mock
    private SpacecraftValidationPort spacecraftValidationPort;

    @Mock
    private UserValidationPort userValidationPort;

    @InjectMocks
    private CreateCargoManifestService createCargoManifestService;

    private CreateCargoManifestCommand command;

    @BeforeEach
    void setUp() {
        command = new CreateCargoManifestCommand(
                1L,
                1L,
                1L,
                10,
                LocalDateTime.now(),
                null,
                1L,
                null,
                ManifestStatus.PENDING,
                ManifestPriority.NORMAL
        );
    }

    @Test
    @DisplayName("Should create cargo manifest successfully")
    void createManifest_Success() {
        when(spacecraftValidationPort.spacecraftExists(1L)).thenReturn(true);
        when(cargoValidationPort.cargoExists(1L)).thenReturn(true);
        when(cargoValidationPort.storageUnitExists(1L)).thenReturn(true);
        when(userValidationPort.userExists(1L)).thenReturn(true);

        CargoManifest savedManifest = CargoManifest.builder()
                .id(1L)
                .spacecraftId(1L)
                .cargoId(1L)
                .storageUnitId(1L)
                .quantity(10)
                .loadedAt(command.loadedAt())
                .loadedByUserId(1L)
                .manifestStatus(ManifestStatus.PENDING)
                .priority(ManifestPriority.NORMAL)
                .build();

        when(cargoManifestRepository.save(any(CargoManifest.class))).thenReturn(savedManifest);
        CargoManifest result = createCargoManifestService.createManifest(command);
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(cargoManifestRepository).save(any(CargoManifest.class));
    }

    @Test
    @DisplayName("Should throw exception when spacecraft not found")
    void createManifest_SpacecraftNotFound() {
        when(spacecraftValidationPort.spacecraftExists(1L)).thenReturn(false);
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> createCargoManifestService.createManifest(command)
        );

        assertEquals("Spacecraft not found with id: 1", exception.getMessage());
        verify(cargoManifestRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when cargo not found")
    void createManifest_CargoNotFound() {
        when(spacecraftValidationPort.spacecraftExists(1L)).thenReturn(true);
        when(cargoValidationPort.cargoExists(1L)).thenReturn(false);
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> createCargoManifestService.createManifest(command)
        );

        assertEquals("Cargo not found with id: 1", exception.getMessage());
        verify(cargoManifestRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when storage unit not found")
    void createManifest_StorageUnitNotFound() {
        when(spacecraftValidationPort.spacecraftExists(1L)).thenReturn(true);
        when(cargoValidationPort.cargoExists(1L)).thenReturn(true);
        when(cargoValidationPort.storageUnitExists(1L)).thenReturn(false);
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> createCargoManifestService.createManifest(command)
        );

        assertEquals("Storage unit not found with id: 1", exception.getMessage());
        verify(cargoManifestRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when user not found")
    void createManifest_UserNotFound() {
        when(spacecraftValidationPort.spacecraftExists(1L)).thenReturn(true);
        when(cargoValidationPort.cargoExists(1L)).thenReturn(true);
        when(cargoValidationPort.storageUnitExists(1L)).thenReturn(true);
        when(userValidationPort.userExists(1L)).thenReturn(false);
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> createCargoManifestService.createManifest(command)
        );

        assertEquals("Loaded by user not found with id: 1", exception.getMessage());
        verify(cargoManifestRepository, never()).save(any());
    }
}
