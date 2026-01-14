package org.orbitalLogistic.mission.application.usecases;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.orbitalLogistic.mission.application.ports.out.MissionAssignmentRepository;
import org.orbitalLogistic.mission.application.ports.out.MissionRepository;
import org.orbitalLogistic.mission.application.ports.out.UserServicePort;
import org.orbitalLogistic.mission.domain.exception.UserServiceNotFound;
import org.orbitalLogistic.mission.domain.model.MissionAssignment;
import org.orbitalLogistic.mission.domain.model.enums.AssignmentRole;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateMissionAssignmentServiceTest {

    @Mock
    private MissionAssignmentRepository missionAssignmentRepository;

    @Mock
    private MissionRepository missionRepository;

    @Mock
    private UserServicePort userServicePort;

    @InjectMocks
    private CreateMissionAssignmentService createMissionAssignmentService;

    private MissionAssignment assignment;

    @BeforeEach
    void setUp() {
        assignment = MissionAssignment.builder()
                .missionId(1L)
                .userId(1L)
                .assignmentRole(AssignmentRole.COMMANDER)
                .responsibilityZone("Command")
                .assignedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void createAssignment_Success() {
        // Given
        when(missionRepository.existsById(1L)).thenReturn(true);
        when(userServicePort.userExists(1L)).thenReturn(true);
        
        MissionAssignment expected = MissionAssignment.builder()
                .id(1L)
                .missionId(1L)
                .userId(1L)
                .assignmentRole(AssignmentRole.COMMANDER)
                .responsibilityZone("Command")
                .assignedAt(assignment.getAssignedAt())
                .build();
        
        when(missionAssignmentRepository.save(any(MissionAssignment.class))).thenReturn(expected);

        // When
        MissionAssignment result = createMissionAssignmentService.createAssignment(assignment);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(AssignmentRole.COMMANDER, result.getAssignmentRole());
        
        verify(missionRepository).existsById(1L);
        verify(userServicePort).userExists(1L);
        verify(missionAssignmentRepository).save(any(MissionAssignment.class));
    }

    @Test
    void createAssignment_ThrowsException_WhenUserNotFound() {
        // Given
        when(missionRepository.existsById(1L)).thenReturn(true);
        when(userServicePort.userExists(1L)).thenReturn(false);

        // When & Then
        assertThrows(UserServiceNotFound.class, 
                () -> createMissionAssignmentService.createAssignment(assignment));
        
        verify(missionRepository).existsById(1L);
        verify(userServicePort).userExists(1L);
        verify(missionAssignmentRepository, never()).save(any());
    }
}
