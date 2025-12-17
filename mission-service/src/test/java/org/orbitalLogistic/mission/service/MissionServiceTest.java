package org.orbitalLogistic.mission.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.orbitalLogistic.mission.clients.SpacecraftDTO;
import org.orbitalLogistic.mission.clients.UserDTO;
import org.orbitalLogistic.mission.clients.resilient.ResilientSpacecraftService;
import org.orbitalLogistic.mission.clients.resilient.ResilientUserService;
import org.orbitalLogistic.mission.dto.common.PageResponseDTO;
import org.orbitalLogistic.mission.dto.request.MissionRequestDTO;
import org.orbitalLogistic.mission.dto.response.MissionResponseDTO;
import org.orbitalLogistic.mission.entities.Mission;
import org.orbitalLogistic.mission.entities.enums.MissionPriority;
import org.orbitalLogistic.mission.entities.enums.MissionStatus;
import org.orbitalLogistic.mission.entities.enums.MissionType;
import org.orbitalLogistic.mission.exceptions.InvalidOperationException;
import org.orbitalLogistic.mission.exceptions.MissionAlreadyExistsException;
import org.orbitalLogistic.mission.exceptions.MissionNotFoundException;
import org.orbitalLogistic.mission.mappers.MissionMapper;
import org.orbitalLogistic.mission.repositories.MissionAssignmentRepository;
import org.orbitalLogistic.mission.repositories.MissionRepository;
import org.orbitalLogistic.mission.services.MissionService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MissionServiceTest {

    @Mock
    private MissionRepository missionRepository;

    @Mock
    private MissionAssignmentRepository missionAssignmentRepository;

    @Mock
    private MissionMapper missionMapper;

    @Mock
    private ResilientUserService userServiceClient;

    @Mock
    private ResilientSpacecraftService spacecraftServiceClient;

    @InjectMocks
    private MissionService missionService;

    private Mission testMission;
    private MissionRequestDTO missionRequest;
    private MissionResponseDTO missionResponse;
    private UserDTO testUser;
    private SpacecraftDTO testSpacecraft;

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

        missionRequest = new MissionRequestDTO(
                LocalDateTime.now().plusDays(12),
                LocalDateTime.now().plusDays(2),
                2L,
                2L,
                MissionPriority.HIGH,
                MissionType.PERSONNEL_TRANSPORT,
                "New Mission",
                "MISS-002"
        );

        missionResponse = new MissionResponseDTO(
                1L,
                "MISS-001",
                "Test Mission",
                MissionType.CARGO_TRANSPORT,
                MissionStatus.PLANNING,
                MissionPriority.MEDIUM,
                1L,
                "TestCommander",
                1L,
                "TestSpacecraft",
                testMission.getScheduledDeparture(),
                testMission.getScheduledArrival(),
                5
        );

        testUser = new UserDTO(1L, "TestCommander", "test@example.com");
        testSpacecraft = new SpacecraftDTO(1L, "SC-001", "TestSpacecraft");
    }

    @Test
    @DisplayName("Получение всех миссий - успешно")
    void getAllMissions_Success() {
        List<Mission> missions = List.of(testMission);
        when(missionRepository.findWithFilters(null, null, null, 10, 0)).thenReturn(missions);
        when(missionRepository.countWithFilters(null, null, null)).thenReturn(1L);
        when(userServiceClient.getUserById(anyLong())).thenReturn(testUser);
        when(spacecraftServiceClient.getSpacecraftById(anyLong())).thenReturn(testSpacecraft);
        when(missionAssignmentRepository.countByMissionId(anyLong())).thenReturn(5);
        when(missionMapper.toResponseDTO(any(Mission.class), anyString(), anyString(), anyInt()))
                .thenReturn(missionResponse);

        PageResponseDTO<MissionResponseDTO> result = missionService.getAllMissions(0, 10);

        assertNotNull(result);
        assertEquals(1, result.totalElements());
        assertEquals(1, result.content().size());

        verify(missionRepository).findWithFilters(null, null, null, 10, 0);
        verify(missionRepository).countWithFilters(null, null, null);
    }

    @Test
    @DisplayName("Поиск миссий с фильтрами - успешно")
    void searchMissions_WithFilters() {
        List<Mission> missions = List.of(testMission);
        when(missionRepository.findWithFilters("MISS", "PLANNING", "CARGO_TRANSPORT", 10, 0))
                .thenReturn(missions);
        when(missionRepository.countWithFilters("MISS", "PLANNING", "CARGO_TRANSPORT")).thenReturn(1L);
        when(userServiceClient.getUserById(anyLong())).thenReturn(testUser);
        when(spacecraftServiceClient.getSpacecraftById(anyLong())).thenReturn(testSpacecraft);
        when(missionAssignmentRepository.countByMissionId(anyLong())).thenReturn(5);
        when(missionMapper.toResponseDTO(any(Mission.class), anyString(), anyString(), anyInt()))
                .thenReturn(missionResponse);

        PageResponseDTO<MissionResponseDTO> result = missionService.searchMissions(
                "MISS", "PLANNING", "CARGO_TRANSPORT", 0, 10);

        assertNotNull(result);
        assertEquals(1, result.totalElements());

        verify(missionRepository).findWithFilters("MISS", "PLANNING", "CARGO_TRANSPORT", 10, 0);
    }

    @Test
    @DisplayName("Получение миссии по ID - успешно")
    void getMissionById_Success() {
        when(missionRepository.findById(1L)).thenReturn(Optional.of(testMission));
        when(userServiceClient.getUserById(anyLong())).thenReturn(testUser);
        when(spacecraftServiceClient.getSpacecraftById(anyLong())).thenReturn(testSpacecraft);
        when(missionAssignmentRepository.countByMissionId(anyLong())).thenReturn(5);
        when(missionMapper.toResponseDTO(any(Mission.class), anyString(), anyString(), anyInt()))
                .thenReturn(missionResponse);

        MissionResponseDTO result = missionService.getMissionById(1L);

        assertNotNull(result);
        assertEquals("MISS-001", result.missionCode());

        verify(missionRepository).findById(1L);
    }

    @Test
    @DisplayName("Получение миссии по ID - не найдена")
    void getMissionById_NotFound() {
        when(missionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(MissionNotFoundException.class,
                () -> missionService.getMissionById(999L));

        verify(missionRepository).findById(999L);
    }

    @Test
    @DisplayName("Создание миссии - успешно")
    void createMission_Success() {
        when(missionRepository.existsByMissionCode(anyString())).thenReturn(false);
        when(userServiceClient.userExists(anyLong())).thenReturn(true);
        when(spacecraftServiceClient.getSpacecraftById(anyLong())).thenReturn(testSpacecraft);
        when(missionMapper.toEntity(any(MissionRequestDTO.class))).thenReturn(testMission);
        when(missionRepository.save(any(Mission.class))).thenReturn(testMission);
        when(userServiceClient.getUserById(anyLong())).thenReturn(testUser);
        when(missionAssignmentRepository.countByMissionId(anyLong())).thenReturn(0);
        when(missionMapper.toResponseDTO(any(Mission.class), anyString(), anyString(), anyInt()))
                .thenReturn(missionResponse);

        MissionResponseDTO result = missionService.createMission(missionRequest);

        assertNotNull(result);

        verify(missionRepository).existsByMissionCode(missionRequest.missionCode());
        verify(userServiceClient).userExists(missionRequest.commandingOfficerId());
        verify(spacecraftServiceClient).getSpacecraftById(missionRequest.spacecraftId());
        verify(missionRepository).save(any(Mission.class));
    }

    @Test
    @DisplayName("Создание миссии - код уже существует")
    void createMission_CodeAlreadyExists() {
        when(missionRepository.existsByMissionCode(anyString())).thenReturn(true);

        assertThrows(MissionAlreadyExistsException.class,
                () -> missionService.createMission(missionRequest));

        verify(missionRepository).existsByMissionCode(missionRequest.missionCode());
        verify(missionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Создание миссии - командир не найден")
    void createMission_CommanderNotFound() {
        when(missionRepository.existsByMissionCode(anyString())).thenReturn(false);
        when(userServiceClient.userExists(anyLong())).thenReturn(false);

        assertThrows(InvalidOperationException.class,
                () -> missionService.createMission(missionRequest));

        verify(userServiceClient).userExists(missionRequest.commandingOfficerId());
        verify(missionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Создание миссии - корабль не найден")
    void createMission_SpacecraftNotFound() {
        when(missionRepository.existsByMissionCode(anyString())).thenReturn(false);
        when(userServiceClient.userExists(anyLong())).thenReturn(true);
        when(spacecraftServiceClient.getSpacecraftById(anyLong()))
                .thenThrow(new org.orbitalLogistic.mission.exceptions.SpacecraftServiceException("Spacecraft with ID " + missionRequest.spacecraftId() + " not found"));

        assertThrows(org.orbitalLogistic.mission.exceptions.SpacecraftServiceException.class,
                () -> missionService.createMission(missionRequest));

        verify(spacecraftServiceClient).getSpacecraftById(missionRequest.spacecraftId());
        verify(missionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Обновление миссии - успешно")
    void updateMission_Success() {
        when(missionRepository.findById(1L)).thenReturn(Optional.of(testMission));
        when(userServiceClient.userExists(anyLong())).thenReturn(true);
        when(spacecraftServiceClient.spacecraftExists(anyLong())).thenReturn(true);
        when(missionRepository.save(any(Mission.class))).thenReturn(testMission);
        when(userServiceClient.getUserById(anyLong())).thenReturn(testUser);
        when(spacecraftServiceClient.getSpacecraftById(anyLong())).thenReturn(testSpacecraft);
        when(missionAssignmentRepository.countByMissionId(anyLong())).thenReturn(5);
        when(missionMapper.toResponseDTO(any(Mission.class), anyString(), anyString(), anyInt()))
                .thenReturn(missionResponse);

        MissionResponseDTO result = missionService.updateMission(1L, missionRequest);

        assertNotNull(result);

        verify(missionRepository).findById(1L);
        verify(missionRepository).save(any(Mission.class));
    }

    @Test
    @DisplayName("Обновление миссии - не найдена")
    void updateMission_NotFound() {
        when(missionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(MissionNotFoundException.class,
                () -> missionService.updateMission(999L, missionRequest));

        verify(missionRepository).findById(999L);
        verify(missionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Обновление статуса миссии - успешно")
    void updateMissionStatus_Success() {
        when(missionRepository.findById(1L)).thenReturn(Optional.of(testMission));
        when(missionRepository.save(any(Mission.class))).thenReturn(testMission);
        when(userServiceClient.getUserById(anyLong())).thenReturn(testUser);
        when(spacecraftServiceClient.getSpacecraftById(anyLong())).thenReturn(testSpacecraft);
        when(missionAssignmentRepository.countByMissionId(anyLong())).thenReturn(5);
        when(missionMapper.toResponseDTO(any(Mission.class), anyString(), anyString(), anyInt()))
                .thenReturn(missionResponse);

        MissionResponseDTO result = missionService.updateMissionStatus(1L, MissionStatus.IN_PROGRESS);

        assertNotNull(result);

        verify(missionRepository).findById(1L);
        verify(missionRepository).save(any(Mission.class));
    }

    @Test
    @DisplayName("Удаление миссии - успешно")
    void deleteMission_Success() {
        when(missionRepository.existsById(1L)).thenReturn(true);
        doNothing().when(missionRepository).deleteById(1L);

        missionService.deleteMission(1L);

        verify(missionRepository).existsById(1L);
        verify(missionRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Удаление миссии - не найдена")
    void deleteMission_NotFound() {
        when(missionRepository.existsById(999L)).thenReturn(false);

        assertThrows(MissionNotFoundException.class,
                () -> missionService.deleteMission(999L));

        verify(missionRepository).existsById(999L);
        verify(missionRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Получение миссий по командиру")
    void getMissionsByCommander() {
        List<Mission> missions = List.of(testMission);
        when(missionRepository.findByCommandingOfficerId(1L)).thenReturn(missions);
        when(userServiceClient.getUserById(anyLong())).thenReturn(testUser);
        when(spacecraftServiceClient.getSpacecraftById(anyLong())).thenReturn(testSpacecraft);
        when(missionAssignmentRepository.countByMissionId(anyLong())).thenReturn(5);
        when(missionMapper.toResponseDTO(any(Mission.class), anyString(), anyString(), anyInt()))
                .thenReturn(missionResponse);

        List<MissionResponseDTO> result = missionService.getMissionsByCommander(1L);

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(missionRepository).findByCommandingOfficerId(1L);
    }

    @Test
    @DisplayName("Получение миссий по кораблю")
    void getMissionsBySpacecraft() {
        List<Mission> missions = List.of(testMission);
        when(missionRepository.findBySpacecraftId(1L)).thenReturn(missions);
        when(userServiceClient.getUserById(anyLong())).thenReturn(testUser);
        when(spacecraftServiceClient.getSpacecraftById(anyLong())).thenReturn(testSpacecraft);
        when(missionAssignmentRepository.countByMissionId(anyLong())).thenReturn(5);
        when(missionMapper.toResponseDTO(any(Mission.class), anyString(), anyString(), anyInt()))
                .thenReturn(missionResponse);

        List<MissionResponseDTO> result = missionService.getMissionsBySpacecraft(1L);

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(missionRepository).findBySpacecraftId(1L);
    }
}

