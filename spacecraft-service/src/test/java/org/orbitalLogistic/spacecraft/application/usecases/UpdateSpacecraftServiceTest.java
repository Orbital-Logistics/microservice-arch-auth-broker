package org.orbitalLogistic.spacecraft.application.usecases;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.orbitalLogistic.spacecraft.application.ports.in.UpdateSpacecraftCommand;
import org.orbitalLogistic.spacecraft.application.ports.out.SpacecraftRepository;
import org.orbitalLogistic.spacecraft.application.ports.out.SpacecraftTypeRepository;
import org.orbitalLogistic.spacecraft.domain.model.Spacecraft;
import org.orbitalLogistic.spacecraft.domain.model.enums.SpacecraftStatus;
import org.orbitalLogistic.spacecraft.exceptions.SpacecraftAlreadyExistsException;
import org.orbitalLogistic.spacecraft.exceptions.SpacecraftNotFoundException;
import org.orbitalLogistic.spacecraft.exceptions.DataNotFoundException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateSpacecraftServiceTest {

    @Mock
    private SpacecraftRepository spacecraftRepository;

    @Mock
    private SpacecraftTypeRepository spacecraftTypeRepository;

    @InjectMocks
    private UpdateSpacecraftService updateSpacecraftService;

    private Spacecraft existingSpacecraft;
    private UpdateSpacecraftCommand updateCommand;

    @BeforeEach
    void setUp() {
        existingSpacecraft = Spacecraft.builder()
                .id(1L)
                .registryCode("SC-001")
                .name("Star Carrier")
                .spacecraftTypeId(1L)
                .massCapacity(new BigDecimal("50000"))
                .volumeCapacity(new BigDecimal("10000"))
                .status(SpacecraftStatus.DOCKED)
                .currentLocation("Earth Orbit")
                .build();

        updateCommand = new UpdateSpacecraftCommand(
                1L,
                "SC-001",
                "Star Carrier Updated",
                1L,
                new BigDecimal("60000"),
                new BigDecimal("12000"),
                SpacecraftStatus.IN_TRANSIT,
                "Mars Orbit"
        );
    }

    @Test
    @DisplayName("Обновление космического корабля - успешно")
    void updateSpacecraft_Success() {
        when(spacecraftRepository.findById(1L)).thenReturn(Optional.of(existingSpacecraft));
        when(spacecraftTypeRepository.existsById(1L)).thenReturn(true);

        Spacecraft updatedSpacecraft = Spacecraft.builder()
                .id(updateCommand.id())
                .registryCode(updateCommand.registryCode())
                .name(updateCommand.name())
                .spacecraftTypeId(updateCommand.spacecraftTypeId())
                .massCapacity(updateCommand.massCapacity())
                .volumeCapacity(updateCommand.volumeCapacity())
                .status(updateCommand.status())
                .currentLocation(updateCommand.currentLocation())
                .build();

        when(spacecraftRepository.save(any(Spacecraft.class))).thenReturn(updatedSpacecraft);

        Spacecraft result = updateSpacecraftService.updateSpacecraft(updateCommand);

        assertNotNull(result);
        assertEquals("Star Carrier Updated", result.getName());
        assertEquals(SpacecraftStatus.IN_TRANSIT, result.getStatus());

        verify(spacecraftRepository).findById(1L);
        verify(spacecraftTypeRepository).existsById(1L);
        verify(spacecraftRepository).save(any(Spacecraft.class));
    }

    @Test
    @DisplayName("Обновление космического корабля - корабль не найден")
    void updateSpacecraft_SpacecraftNotFound() {
        when(spacecraftRepository.findById(999L)).thenReturn(Optional.empty());

        UpdateSpacecraftCommand command = new UpdateSpacecraftCommand(
                999L, "SC-999", "Unknown", 1L,
                new BigDecimal("50000"), new BigDecimal("10000"),
                SpacecraftStatus.DOCKED, "Earth"
        );

        assertThrows(SpacecraftNotFoundException.class,
                () -> updateSpacecraftService.updateSpacecraft(command));

        verify(spacecraftRepository).findById(999L);
        verify(spacecraftRepository, never()).save(any());
    }

    @Test
    @DisplayName("Обновление космического корабля - регистрационный код занят другим кораблем")
    void updateSpacecraft_RegistryCodeTakenByAnother() {
        UpdateSpacecraftCommand commandWithDifferentCode = new UpdateSpacecraftCommand(
                1L, "SC-002", "Star Carrier Updated", 1L,
                new BigDecimal("60000"), new BigDecimal("12000"),
                SpacecraftStatus.IN_TRANSIT, "Mars Orbit"
        );

        when(spacecraftRepository.findById(1L)).thenReturn(Optional.of(existingSpacecraft));
        when(spacecraftRepository.existsByRegistryCode("SC-002")).thenReturn(true);

        assertThrows(SpacecraftAlreadyExistsException.class,
                () -> updateSpacecraftService.updateSpacecraft(commandWithDifferentCode));

        verify(spacecraftRepository).findById(1L);
        verify(spacecraftRepository).existsByRegistryCode("SC-002");
        verify(spacecraftRepository, never()).save(any());
    }

    @Test
    @DisplayName("Обновление космического корабля - тип не найден")
    void updateSpacecraft_TypeNotFound() {
        UpdateSpacecraftCommand commandWithInvalidType = new UpdateSpacecraftCommand(
                1L, "SC-001", "Star Carrier Updated", 999L,
                new BigDecimal("60000"), new BigDecimal("12000"),
                SpacecraftStatus.IN_TRANSIT, "Mars Orbit"
        );

        when(spacecraftRepository.findById(1L)).thenReturn(Optional.of(existingSpacecraft));
        when(spacecraftTypeRepository.existsById(999L)).thenReturn(false);

        assertThrows(DataNotFoundException.class,
                () -> updateSpacecraftService.updateSpacecraft(commandWithInvalidType));

        verify(spacecraftRepository).findById(1L);
        verify(spacecraftTypeRepository).existsById(999L);
        verify(spacecraftRepository, never()).save(any());
    }
}

