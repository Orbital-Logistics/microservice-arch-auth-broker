package org.orbitalLogistic.mission.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.orbitalLogistic.mission.clients.UserDTO;
import org.orbitalLogistic.mission.clients.resilient.ResilientUserService;
import org.orbitalLogistic.mission.dto.common.PageResponseDTO;
import org.orbitalLogistic.mission.dto.request.MissionAssignmentRequestDTO;
import org.orbitalLogistic.mission.dto.response.MissionAssignmentResponseDTO;
import org.orbitalLogistic.mission.entities.Mission;
import org.orbitalLogistic.mission.entities.MissionAssignment;
import org.orbitalLogistic.mission.entities.enums.AssignmentRole;
import org.orbitalLogistic.mission.entities.enums.MissionPriority;
import org.orbitalLogistic.mission.entities.enums.MissionStatus;
import org.orbitalLogistic.mission.entities.enums.MissionType;
import org.orbitalLogistic.mission.exceptions.InvalidOperationException;
import org.orbitalLogistic.mission.exceptions.MissionAssignmentNotFoundException;
import org.orbitalLogistic.mission.exceptions.MissionNotFoundException;
import org.orbitalLogistic.mission.mappers.MissionAssignmentMapper;
import org.orbitalLogistic.mission.repositories.MissionAssignmentRepository;
import org.orbitalLogistic.mission.repositories.MissionRepository;
import org.orbitalLogistic.mission.services.MissionAssignmentService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MissionAssignmentServiceTest {

    @Mock
    private MissionAssignmentRepository missionAssignmentRepository;

    @Mock
    private MissionRepository missionRepository;

    @Mock
    private MissionAssignmentMapper missionAssignmentMapper;

    @Mock
    private ResilientUserService userServiceClient;

    @InjectMocks
    private MissionAssignmentService missionAssignmentService;

    private MissionAssignment testAssignment;
    private Mission testMission;
    private MissionAssignmentRequestDTO assignmentRequest;
    private MissionAssignmentResponseDTO assignmentResponse;
    private UserDTO testUser;

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

        testAssignment = MissionAssignment.builder()
                .id(1L)
                .missionId(1L)
                .userId(1L)
                .assignedAt(LocalDateTime.now())
                .assignmentRole(AssignmentRole.PILOT)
                .responsibilityZone("Navigation Systems")
                .build();

        assignmentRequest = new MissionAssignmentRequestDTO(
                1L,
                1L,
                AssignmentRole.PILOT,
                "Navigation Systems"
        );

        assignmentResponse = new MissionAssignmentResponseDTO(
                1L,
                1L,
                "Test Mission",
                1L,
                "John Doe",
                testAssignment.getAssignedAt(),
                AssignmentRole.PILOT,
                "Navigation Systems"
        );

        testUser = new UserDTO(1L, "John Doe", "john@example.com");
    }

    @Test
    @DisplayName("Получение всех назначений - успешно")
    void getAllAssignments_Success() {
        List<MissionAssignment> assignments = List.of(testAssignment);
        when(missionAssignmentRepository.findWithFilters(null, null, 10, 0)).thenReturn(assignments);
        when(missionAssignmentRepository.countWithFilters(null, null)).thenReturn(1L);
        when(missionRepository.findById(anyLong())).thenReturn(Optional.of(testMission));
        when(userServiceClient.getUserById(anyLong())).thenReturn(testUser);
        when(missionAssignmentMapper.toResponseDTO(any(MissionAssignment.class), anyString(), anyString()))
                .thenReturn(assignmentResponse);

        PageResponseDTO<MissionAssignmentResponseDTO> result = missionAssignmentService.getAllAssignments(0, 10);

        assertNotNull(result);
        assertEquals(1, result.totalElements());
        assertEquals(1, result.content().size());

        verify(missionAssignmentRepository).findWithFilters(null, null, 10, 0);
        verify(missionAssignmentRepository).countWithFilters(null, null);
    }

    @Test
    @DisplayName("Получение назначения по ID - успешно")
    void getAssignmentById_Success() {
        when(missionAssignmentRepository.findById(1L)).thenReturn(Optional.of(testAssignment));
        when(missionRepository.findById(anyLong())).thenReturn(Optional.of(testMission));
        when(userServiceClient.getUserById(anyLong())).thenReturn(testUser);
        when(missionAssignmentMapper.toResponseDTO(any(MissionAssignment.class), anyString(), anyString()))
                .thenReturn(assignmentResponse);

        MissionAssignmentResponseDTO result = missionAssignmentService.getAssignmentById(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());

        verify(missionAssignmentRepository).findById(1L);
    }

    @Test
    @DisplayName("Получение назначения по ID - не найдено")
    void getAssignmentById_NotFound() {
        when(missionAssignmentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(MissionAssignmentNotFoundException.class,
                () -> missionAssignmentService.getAssignmentById(999L));

        verify(missionAssignmentRepository).findById(999L);
    }

    @Test
    @DisplayName("Получение назначений по миссии")
    void getAssignmentsByMission() {
        List<MissionAssignment> assignments = List.of(testAssignment);
        when(missionAssignmentRepository.findByMissionId(1L)).thenReturn(assignments);
        when(missionRepository.findById(anyLong())).thenReturn(Optional.of(testMission));
        when(userServiceClient.getUserById(anyLong())).thenReturn(testUser);
        when(missionAssignmentMapper.toResponseDTO(any(MissionAssignment.class), anyString(), anyString()))
                .thenReturn(assignmentResponse);

        List<MissionAssignmentResponseDTO> result = missionAssignmentService.getAssignmentsByMission(1L);

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(missionAssignmentRepository).findByMissionId(1L);
    }

    @Test
    @DisplayName("Получение назначений по пользователю")
    void getAssignmentsByUser() {
        List<MissionAssignment> assignments = List.of(testAssignment);
        when(missionAssignmentRepository.findByUserId(1L)).thenReturn(assignments);
        when(missionRepository.findById(anyLong())).thenReturn(Optional.of(testMission));
        when(userServiceClient.getUserById(anyLong())).thenReturn(testUser);
        when(missionAssignmentMapper.toResponseDTO(any(MissionAssignment.class), anyString(), anyString()))
                .thenReturn(assignmentResponse);

        List<MissionAssignmentResponseDTO> result = missionAssignmentService.getAssignmentsByUser(1L);

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(missionAssignmentRepository).findByUserId(1L);
    }

    @Test
    @DisplayName("Создание назначения - успешно")
    void createAssignment_Success() {
        when(missionRepository.findById(1L)).thenReturn(Optional.of(testMission));
        when(userServiceClient.userExists(1L)).thenReturn(true);
        when(missionAssignmentRepository.findByMissionIdAndUserId(1L, 1L)).thenReturn(List.of());
        when(missionAssignmentMapper.toEntity(any(MissionAssignmentRequestDTO.class))).thenReturn(testAssignment);
        when(missionAssignmentRepository.save(any(MissionAssignment.class))).thenReturn(testAssignment);
        when(userServiceClient.getUserById(anyLong())).thenReturn(testUser);
        when(missionAssignmentMapper.toResponseDTO(any(MissionAssignment.class), anyString(), anyString()))
                .thenReturn(assignmentResponse);

        MissionAssignmentResponseDTO result = missionAssignmentService.createAssignment(assignmentRequest);

        assertNotNull(result);

        verify(missionRepository, times(2)).findById(1L);
        verify(userServiceClient).userExists(1L);
        verify(missionAssignmentRepository).findByMissionIdAndUserId(1L, 1L);
        verify(missionAssignmentRepository).save(any(MissionAssignment.class));
    }

    @Test
    @DisplayName("Создание назначения - миссия не найдена")
    void createAssignment_MissionNotFound() {
        when(missionRepository.findById(999L)).thenReturn(Optional.empty());

        MissionAssignmentRequestDTO request = new MissionAssignmentRequestDTO(
                999L, 1L, AssignmentRole.PILOT, "Navigation"
        );

        assertThrows(MissionNotFoundException.class,
                () -> missionAssignmentService.createAssignment(request));

        verify(missionRepository).findById(999L);
        verify(missionAssignmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Создание назначения - пользователь не найден")
    void createAssignment_UserNotFound() {
        when(missionRepository.findById(1L)).thenReturn(Optional.of(testMission));
        when(userServiceClient.userExists(999L)).thenReturn(false);

        MissionAssignmentRequestDTO request = new MissionAssignmentRequestDTO(
                1L, 999L, AssignmentRole.PILOT, "Navigation"
        );

        assertThrows(InvalidOperationException.class,
                () -> missionAssignmentService.createAssignment(request));

        verify(userServiceClient).userExists(999L);
        verify(missionAssignmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Создание назначения - пользователь уже назначен")
    void createAssignment_UserAlreadyAssigned() {
        when(missionRepository.findById(1L)).thenReturn(Optional.of(testMission));
        when(userServiceClient.userExists(1L)).thenReturn(true);
        when(missionAssignmentRepository.findByMissionIdAndUserId(1L, 1L))
                .thenReturn(List.of(testAssignment));

        assertThrows(InvalidOperationException.class,
                () -> missionAssignmentService.createAssignment(assignmentRequest));

        verify(missionAssignmentRepository).findByMissionIdAndUserId(1L, 1L);
        verify(missionAssignmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Удаление назначения - успешно")
    void deleteAssignment_Success() {
        when(missionAssignmentRepository.existsById(1L)).thenReturn(true);
        doNothing().when(missionAssignmentRepository).deleteById(1L);

        missionAssignmentService.deleteAssignment(1L);

        verify(missionAssignmentRepository).existsById(1L);
        verify(missionAssignmentRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Удаление назначения - не найдено")
    void deleteAssignment_NotFound() {
        when(missionAssignmentRepository.existsById(999L)).thenReturn(false);

        assertThrows(MissionAssignmentNotFoundException.class,
                () -> missionAssignmentService.deleteAssignment(999L));

        verify(missionAssignmentRepository).existsById(999L);
        verify(missionAssignmentRepository, never()).deleteById(anyLong());
    }
}

