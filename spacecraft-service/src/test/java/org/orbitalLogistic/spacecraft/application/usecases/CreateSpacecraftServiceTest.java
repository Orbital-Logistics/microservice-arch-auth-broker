package org.orbitalLogistic.spacecraft.application.usecases;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.orbitalLogistic.spacecraft.application.ports.in.CreateSpacecraftCommand;
import org.orbitalLogistic.spacecraft.application.ports.out.SpacecraftRepository;
import org.orbitalLogistic.spacecraft.application.ports.out.SpacecraftTypeRepository;
import org.orbitalLogistic.spacecraft.domain.model.Spacecraft;
import org.orbitalLogistic.spacecraft.domain.model.enums.SpacecraftStatus;
import org.orbitalLogistic.spacecraft.exceptions.SpacecraftAlreadyExistsException;
import org.orbitalLogistic.spacecraft.exceptions.DataNotFoundException;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateSpacecraftServiceTest {

    @Mock
    private SpacecraftRepository spacecraftRepository;

    @Mock
    private SpacecraftTypeRepository spacecraftTypeRepository;

    @InjectMocks
    private CreateSpacecraftService createSpacecraftService;

    private CreateSpacecraftCommand command;

    @BeforeEach
    void setUp() {
        command = new CreateSpacecraftCommand(
                "SC-001",
                "Star Carrier",
                1L,
                new BigDecimal("50000"),
                new BigDecimal("10000"),
                SpacecraftStatus.DOCKED,
                "Earth Orbit"
        );
    }

    @Test
    @DisplayName("Создание космического корабля - успешно")
    void createSpacecraft_Success() {
        when(spacecraftRepository.existsByRegistryCode(anyString())).thenReturn(false);
        when(spacecraftTypeRepository.existsById(anyLong())).thenReturn(true);

        Spacecraft savedSpacecraft = Spacecraft.builder()
                .id(1L)
                .registryCode(command.registryCode())
                .name(command.name())
                .spacecraftTypeId(command.spacecraftTypeId())
                .massCapacity(command.massCapacity())
                .volumeCapacity(command.volumeCapacity())
                .status(command.status())
                .currentLocation(command.currentLocation())
                .build();

        when(spacecraftRepository.save(any(Spacecraft.class))).thenReturn(savedSpacecraft);

        Spacecraft result = createSpacecraftService.createSpacecraft(command);

        assertNotNull(result);
        assertEquals("SC-001", result.getRegistryCode());
        assertEquals("Star Carrier", result.getName());

        verify(spacecraftRepository).existsByRegistryCode("SC-001");
        verify(spacecraftTypeRepository).existsById(1L);
        verify(spacecraftRepository).save(any(Spacecraft.class));
    }

    @Test
    @DisplayName("Создание космического корабля - регистрационный код уже существует")
    void createSpacecraft_RegistryCodeAlreadyExists() {
        when(spacecraftRepository.existsByRegistryCode(anyString())).thenReturn(true);

        assertThrows(SpacecraftAlreadyExistsException.class,
                () -> createSpacecraftService.createSpacecraft(command));

        verify(spacecraftRepository).existsByRegistryCode("SC-001");
        verify(spacecraftRepository, never()).save(any());
    }

    @Test
    @DisplayName("Создание космического корабля - тип не найден")
    void createSpacecraft_TypeNotFound() {
        when(spacecraftRepository.existsByRegistryCode(anyString())).thenReturn(false);
        when(spacecraftTypeRepository.existsById(anyLong())).thenReturn(false);

        assertThrows(DataNotFoundException.class,
                () -> createSpacecraftService.createSpacecraft(command));

        verify(spacecraftRepository).existsByRegistryCode("SC-001");
        verify(spacecraftTypeRepository).existsById(1L);
        verify(spacecraftRepository, never()).save(any());
    }

    @Test
    @DisplayName("Создание космического корабля - неверные данные")
    void createSpacecraft_InvalidData() {
        CreateSpacecraftCommand invalidCommand = new CreateSpacecraftCommand(
                "",
                "Star Carrier",
                1L,
                new BigDecimal("50000"),
                new BigDecimal("10000"),
                SpacecraftStatus.DOCKED,
                "Earth Orbit"
        );

        when(spacecraftRepository.existsByRegistryCode(anyString())).thenReturn(false);
        when(spacecraftTypeRepository.existsById(anyLong())).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> createSpacecraftService.createSpacecraft(invalidCommand));

        verify(spacecraftRepository, never()).save(any());
    }
}

