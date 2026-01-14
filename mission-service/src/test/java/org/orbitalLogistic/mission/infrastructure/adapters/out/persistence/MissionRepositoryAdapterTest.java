package org.orbitalLogistic.mission.infrastructure.adapters.out.persistence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.orbitalLogistic.mission.domain.model.Mission;
import org.orbitalLogistic.mission.domain.model.enums.MissionPriority;
import org.orbitalLogistic.mission.domain.model.enums.MissionStatus;
import org.orbitalLogistic.mission.domain.model.enums.MissionType;
import org.orbitalLogistic.mission.infrastructure.adapters.out.persistence.entity.MissionJpaEntity;
import org.orbitalLogistic.mission.infrastructure.adapters.out.persistence.mapper.MissionPersistenceMapper;
import org.orbitalLogistic.mission.infrastructure.adapters.out.persistence.repository.MissionJdbcRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MissionRepositoryAdapterTest {

    @Mock
    private MissionJdbcRepository jdbcRepository;

    @Mock
    private MissionPersistenceMapper mapper;

    @InjectMocks
    private MissionRepositoryAdapter adapter;

    private Mission mission;
    private MissionJpaEntity entity;

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
                .scheduledDeparture(LocalDateTime.now())
                .scheduledArrival(LocalDateTime.now().plusDays(180))
                .build();

        entity = new MissionJpaEntity();
        entity.setId(1L);
        entity.setMissionCode("MARS-001");
        entity.setMissionName("Mars Exploration");
        entity.setMissionType(MissionType.CARGO_TRANSPORT);
        entity.setStatus(MissionStatus.PLANNING);
        entity.setPriority(MissionPriority.HIGH);
        entity.setCommandingOfficerId(1L);
        entity.setSpacecraftId(1L);
    }

    @Test
    void save_Success() {
        // Given
        when(mapper.toEntity(mission)).thenReturn(entity);
        when(jdbcRepository.save(entity)).thenReturn(entity);
        when(mapper.toDomain(entity)).thenReturn(mission);

        // When
        Mission result = adapter.save(mission);

        // Then
        assertNotNull(result);
        assertEquals("MARS-001", result.getMissionCode());
        verify(mapper).toEntity(mission);
        verify(jdbcRepository).save(entity);
        verify(mapper).toDomain(entity);
    }

    @Test
    void findById_Success() {
        // Given
        when(jdbcRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(mission);

        // When
        Optional<Mission> result = adapter.findById(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals("MARS-001", result.get().getMissionCode());
        verify(jdbcRepository).findById(1L);
        verify(mapper).toDomain(entity);
    }

    @Test
    void findById_ReturnsEmpty_WhenNotFound() {
        // Given
        when(jdbcRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<Mission> result = adapter.findById(999L);

        // Then
        assertFalse(result.isPresent());
        verify(jdbcRepository).findById(999L);
        verify(mapper, never()).toDomain(any());
    }

    @Test
    void findAll_Success() {
        // Given
        when(jdbcRepository.findAll()).thenReturn(List.of(entity));
        when(mapper.toDomain(entity)).thenReturn(mission);

        // When
        List<Mission> result = adapter.findAll();

        // Then
        assertEquals(1, result.size());
        assertEquals("MARS-001", result.get(0).getMissionCode());
        verify(jdbcRepository).findAll();
        verify(mapper).toDomain(entity);
    }

    @Test
    void existsById_ReturnsTrue() {
        // Given
        when(jdbcRepository.existsById(1L)).thenReturn(true);

        // When
        boolean result = adapter.existsById(1L);

        // Then
        assertTrue(result);
        verify(jdbcRepository).existsById(1L);
    }

    @Test
    void deleteById_Success() {
        // When
        adapter.deleteById(1L);

        // Then
        verify(jdbcRepository).deleteById(1L);
    }

    @Test
    void existsByMissionCode_ReturnsTrue() {
        // Given
        when(jdbcRepository.existsByMissionCode("MARS-001")).thenReturn(true);

        // When
        boolean result = adapter.existsByMissionCode("MARS-001");

        // Then
        assertTrue(result);
        verify(jdbcRepository).existsByMissionCode("MARS-001");
    }
}
