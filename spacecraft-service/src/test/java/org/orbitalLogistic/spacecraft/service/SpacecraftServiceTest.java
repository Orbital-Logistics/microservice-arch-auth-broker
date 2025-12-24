package org.orbitalLogistic.spacecraft.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.orbitalLogistic.spacecraft.dto.common.PageResponseDTO;
import org.orbitalLogistic.spacecraft.dto.request.SpacecraftRequestDTO;
import org.orbitalLogistic.spacecraft.dto.response.SpacecraftResponseDTO;
import org.orbitalLogistic.spacecraft.entities.Spacecraft;
import org.orbitalLogistic.spacecraft.entities.SpacecraftType;
import org.orbitalLogistic.spacecraft.entities.enums.SpacecraftClassification;
import org.orbitalLogistic.spacecraft.entities.enums.SpacecraftStatus;
import org.orbitalLogistic.spacecraft.exceptions.SpacecraftAlreadyExistsException;
import org.orbitalLogistic.spacecraft.exceptions.SpacecraftNotFoundException;
import org.orbitalLogistic.spacecraft.mappers.SpacecraftMapper;
import org.orbitalLogistic.spacecraft.repositories.SpacecraftRepository;
import org.orbitalLogistic.spacecraft.services.SpacecraftService;
import org.orbitalLogistic.spacecraft.services.SpacecraftTypeService;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpacecraftServiceTest {

    @Mock
    private SpacecraftRepository spacecraftRepository;

    @Mock
    private SpacecraftMapper spacecraftMapper;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private SpacecraftTypeService spacecraftTypeService;

    @InjectMocks
    private SpacecraftService spacecraftService;

    private Spacecraft testSpacecraft;
    private SpacecraftType testSpacecraftType;
    private SpacecraftRequestDTO spacecraftRequest;
    private SpacecraftResponseDTO spacecraftResponse;

    @BeforeEach
    void setUp() {
        testSpacecraftType = SpacecraftType.builder()
                .id(1L)
                .typeName("Cargo Hauler")
                .classification(SpacecraftClassification.CARGO_HAULER)
                .maxCrewCapacity(10)
                .build();

        testSpacecraft = Spacecraft.builder()
                .id(1L)
                .registryCode("SC-001")
                .name("Star Carrier")
                .spacecraftTypeId(1L)
                .massCapacity(new BigDecimal("10000.00"))
                .volumeCapacity(new BigDecimal("5000.00"))
                .status(SpacecraftStatus.DOCKED)
                .currentLocation("Mars Orbit")
                .build();

        spacecraftRequest = new SpacecraftRequestDTO(
                "SC-002",
                "Nova Transporter",
                1L,
                new BigDecimal("15000.00"),
                new BigDecimal("7500.00"),
                SpacecraftStatus.DOCKED,
                "Earth Orbit"
        );

        spacecraftResponse = new SpacecraftResponseDTO(
                testSpacecraft.getId(),
                testSpacecraft.getRegistryCode(),
                testSpacecraft.getName(),
                testSpacecraftType.getTypeName(),
                testSpacecraftType.getClassification(),
                testSpacecraft.getMassCapacity(),
                testSpacecraft.getVolumeCapacity(),
                testSpacecraft.getStatus(),
                testSpacecraft.getCurrentLocation(),
                BigDecimal.ZERO,
                BigDecimal.ZERO
        );

        spacecraftService.setSpacecraftTypeService(spacecraftTypeService);
    }

    @Test
    @DisplayName("Получение всех кораблей с фильтрами")
    void getSpacecrafts_WithFilters() {
        List<Spacecraft> spacecrafts = List.of(testSpacecraft);
        when(spacecraftRepository.findWithFilters(anyString(), anyString(), anyInt(), anyInt()))
                .thenReturn(spacecrafts);
        when(spacecraftRepository.countWithFilters(anyString(), anyString())).thenReturn(1L);
        when(spacecraftTypeService.getEntityByIdBlocking(anyLong())).thenReturn(testSpacecraftType);
        when(spacecraftMapper.toResponseDTO(any(Spacecraft.class), any(), any(), any(), any()))
                .thenReturn(spacecraftResponse);

        PageResponseDTO<SpacecraftResponseDTO> result = spacecraftService.getSpacecraftsBlocking("Star", "AVAILABLE", 0, 10);

        assertNotNull(result);
        assertEquals(1, result.totalElements());
        assertEquals(1, result.content().size());
        assertEquals(0, result.currentPage());
        assertTrue(result.first());
        assertTrue(result.last());

        verify(spacecraftRepository).findWithFilters("Star", "AVAILABLE", 10, 0);
        verify(spacecraftRepository).countWithFilters("Star", "AVAILABLE");
    }

    @Test
    @DisplayName("Получение корабля по ID - успешно")
    void getSpacecraftById_Success() {
        when(spacecraftRepository.findById(1L)).thenReturn(Optional.of(testSpacecraft));
        when(spacecraftTypeService.getEntityByIdBlocking(anyLong())).thenReturn(testSpacecraftType);
        when(spacecraftMapper.toResponseDTO(any(Spacecraft.class), any(), any(), any(), any()))
                .thenReturn(spacecraftResponse);

        SpacecraftResponseDTO result = spacecraftService.getSpacecraftByIdBlocking(1L);

        assertNotNull(result);
        assertEquals("SC-001", result.registryCode());
        assertEquals("Star Carrier", result.name());

        verify(spacecraftRepository).findById(1L);
    }

    @Test
    @DisplayName("Получение корабля по ID - не найден")
    void getSpacecraftById_NotFound() {
        when(spacecraftRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(SpacecraftNotFoundException.class, () -> spacecraftService.getSpacecraftByIdBlocking(999L));

        verify(spacecraftRepository).findById(999L);
    }

    @Test
    @DisplayName("Создание корабля - успешно")
    void createSpacecraft_Success() {
        when(spacecraftRepository.existsByRegistryCode(anyString())).thenReturn(false);
        when(spacecraftTypeService.getEntityByIdBlocking(1L)).thenReturn(testSpacecraftType);
        when(jdbcTemplate.queryForObject(anyString(), eq(Long.class),
                any(), any(), any(), any(), any(), any(), any())).thenReturn(2L);
        when(spacecraftRepository.findById(2L)).thenReturn(Optional.of(testSpacecraft));
        when(spacecraftMapper.toResponseDTO(any(Spacecraft.class), any(), any(), any(), any()))
                .thenReturn(spacecraftResponse);

        SpacecraftResponseDTO result = spacecraftService.createSpacecraftBlocking(spacecraftRequest);

        assertNotNull(result);

        verify(spacecraftRepository).existsByRegistryCode(spacecraftRequest.registryCode());
        verify(spacecraftTypeService, times(2)).getEntityByIdBlocking(1L);
        verify(jdbcTemplate).queryForObject(anyString(), eq(Long.class),
                any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Создание корабля - регистрационный код уже существует")
    void createSpacecraft_RegistryCodeAlreadyExists() {
        when(spacecraftRepository.existsByRegistryCode(anyString())).thenReturn(true);

        assertThrows(SpacecraftAlreadyExistsException.class,
                () -> spacecraftService.createSpacecraftBlocking(spacecraftRequest));

        verify(spacecraftRepository).existsByRegistryCode(spacecraftRequest.registryCode());
        verify(jdbcTemplate, never()).queryForObject(anyString(), eq(Long.class), any());
    }

    @Test
    @DisplayName("Обновление корабля - успешно")
    void updateSpacecraft_Success() {
        when(spacecraftRepository.findById(1L)).thenReturn(Optional.of(testSpacecraft));
        when(spacecraftTypeService.getEntityByIdBlocking(1L)).thenReturn(testSpacecraftType);
        when(jdbcTemplate.update(anyString(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(1);
        when(spacecraftMapper.toResponseDTO(any(Spacecraft.class), any(), any(), any(), any()))
                .thenReturn(spacecraftResponse);

        SpacecraftResponseDTO result = spacecraftService.updateSpacecraftBlocking(1L, spacecraftRequest);

        assertNotNull(result);

        verify(spacecraftRepository).findById(1L);
        verify(spacecraftTypeService, times(2)).getEntityByIdBlocking(1L);
        verify(jdbcTemplate).update(anyString(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Обновление корабля - не найден")
    void updateSpacecraft_NotFound() {
        when(spacecraftRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(SpacecraftNotFoundException.class,
                () -> spacecraftService.updateSpacecraftBlocking(999L, spacecraftRequest));

        verify(spacecraftRepository).findById(999L);
        verify(jdbcTemplate, never()).update(anyString(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Получение доступных кораблей")
    void getAvailableSpacecrafts() {
        List<Spacecraft> spacecrafts = List.of(testSpacecraft);
        when(spacecraftRepository.findAvailableForMission()).thenReturn(spacecrafts);
        when(spacecraftTypeService.getEntityByIdBlocking(anyLong())).thenReturn(testSpacecraftType);
        when(spacecraftMapper.toResponseDTO(any(Spacecraft.class), any(), any(), any(), any()))
                .thenReturn(spacecraftResponse);

        List<SpacecraftResponseDTO> result = spacecraftService.getAvailableSpacecraftsBlocking();

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(spacecraftRepository).findAvailableForMission();
    }

    @Test
    @DisplayName("Обновление статуса корабля - успешно")
    void updateSpacecraftStatus_Success() {
        when(spacecraftRepository.findById(1L)).thenReturn(Optional.of(testSpacecraft));
        when(spacecraftTypeService.getEntityByIdBlocking(anyLong())).thenReturn(testSpacecraftType);
        when(jdbcTemplate.update(anyString(), anyString(), anyLong())).thenReturn(1);
        when(spacecraftMapper.toResponseDTO(any(Spacecraft.class), any(), any(), any(), any()))
                .thenReturn(spacecraftResponse);

        SpacecraftResponseDTO result = spacecraftService.updateSpacecraftStatusBlocking(1L, SpacecraftStatus.IN_TRANSIT);

        assertNotNull(result);

        verify(spacecraftRepository).findById(1L);
        verify(jdbcTemplate).update(anyString(), anyString(), anyLong());
    }

    @Test
    @DisplayName("Проверка существования корабля - существует")
    void spacecraftExists_True() {
        when(spacecraftRepository.existsById(1L)).thenReturn(true);

        boolean exists = spacecraftService.spacecraftExistsBlocking(1L);

        assertTrue(exists);

        verify(spacecraftRepository).existsById(1L);
    }

    @Test
    @DisplayName("Проверка существования корабля - не существует")
    void spacecraftExists_False() {
        when(spacecraftRepository.existsById(999L)).thenReturn(false);

        boolean exists = spacecraftService.spacecraftExistsBlocking(999L);

        assertFalse(exists);

        verify(spacecraftRepository).existsById(999L);
    }

    @Test
    @DisplayName("Получение кораблей с прокруткой")
    void getSpacecraftsScroll() {
        List<Spacecraft> spacecrafts = List.of(testSpacecraft);
        when(spacecraftRepository.findWithFilters(isNull(), isNull(), eq(11), eq(0)))
                .thenReturn(spacecrafts);
        when(spacecraftTypeService.getEntityByIdBlocking(anyLong())).thenReturn(testSpacecraftType);
        when(spacecraftMapper.toResponseDTO(any(Spacecraft.class), any(), any(), any(), any()))
                .thenReturn(spacecraftResponse);

        List<SpacecraftResponseDTO> result = spacecraftService.getSpacecraftsScrollBlocking(0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(spacecraftRepository).findWithFilters(null, null, 11, 0);
    }
}
