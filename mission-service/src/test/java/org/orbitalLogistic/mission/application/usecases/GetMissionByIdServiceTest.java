package org.orbitalLogistic.mission.application.usecases;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.orbitalLogistic.mission.application.ports.out.MissionRepository;
import org.orbitalLogistic.mission.domain.model.Mission;
import org.orbitalLogistic.mission.domain.model.enums.MissionPriority;
import org.orbitalLogistic.mission.domain.model.enums.MissionStatus;
import org.orbitalLogistic.mission.domain.model.enums.MissionType;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetMissionByIdServiceTest {

    @Mock
    private MissionRepository missionRepository;

    @InjectMocks
    private GetMissionByIdService getMissionByIdService;

    private Mission mission;

    @BeforeEach
    void setUp() {
        mission = Mission.builder()
                .id(1L)
                .missionCode("MARS-001")
                .missionName("Mars Exploration")
                .missionType(MissionType.CARGO_TRANSPORT)
                .status(MissionStatus.PLANNING)
                .priority(MissionPriority.HIGH)
                .commandingOfficerId(1L)
                .spacecraftId(1L)
                .scheduledDeparture(LocalDateTime.now().plusDays(30))
                .scheduledArrival(LocalDateTime.now().plusDays(180))
                .build();
    }

    @Test
    void getMissionById_Success() {
        // Given
        when(missionRepository.findById(1L)).thenReturn(Optional.of(mission));

        // When
        Optional<Mission> result = getMissionByIdService.getMissionById(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals("MARS-001", result.get().getMissionCode());
        verify(missionRepository).findById(1L);
    }

    @Test
    void getMissionById_ReturnsEmpty_WhenNotFound() {
        // Given
        when(missionRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<Mission> result = getMissionByIdService.getMissionById(999L);

        // Then
        assertFalse(result.isPresent());
        verify(missionRepository).findById(999L);
    }

    @Test
    void existsById_ReturnsTrue_WhenExists() {
        // Given
        when(missionRepository.existsById(1L)).thenReturn(true);

        // When
        boolean result = getMissionByIdService.existsById(1L);

        // Then
        assertTrue(result);
        verify(missionRepository).existsById(1L);
    }

    @Test
    void existsById_ReturnsFalse_WhenNotExists() {
        // Given
        when(missionRepository.existsById(999L)).thenReturn(false);

        // When
        boolean result = getMissionByIdService.existsById(999L);

        // Then
        assertFalse(result);
        verify(missionRepository).existsById(999L);
    }
}
