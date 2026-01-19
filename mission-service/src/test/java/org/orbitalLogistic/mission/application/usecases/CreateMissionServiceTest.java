package org.orbitalLogistic.mission.application.usecases;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.orbitalLogistic.mission.application.ports.in.CreateMissionCommand;
import org.orbitalLogistic.mission.application.ports.out.MissionRepository;
import org.orbitalLogistic.mission.application.ports.out.SpacecraftServicePort;
import org.orbitalLogistic.mission.application.ports.out.UserServicePort;
import org.orbitalLogistic.mission.domain.exception.MissionAlreadyExistsException;
import org.orbitalLogistic.mission.domain.exception.SpacecraftServiceNotFound;
import org.orbitalLogistic.mission.domain.exception.UserServiceNotFound;
import org.orbitalLogistic.mission.domain.model.Mission;
import org.orbitalLogistic.mission.domain.model.enums.MissionPriority;
import org.orbitalLogistic.mission.domain.model.enums.MissionStatus;
import org.orbitalLogistic.mission.domain.model.enums.MissionType;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateMissionServiceTest {

    @Mock
    private MissionRepository missionRepository;

    @Mock
    private UserServicePort userServicePort;

    @Mock
    private SpacecraftServicePort spacecraftServicePort;

    @InjectMocks
    private CreateMissionService createMissionService;

    private CreateMissionCommand command;

    @BeforeEach
    void setUp() {
        command = new CreateMissionCommand(
                "MARS-001",
                "Mars Exploration",
                MissionType.CARGO_TRANSPORT,
                MissionPriority.HIGH,
                1L,
                1L,
                LocalDateTime.now().plusDays(30),
                LocalDateTime.now().plusDays(180)
        );
    }

    // @Test
    // void createMission_Success() {
    //     // Given
    //     when(missionRepository.existsByMissionCode("MARS-001")).thenReturn(false);
    //     when(userServicePort.userExists(1L)).thenReturn(true);
    //     when(spacecraftServicePort.spacecraftExists(1L)).thenReturn(true);
        
    //     Mission expectedMission = Mission.builder()
    //             .id(1L)
    //             .missionCode("MARS-001")
    //             .missionName("Mars Exploration")
    //             .missionType(MissionType.CARGO_TRANSPORT)
    //             .status(MissionStatus.PLANNING)
    //             .priority(MissionPriority.HIGH)
    //             .commandingOfficerId(1L)
    //             .spacecraftId(1L)
    //             .scheduledDeparture(command.scheduledDeparture())
    //             .scheduledArrival(command.scheduledArrival())
    //             .build();
        
    //     when(missionRepository.save(any(Mission.class))).thenReturn(expectedMission);

    //     // When
    //     Mission result = createMissionService.createMission(command);

    //     // Then
    //     assertNotNull(result);
    //     assertEquals("MARS-001", result.getMissionCode());
    //     assertEquals("Mars Exploration", result.getMissionName());
    //     assertEquals(MissionStatus.PLANNING, result.getStatus());
        
    //     verify(missionRepository).existsByMissionCode("MARS-001");
    //     verify(userServicePort).userExists(1L);
    //     verify(spacecraftServicePort).spacecraftExists(1L);
    //     verify(missionRepository).save(any(Mission.class));
    // }

    // @Test
    // void createMission_ThrowsException_WhenMissionCodeExists() {
    //     // Given
    //     when(missionRepository.existsByMissionCode("MARS-001")).thenReturn(true);

    //     // When & Then
    //     assertThrows(MissionAlreadyExistsException.class, 
    //             () -> createMissionService.createMission(command));
        
    //     verify(missionRepository).existsByMissionCode("MARS-001");
    //     verify(userServicePort, never()).userExists(any());
    //     verify(spacecraftServicePort, never()).spacecraftExists(any());
    //     verify(missionRepository, never()).save(any());
    // }

    // @Test
    // void createMission_ThrowsException_WhenUserNotFound() {
    //     // Given
    //     when(missionRepository.existsByMissionCode("MARS-001")).thenReturn(false);
    //     when(userServicePort.userExists(1L)).thenReturn(false);

    //     // When & Then
    //     assertThrows(UserServiceNotFound.class, 
    //             () -> createMissionService.createMission(command));
        
    //     verify(missionRepository).existsByMissionCode("MARS-001");
    //     verify(userServicePort).userExists(1L);
    //     verify(spacecraftServicePort, never()).spacecraftExists(any());
    //     verify(missionRepository, never()).save(any());
    // }

    // @Test
    // void createMission_ThrowsException_WhenSpacecraftNotFound() {
    //     // Given
    //     when(missionRepository.existsByMissionCode("MARS-001")).thenReturn(false);
    //     when(userServicePort.userExists(1L)).thenReturn(true);
    //     when(spacecraftServicePort.spacecraftExists(1L)).thenReturn(false);

    //     // When & Then
    //     assertThrows(SpacecraftServiceNotFound.class, 
    //             () -> createMissionService.createMission(command));
        
    //     verify(missionRepository).existsByMissionCode("MARS-001");
    //     verify(userServicePort).userExists(1L);
    //     verify(spacecraftServicePort).spacecraftExists(1L);
    //     verify(missionRepository, never()).save(any());
    // }
}
