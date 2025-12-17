package org.orbitalLogistic.mission.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.orbitalLogistic.mission.TestcontainersConfiguration;
import org.orbitalLogistic.mission.entities.Mission;
import org.orbitalLogistic.mission.entities.SpacecraftMission;
import org.orbitalLogistic.mission.entities.enums.MissionPriority;
import org.orbitalLogistic.mission.entities.enums.MissionStatus;
import org.orbitalLogistic.mission.entities.enums.MissionType;
import org.orbitalLogistic.mission.repositories.MissionRepository;
import org.orbitalLogistic.mission.repositories.SpacecraftMissionRepository;
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
@Tag("integration-tests")
@TestPropertySource(properties = {
        "spring.cloud.config.enabled=false"
})
class SpacecraftMissionRepositoryTest {

    @Autowired
    private SpacecraftMissionRepository spacecraftMissionRepository;

    @Autowired
    private MissionRepository missionRepository;

    private Mission testMission1;
    private Mission testMission2;
    private SpacecraftMission testSpacecraftMission1;
    private SpacecraftMission testSpacecraftMission2;

    @BeforeEach
    void setUp() {
        spacecraftMissionRepository.deleteAll();
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

        testSpacecraftMission1 = SpacecraftMission.builder()
                .spacecraftId(1L)
                .missionId(testMission1.getId())
                .build();
        testSpacecraftMission1 = spacecraftMissionRepository.save(testSpacecraftMission1);

        testSpacecraftMission2 = SpacecraftMission.builder()
                .spacecraftId(2L)
                .missionId(testMission2.getId())
                .build();
        testSpacecraftMission2 = spacecraftMissionRepository.save(testSpacecraftMission2);
    }

    @Test
    @DisplayName("Поиск по ID - успешно")
    void findById_Success() {
        Optional<SpacecraftMission> result = spacecraftMissionRepository.findById(testSpacecraftMission1.getId());

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getSpacecraftId());
        assertEquals(testMission1.getId(), result.get().getMissionId());
    }

    @Test
    @DisplayName("Поиск по ID - не найдено")
    void findById_NotFound() {
        Optional<SpacecraftMission> result = spacecraftMissionRepository.findById(999L);

        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Поиск по spacecraft ID")
    void findBySpacecraftId() {
        List<SpacecraftMission> result = spacecraftMissionRepository.findBySpacecraftId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testSpacecraftMission1.getId(), result.get(0).getId());
    }

    @Test
    @DisplayName("Поиск по spacecraft ID - пустой результат")
    void findBySpacecraftId_Empty() {
        List<SpacecraftMission> result = spacecraftMissionRepository.findBySpacecraftId(999L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Поиск по mission ID")
    void findByMissionId() {
        List<SpacecraftMission> result = spacecraftMissionRepository.findByMissionId(testMission1.getId());

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testSpacecraftMission1.getId(), result.get(0).getId());
    }

    @Test
    @DisplayName("Поиск по mission ID - пустой результат")
    void findByMissionId_Empty() {
        List<SpacecraftMission> result = spacecraftMissionRepository.findByMissionId(999L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Проверка существования комбинации spacecraft и mission - существует")
    void existsBySpacecraftIdAndMissionId_True() {
        boolean exists = spacecraftMissionRepository.existsBySpacecraftIdAndMissionId(
                1L, testMission1.getId());

        assertTrue(exists);
    }

    @Test
    @DisplayName("Проверка существования комбинации spacecraft и mission - не существует")
    void existsBySpacecraftIdAndMissionId_False() {
        boolean exists = spacecraftMissionRepository.existsBySpacecraftIdAndMissionId(
                999L, testMission1.getId());

        assertFalse(exists);
    }

    @Test
    @DisplayName("Получение всех назначений")
    void findAll_Success() {
        List<SpacecraftMission> result = (List<SpacecraftMission>) spacecraftMissionRepository.findAll();

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Сохранение назначения")
    void save_Success() {
        SpacecraftMission newSpacecraftMission = SpacecraftMission.builder()
                .spacecraftId(3L)
                .missionId(testMission1.getId())
                .build();

        SpacecraftMission saved = spacecraftMissionRepository.save(newSpacecraftMission);

        assertNotNull(saved.getId());
        assertEquals(3L, saved.getSpacecraftId());
        assertEquals(testMission1.getId(), saved.getMissionId());
    }

    @Test
    @DisplayName("Удаление назначения")
    void delete_Success() {
        Long spacecraftMissionId = testSpacecraftMission1.getId();
        assertTrue(spacecraftMissionRepository.existsById(spacecraftMissionId));

        spacecraftMissionRepository.deleteById(spacecraftMissionId);

        assertFalse(spacecraftMissionRepository.existsById(spacecraftMissionId));
    }
}

