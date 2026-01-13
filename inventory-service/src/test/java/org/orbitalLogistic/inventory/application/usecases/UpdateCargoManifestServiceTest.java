package org.orbitalLogistic.inventory.application.usecases;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.orbitalLogistic.inventory.application.ports.in.UpdateCargoManifestCommand;
import org.orbitalLogistic.inventory.application.ports.out.CargoManifestRepository;
import org.orbitalLogistic.inventory.application.ports.out.CargoValidationPort;
import org.orbitalLogistic.inventory.application.ports.out.SpacecraftValidationPort;
import org.orbitalLogistic.inventory.application.ports.out.UserValidationPort;
import org.orbitalLogistic.inventory.domain.model.CargoManifest;
import org.orbitalLogistic.inventory.domain.model.enums.ManifestPriority;
import org.orbitalLogistic.inventory.domain.model.enums.ManifestStatus;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateCargoManifestServiceTest {

    @Mock
    private CargoManifestRepository cargoManifestRepository;

    @Mock
    private CargoValidationPort cargoValidationPort;

    @Mock
    private SpacecraftValidationPort spacecraftValidationPort;

    @Mock
    private UserValidationPort userValidationPort;

    @InjectMocks
    private UpdateCargoManifestService updateCargoManifestService;

    private CargoManifest existingManifest;
    private UpdateCargoManifestCommand command;

    @BeforeEach
    void setUp() {
        existingManifest = CargoManifest.builder()
                .id(1L)
                .spacecraftId(1L)
                .cargoId(1L)
                .storageUnitId(1L)
                .quantity(10)
                .loadedAt(LocalDateTime.now())
                .loadedByUserId(1L)
                .manifestStatus(ManifestStatus.PENDING)
                .priority(ManifestPriority.NORMAL)
                .build();

        command = new UpdateCargoManifestCommand(
                1L, 1L, 1L, 1L, 15,
                LocalDateTime.now(), null, 1L, null,
                ManifestStatus.LOADED, ManifestPriority.HIGH
        );
    }

    @Test
    @DisplayName("Should update manifest successfully")
    void updateManifest_Success() {
        when(cargoManifestRepository.findById(1L)).thenReturn(Optional.of(existingManifest));
        when(spacecraftValidationPort.spacecraftExists(1L)).thenReturn(true);
        when(cargoValidationPort.cargoExists(1L)).thenReturn(true);
        when(cargoValidationPort.storageUnitExists(1L)).thenReturn(true);
        when(userValidationPort.userExists(1L)).thenReturn(true);

        CargoManifest updatedManifest = existingManifest.toBuilder()
                .quantity(15)
                .manifestStatus(ManifestStatus.LOADED)
                .priority(ManifestPriority.HIGH)
                .build();

        when(cargoManifestRepository.save(any(CargoManifest.class))).thenReturn(updatedManifest);
        CargoManifest result = updateCargoManifestService.updateManifest(command);
        assertNotNull(result);
        assertEquals(15, result.getQuantity());
        assertEquals(ManifestStatus.LOADED, result.getManifestStatus());
        assertEquals(ManifestPriority.HIGH, result.getPriority());
        verify(cargoManifestRepository).save(any(CargoManifest.class));
    }

    @Test
    @DisplayName("Should throw exception when manifest not found")
    void updateManifest_NotFound() {
        when(cargoManifestRepository.findById(999L)).thenReturn(Optional.empty());

        UpdateCargoManifestCommand notFoundCommand = new UpdateCargoManifestCommand(
                999L, 1L, 1L, 1L, 10, null, null, 1L, null,
                ManifestStatus.PENDING, ManifestPriority.NORMAL
        );
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> updateCargoManifestService.updateManifest(notFoundCommand)
        );

        assertEquals("Cargo manifest not found with id: 999", exception.getMessage());
        verify(cargoManifestRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when spacecraft not found")
    void updateManifest_SpacecraftNotFound() {
        when(cargoManifestRepository.findById(1L)).thenReturn(Optional.of(existingManifest));
        when(spacecraftValidationPort.spacecraftExists(1L)).thenReturn(false);
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> updateCargoManifestService.updateManifest(command)
        );

        assertEquals("Spacecraft not found with id: 1", exception.getMessage());
        verify(cargoManifestRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should preserve existing values when command has nulls")
    void updateManifest_PreserveExistingValues() {
        UpdateCargoManifestCommand partialCommand = new UpdateCargoManifestCommand(
                1L, null, null, null, null, null, null, null, null,
                ManifestStatus.LOADED, null
        );

        when(cargoManifestRepository.findById(1L)).thenReturn(Optional.of(existingManifest));
        when(spacecraftValidationPort.spacecraftExists(1L)).thenReturn(true);
        when(cargoValidationPort.cargoExists(1L)).thenReturn(true);
        when(cargoValidationPort.storageUnitExists(1L)).thenReturn(true);
        when(userValidationPort.userExists(1L)).thenReturn(true);

        CargoManifest updatedManifest = existingManifest.toBuilder()
                .manifestStatus(ManifestStatus.LOADED)
                .build();

        when(cargoManifestRepository.save(any(CargoManifest.class))).thenReturn(updatedManifest);
        CargoManifest result = updateCargoManifestService.updateManifest(partialCommand);
        assertNotNull(result);
        assertEquals(1L, result.getSpacecraftId()); // preserved
        assertEquals(1L, result.getCargoId()); // preserved
        assertEquals(10, result.getQuantity()); // preserved
        assertEquals(ManifestStatus.LOADED, result.getManifestStatus()); // updated
        assertEquals(ManifestPriority.NORMAL, result.getPriority()); // preserved
    }
}
