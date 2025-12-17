package org.orbitalLogistic.mission.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.orbitalLogistic.mission.clients.SpacecraftDTO;
import org.orbitalLogistic.mission.clients.resilient.ResilientSpacecraftService;
import org.orbitalLogistic.mission.dto.request.SpacecraftMissionRequestDTO;
import org.orbitalLogistic.mission.dto.response.SpacecraftMissionResponseDTO;
import org.orbitalLogistic.mission.entities.Mission;
import org.orbitalLogistic.mission.entities.SpacecraftMission;
import org.orbitalLogistic.mission.entities.enums.MissionPriority;
import org.orbitalLogistic.mission.entities.enums.MissionStatus;
import org.orbitalLogistic.mission.entities.enums.MissionType;
import org.orbitalLogistic.mission.exceptions.MissionNotFoundException;
import org.orbitalLogistic.mission.exceptions.MissionSpacecraftExistsException;
import org.orbitalLogistic.mission.mappers.SpacecraftMissionMapper;
import org.orbitalLogistic.mission.repositories.MissionRepository;
import org.orbitalLogistic.mission.repositories.SpacecraftMissionRepository;
import org.orbitalLogistic.mission.services.SpacecraftMissionService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpacecraftMissionServiceTest {

    @Mock
    private SpacecraftMissionRepository spacecraftMissionRepository;

    @Mock
    private MissionRepository missionRepository;

    @Mock
    private SpacecraftMissionMapper spacecraftMissionMapper;

    @Mock
    private ResilientSpacecraftService spacecraftServiceClient;

    @InjectMocks
    private SpacecraftMissionService spacecraftMissionService;

    private SpacecraftMission testSpacecraftMission;
    private Mission testMission;
    private SpacecraftDTO testSpacecraft;
    private SpacecraftMissionRequestDTO spacecraftMissionRequest;
    private SpacecraftMissionResponseDTO spacecraftMissionResponse;

    @BeforeEach
    void setUp() {
        testMission = Mission.builder()
                .id(1L)
                .missionCode("MISS-001")
                .missionName("Test Mission")
                .missionType(MissionType.CARGO_TRANSPORT)
                .status(MissionStatus.PLANNING)
                .priority(MissionPriority.MEDIUM)
                .commandingOfficerId(1L)
                .spacecraftId(1L)
                .scheduledDeparture(LocalDateTime.now().plusDays(1))
                .scheduledArrival(LocalDateTime.now().plusDays(10))
                .build();

        testSpacecraftMission = SpacecraftMission.builder()
                .id(1L)
                .spacecraftId(1L)
                .missionId(1L)
                .build();

        testSpacecraft = new SpacecraftDTO(1L, "SC-001", "Star Carrier");

        spacecraftMissionRequest = new SpacecraftMissionRequestDTO(1L, 1L);

        spacecraftMissionResponse = new SpacecraftMissionResponseDTO(
                1L,
                1L,
                "Star Carrier",
                1L,
                "Test Mission"
        );
    }

    @Test
    @DisplayName("Получение всех назначений кораблей на миссии - успешно")
    void getAllSpacecraftMissions_Success() {
        List<SpacecraftMission> spacecraftMissions = List.of(testSpacecraftMission);
        when(spacecraftMissionRepository.findAll()).thenReturn(spacecraftMissions);
        when(spacecraftServiceClient.getSpacecraftById(anyLong())).thenReturn(testSpacecraft);
        when(missionRepository.findById(anyLong())).thenReturn(Optional.of(testMission));
        when(spacecraftMissionMapper.toResponseDTO(any(SpacecraftMission.class), anyString(), anyString()))
                .thenReturn(spacecraftMissionResponse);

        List<SpacecraftMissionResponseDTO> result = spacecraftMissionService.getAllSpacecraftMissions();

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(spacecraftMissionRepository).findAll();
    }

    @Test
    @DisplayName("Получение назначений по кораблю")
    void getBySpacecraft() {
        List<SpacecraftMission> spacecraftMissions = List.of(testSpacecraftMission);
        when(spacecraftMissionRepository.findBySpacecraftId(1L)).thenReturn(spacecraftMissions);
        when(spacecraftServiceClient.getSpacecraftById(anyLong())).thenReturn(testSpacecraft);
        when(missionRepository.findById(anyLong())).thenReturn(Optional.of(testMission));
        when(spacecraftMissionMapper.toResponseDTO(any(SpacecraftMission.class), anyString(), anyString()))
                .thenReturn(spacecraftMissionResponse);

        List<SpacecraftMissionResponseDTO> result = spacecraftMissionService.getBySpacecraft(1L);

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(spacecraftMissionRepository).findBySpacecraftId(1L);
    }

    @Test
    @DisplayName("Получение назначений по миссии")
    void getByMission() {
        List<SpacecraftMission> spacecraftMissions = List.of(testSpacecraftMission);
        when(spacecraftMissionRepository.findByMissionId(1L)).thenReturn(spacecraftMissions);
        when(spacecraftServiceClient.getSpacecraftById(anyLong())).thenReturn(testSpacecraft);
        when(missionRepository.findById(anyLong())).thenReturn(Optional.of(testMission));
        when(spacecraftMissionMapper.toResponseDTO(any(SpacecraftMission.class), anyString(), anyString()))
                .thenReturn(spacecraftMissionResponse);

        List<SpacecraftMissionResponseDTO> result = spacecraftMissionService.getByMission(1L);

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(spacecraftMissionRepository).findByMissionId(1L);
    }

    @Test
    @DisplayName("Создание назначения корабля на миссию - успешно")
    void createSpacecraftMission_Success() {
        when(missionRepository.existsById(1L)).thenReturn(true);
        when(spacecraftMissionRepository.existsBySpacecraftIdAndMissionId(1L, 1L)).thenReturn(false);
        when(spacecraftMissionMapper.toEntity(any(SpacecraftMissionRequestDTO.class)))
                .thenReturn(testSpacecraftMission);
        when(spacecraftMissionRepository.save(any(SpacecraftMission.class))).thenReturn(testSpacecraftMission);
        when(spacecraftServiceClient.getSpacecraftById(anyLong())).thenReturn(testSpacecraft);
        when(missionRepository.findById(anyLong())).thenReturn(Optional.of(testMission));
        when(spacecraftMissionMapper.toResponseDTO(any(SpacecraftMission.class), anyString(), anyString()))
                .thenReturn(spacecraftMissionResponse);

        SpacecraftMissionResponseDTO result = spacecraftMissionService.createSpacecraftMission(spacecraftMissionRequest);

        assertNotNull(result);

        verify(missionRepository).existsById(1L);
        verify(spacecraftMissionRepository).existsBySpacecraftIdAndMissionId(1L, 1L);
        verify(spacecraftMissionRepository).save(any(SpacecraftMission.class));
    }

    @Test
    @DisplayName("Создание назначения корабля на миссию - миссия не найдена")
    void createSpacecraftMission_MissionNotFound() {
        when(missionRepository.existsById(999L)).thenReturn(false);

        SpacecraftMissionRequestDTO request = new SpacecraftMissionRequestDTO(1L, 999L);

        assertThrows(MissionNotFoundException.class,
                () -> spacecraftMissionService.createSpacecraftMission(request));

        verify(missionRepository).existsById(999L);
        verify(spacecraftMissionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Создание назначения корабля на миссию - комбинация уже существует")
    void createSpacecraftMission_AlreadyExists() {
        when(missionRepository.existsById(1L)).thenReturn(true);
        when(spacecraftMissionRepository.existsBySpacecraftIdAndMissionId(1L, 1L)).thenReturn(true);

        assertThrows(MissionSpacecraftExistsException.class,
                () -> spacecraftMissionService.createSpacecraftMission(spacecraftMissionRequest));

        verify(missionRepository).existsById(1L);
        verify(spacecraftMissionRepository).existsBySpacecraftIdAndMissionId(1L, 1L);
        verify(spacecraftMissionRepository, never()).save(any());
    }
}

