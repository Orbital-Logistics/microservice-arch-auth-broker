package org.orbitalLogistic.mission.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.orbitalLogistic.mission.TestcontainersConfiguration;
import org.orbitalLogistic.mission.entities.Mission;
import org.orbitalLogistic.mission.entities.MissionAssignment;
import org.orbitalLogistic.mission.entities.enums.*;
import org.orbitalLogistic.mission.repositories.MissionAssignmentRepository;
import org.orbitalLogistic.mission.repositories.MissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.cloud.config.enabled=false"
})
class MissionAssignmentRepositoryTest {

    @Autowired
    private MissionAssignmentRepository missionAssignmentRepository;

    @Autowired
    private MissionRepository missionRepository;

    private Mission testMission1;
    private Mission testMission2;
    private MissionAssignment testAssignment1;
    private MissionAssignment testAssignment2;

    @BeforeEach
    void setUp() {
        missionAssignmentRepository.deleteAll();
        missionRepository.deleteAll();

        testMission1 = Mission.builder()
                .missionCode("MISS-001")
                .missionName("Test Mission 1")
                .missionType(MissionType.CARGO_TRANSPORT)
                .status(MissionStatus.PLANNING)
                .priority(MissionPriority.MEDIUM)
                .commandingOfficerId(1L)
                .spacecraftId(1L)
                .scheduledDeparture(LocalDateTime.now().plusDays(1))
                .scheduledArrival(LocalDateTime.now().plusDays(10))
                .build();
        testMission1 = missionRepository.save(testMission1);

        testMission2 = Mission.builder()
                .missionCode("MISS-002")
                .missionName("Test Mission 2")
                .missionType(MissionType.PERSONNEL_TRANSPORT)
                .status(MissionStatus.SCHEDULED)
                .priority(MissionPriority.HIGH)
                .commandingOfficerId(2L)
                .spacecraftId(2L)
                .scheduledDeparture(LocalDateTime.now().plusDays(2))
                .scheduledArrival(LocalDateTime.now().plusDays(12))
                .build();
        testMission2 = missionRepository.save(testMission2);

        testAssignment1 = MissionAssignment.builder()
                .missionId(testMission1.getId())
                .userId(1L)
                .assignedAt(LocalDateTime.now())
                .assignmentRole(AssignmentRole.PILOT)
                .responsibilityZone("Navigation Systems")
                .build();
        testAssignment1 = missionAssignmentRepository.save(testAssignment1);

        testAssignment2 = MissionAssignment.builder()
                .missionId(testMission2.getId())
                .userId(2L)
                .assignedAt(LocalDateTime.now())
                .assignmentRole(AssignmentRole.ENGINEER)
                .responsibilityZone("Propulsion Systems")
                .build();
        testAssignment2 = missionAssignmentRepository.save(testAssignment2);
    }

    @Test
    @DisplayName("Поиск назначения по ID - успешно")
    void findById_Success() {
        Optional<MissionAssignment> result = missionAssignmentRepository.findById(testAssignment1.getId());

        assertTrue(result.isPresent());
        assertEquals(testMission1.getId(), result.get().getMissionId());
        assertEquals(1L, result.get().getUserId());
        assertEquals(AssignmentRole.PILOT, result.get().getAssignmentRole());
    }

    @Test
    @DisplayName("Поиск назначения по ID - не найдено")
    void findById_NotFound() {
        Optional<MissionAssignment> result = missionAssignmentRepository.findById(999L);

        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Поиск назначений по миссии")
    void findByMissionId() {
        List<MissionAssignment> result = missionAssignmentRepository.findByMissionId(testMission1.getId());

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testAssignment1.getId(), result.get(0).getId());
    }

    @Test
    @DisplayName("Поиск назначений по миссии - пустой результат")
    void findByMissionId_Empty() {
        List<MissionAssignment> result = missionAssignmentRepository.findByMissionId(999L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Поиск назначений по пользователю")
    void findByUserId() {
        List<MissionAssignment> result = missionAssignmentRepository.findByUserId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testAssignment1.getId(), result.get(0).getId());
    }

    @Test
    @DisplayName("Поиск назначений по пользователю - пустой результат")
    void findByUserId_Empty() {
        List<MissionAssignment> result = missionAssignmentRepository.findByUserId(999L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Подсчет назначений по миссии")
    void countByMissionId() {
        int count = missionAssignmentRepository.countByMissionId(testMission1.getId());

        assertEquals(1, count);
    }

    @Test
    @DisplayName("Подсчет назначений по миссии - ноль")
    void countByMissionId_Zero() {
        int count = missionAssignmentRepository.countByMissionId(999L);

        assertEquals(0, count);
    }

    @Test
    @DisplayName("Поиск по миссии и пользователю")
    void findByMissionIdAndUserId() {
        List<MissionAssignment> result = missionAssignmentRepository.findByMissionIdAndUserId(
                testMission1.getId(), 1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testAssignment1.getId(), result.get(0).getId());
    }

    @Test
    @DisplayName("Поиск по миссии и пользователю - пустой результат")
    void findByMissionIdAndUserId_Empty() {
        List<MissionAssignment> result = missionAssignmentRepository.findByMissionIdAndUserId(
                testMission1.getId(), 999L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Поиск с фильтрами - по миссии")
    void findWithFilters_ByMissionId() {
        List<MissionAssignment> result = missionAssignmentRepository.findWithFilters(
                testMission1.getId(), null, 10, 0);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testAssignment1.getId(), result.get(0).getId());
    }

    @Test
    @DisplayName("Поиск с фильтрами - по пользователю")
    void findWithFilters_ByUserId() {
        List<MissionAssignment> result = missionAssignmentRepository.findWithFilters(
                null, 2L, 10, 0);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testAssignment2.getId(), result.get(0).getId());
    }

    @Test
    @DisplayName("Поиск с фильтрами - все назначения")
    void findWithFilters_All() {
        List<MissionAssignment> result = missionAssignmentRepository.findWithFilters(
                null, null, 10, 0);

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Поиск с фильтрами - с пагинацией")
    void findWithFilters_WithPagination() {
        List<MissionAssignment> page1 = missionAssignmentRepository.findWithFilters(
                null, null, 1, 0);
        List<MissionAssignment> page2 = missionAssignmentRepository.findWithFilters(
                null, null, 1, 1);

        assertNotNull(page1);
        assertNotNull(page2);
        assertEquals(1, page1.size());
        assertEquals(1, page2.size());
        assertNotEquals(page1.get(0).getId(), page2.get(0).getId());
    }

    @Test
    @DisplayName("Подсчет с фильтрами - по миссии")
    void countWithFilters_ByMissionId() {
        long count = missionAssignmentRepository.countWithFilters(testMission1.getId(), null);

        assertEquals(1, count);
    }

    @Test
    @DisplayName("Подсчет с фильтрами - по пользователю")
    void countWithFilters_ByUserId() {
        long count = missionAssignmentRepository.countWithFilters(null, 2L);

        assertEquals(1, count);
    }

    @Test
    @DisplayName("Подсчет с фильтрами - все назначения")
    void countWithFilters_All() {
        long count = missionAssignmentRepository.countWithFilters(null, null);

        assertEquals(2, count);
    }

    @Test
    @DisplayName("Сохранение назначения")
    void save_Success() {
        MissionAssignment newAssignment = MissionAssignment.builder()
                .missionId(testMission1.getId())
                .userId(3L)
                .assignedAt(LocalDateTime.now())
                .assignmentRole(AssignmentRole.SCIENTIST)
                .responsibilityZone("Research")
                .build();

        MissionAssignment saved = missionAssignmentRepository.save(newAssignment);

        assertNotNull(saved.getId());
        assertEquals(testMission1.getId(), saved.getMissionId());
        assertEquals(3L, saved.getUserId());
        assertEquals(AssignmentRole.SCIENTIST, saved.getAssignmentRole());
    }

    @Test
    @DisplayName("Удаление назначения")
    void delete_Success() {
        Long assignmentId = testAssignment1.getId();
        assertTrue(missionAssignmentRepository.existsById(assignmentId));

        missionAssignmentRepository.deleteById(assignmentId);

        assertFalse(missionAssignmentRepository.existsById(assignmentId));
    }
}

