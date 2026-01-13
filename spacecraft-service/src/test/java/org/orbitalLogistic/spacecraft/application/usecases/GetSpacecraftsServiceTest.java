package org.orbitalLogistic.spacecraft.application.usecases;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.orbitalLogistic.spacecraft.application.ports.out.SpacecraftRepository;
import org.orbitalLogistic.spacecraft.domain.model.Spacecraft;
import org.orbitalLogistic.spacecraft.domain.model.enums.SpacecraftStatus;
import org.orbitalLogistic.spacecraft.exceptions.SpacecraftNotFoundException;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetSpacecraftsServiceTest {

    @Mock
    private SpacecraftRepository spacecraftRepository;

    @InjectMocks
    private GetSpacecraftsService getSpacecraftsService;

    private Spacecraft spacecraft1;
    private Spacecraft spacecraft2;

    @BeforeEach
    void setUp() {
        spacecraft1 = Spacecraft.builder()
                .id(1L)
                .registryCode("SC-001")
                .name("Star Carrier")
                .spacecraftTypeId(1L)
                .massCapacity(new BigDecimal("50000"))
                .volumeCapacity(new BigDecimal("10000"))
                .status(SpacecraftStatus.DOCKED)
                .currentLocation("Earth Orbit")
                .build();

        spacecraft2 = Spacecraft.builder()
                .id(2L)
                .registryCode("SC-002")
                .name("Galaxy Explorer")
                .spacecraftTypeId(1L)
                .massCapacity(new BigDecimal("30000"))
                .volumeCapacity(new BigDecimal("8000"))
                .status(SpacecraftStatus.IN_TRANSIT)
                .currentLocation("Mars Orbit")
                .build();
    }

    @Test
    @DisplayName("Получение всех кораблей с фильтрами - успешно")
    void getSpacecrafts_WithFilters_Success() {
        List<Spacecraft> spacecrafts = Arrays.asList(spacecraft1);
        when(spacecraftRepository.findWithFilters(anyString(), anyString(), anyInt(), anyInt()))
                .thenReturn(spacecrafts);

        List<Spacecraft> result = getSpacecraftsService.getSpacecrafts("Star", "DOCKED", 10, 0);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("SC-001", result.get(0).getRegistryCode());

        verify(spacecraftRepository).findWithFilters("Star", "DOCKED", 10, 0);
    }

    @Test
    @DisplayName("Получение корабля по ID - успешно")
    void getSpacecraftById_Success() {
        when(spacecraftRepository.findById(1L)).thenReturn(Optional.of(spacecraft1));

        Spacecraft result = getSpacecraftsService.getSpacecraftById(1L);

        assertNotNull(result);
        assertEquals("SC-001", result.getRegistryCode());
        assertEquals("Star Carrier", result.getName());

        verify(spacecraftRepository).findById(1L);
    }

    @Test
    @DisplayName("Получение корабля по ID - не найден")
    void getSpacecraftById_NotFound() {
        when(spacecraftRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(SpacecraftNotFoundException.class,
                () -> getSpacecraftsService.getSpacecraftById(999L));

        verify(spacecraftRepository).findById(999L);
    }

    @Test
    @DisplayName("Получение доступных кораблей - успешно")
    void getAvailableSpacecrafts_Success() {
        List<Spacecraft> availableSpacecrafts = Arrays.asList(spacecraft1);
        when(spacecraftRepository.findAvailableForMission()).thenReturn(availableSpacecrafts);

        List<Spacecraft> result = getSpacecraftsService.getAvailableSpacecrafts();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).isAvailableForMission());

        verify(spacecraftRepository).findAvailableForMission();
    }

    @Test
    @DisplayName("Подсчет кораблей с фильтрами")
    void countSpacecrafts_Success() {
        when(spacecraftRepository.countWithFilters(anyString(), anyString())).thenReturn(5L);

        long result = getSpacecraftsService.countSpacecrafts("Star", "DOCKED");

        assertEquals(5L, result);

        verify(spacecraftRepository).countWithFilters("Star", "DOCKED");
    }

    @Test
    @DisplayName("Проверка существования корабля")
    void spacecraftExists_Success() {
        when(spacecraftRepository.existsById(1L)).thenReturn(true);
        when(spacecraftRepository.existsById(999L)).thenReturn(false);

        assertTrue(getSpacecraftsService.spacecraftExists(1L));
        assertFalse(getSpacecraftsService.spacecraftExists(999L));

        verify(spacecraftRepository).existsById(1L);
        verify(spacecraftRepository).existsById(999L);
    }
}

