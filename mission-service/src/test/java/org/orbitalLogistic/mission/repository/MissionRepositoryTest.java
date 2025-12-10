package org.orbitalLogistic.mission.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.orbitalLogistic.mission.TestcontainersConfiguration;
import org.orbitalLogistic.mission.entities.Mission;
import org.orbitalLogistic.mission.entities.enums.MissionPriority;
import org.orbitalLogistic.mission.entities.enums.MissionStatus;
import org.orbitalLogistic.mission.entities.enums.MissionType;
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
class MissionRepositoryTest {

    @Autowired
    private MissionRepository missionRepository;

    private Mission testMission1;
    private Mission testMission2;

    @BeforeEach
    void setUp() {
        missionRepository.deleteAll();

        testMission1 = Mission.builder()
                .missionCode("MISS-001")
                .missionName("Cargo Transport Alpha")
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
                .missionName("Personnel Transport Beta")
                .missionType(MissionType.PERSONNEL_TRANSPORT)
                .status(MissionStatus.SCHEDULED)
                .priority(MissionPriority.HIGH)
                .commandingOfficerId(2L)
                .spacecraftId(2L)
                .scheduledDeparture(LocalDateTime.now().plusDays(2))
                .scheduledArrival(LocalDateTime.now().plusDays(12))
                .build();
        testMission2 = missionRepository.save(testMission2);
    }

    @Test
    @DisplayName("Поиск миссии по ID - успешно")
    void findById_Success() {
        Optional<Mission> result = missionRepository.findById(testMission1.getId());

        assertTrue(result.isPresent());
        assertEquals("MISS-001", result.get().getMissionCode());
        assertEquals("Cargo Transport Alpha", result.get().getMissionName());
        assertEquals(MissionType.CARGO_TRANSPORT, result.get().getMissionType());
    }

    @Test
    @DisplayName("Поиск миссии по ID - не найдена")
    void findById_NotFound() {
        Optional<Mission> result = missionRepository.findById(999L);

        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Поиск миссии по коду - успешно")
    void findByMissionCode_Success() {
        Optional<Mission> result = missionRepository.findByMissionCode("MISS-001");

        assertTrue(result.isPresent());
        assertEquals(testMission1.getId(), result.get().getId());
        assertEquals(MissionType.CARGO_TRANSPORT, result.get().getMissionType());
    }

    @Test
    @DisplayName("Поиск миссии по коду - не найдена")
    void findByMissionCode_NotFound() {
        Optional<Mission> result = missionRepository.findByMissionCode("MISS-999");

        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Проверка существования миссии по коду - существует")
    void existsByMissionCode_True() {
        boolean exists = missionRepository.existsByMissionCode("MISS-001");

        assertTrue(exists);
    }

    @Test
    @DisplayName("Проверка существования миссии по коду - не существует")
    void existsByMissionCode_False() {
        boolean exists = missionRepository.existsByMissionCode("MISS-999");

        assertFalse(exists);
    }

    @Test
    @DisplayName("Поиск миссий по статусу")
    void findByStatus_Success() {
        List<Mission> result = missionRepository.findByStatus(MissionStatus.PLANNING);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("MISS-001", result.get(0).getMissionCode());
    }

    @Test
    @DisplayName("Поиск миссий по статусу - пустой результат")
    void findByStatus_Empty() {
        List<Mission> result = missionRepository.findByStatus(MissionStatus.COMPLETED);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Поиск миссий по командиру")
    void findByCommandingOfficerId() {
        List<Mission> result = missionRepository.findByCommandingOfficerId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("MISS-001", result.get(0).getMissionCode());
    }

    @Test
    @DisplayName("Поиск миссий по командиру - пустой результат")
    void findByCommandingOfficerId_Empty() {
        List<Mission> result = missionRepository.findByCommandingOfficerId(999L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Поиск миссий по кораблю")
    void findBySpacecraftId() {
        List<Mission> result = missionRepository.findBySpacecraftId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("MISS-001", result.get(0).getMissionCode());
    }

    @Test
    @DisplayName("Поиск миссий по кораблю - пустой результат")
    void findBySpacecraftId_Empty() {
        List<Mission> result = missionRepository.findBySpacecraftId(999L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Поиск с фильтрами - по коду миссии")
    void findWithFilters_ByMissionCode() {
        List<Mission> result = missionRepository.findWithFilters("MISS-001", null, null, 10, 0);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("MISS-001", result.get(0).getMissionCode());
    }

    @Test
    @DisplayName("Поиск с фильтрами - по статусу")
    void findWithFilters_ByStatus() {
        List<Mission> result = missionRepository.findWithFilters(null, "SCHEDULED", null, 10, 0);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("MISS-002", result.get(0).getMissionCode());
    }

    @Test
    @DisplayName("Поиск с фильтрами - по типу миссии")
    void findWithFilters_ByMissionType() {
        List<Mission> result = missionRepository.findWithFilters(null, null, "PERSONNEL_TRANSPORT", 10, 0);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("MISS-002", result.get(0).getMissionCode());
    }

    @Test
    @DisplayName("Поиск с фильтрами - все миссии")
    void findWithFilters_All() {
        List<Mission> result = missionRepository.findWithFilters(null, null, null, 10, 0);

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Поиск с фильтрами - с пагинацией")
    void findWithFilters_WithPagination() {
        List<Mission> page1 = missionRepository.findWithFilters(null, null, null, 1, 0);
        List<Mission> page2 = missionRepository.findWithFilters(null, null, null, 1, 1);

        assertNotNull(page1);
        assertNotNull(page2);
        assertEquals(1, page1.size());
        assertEquals(1, page2.size());
        assertNotEquals(page1.get(0).getId(), page2.get(0).getId());
    }

    @Test
    @DisplayName("Подсчет с фильтрами - по коду миссии")
    void countWithFilters_ByMissionCode() {
        long count = missionRepository.countWithFilters("MISS-001", null, null);

        assertEquals(1, count);
    }

    @Test
    @DisplayName("Подсчет с фильтрами - по статусу")
    void countWithFilters_ByStatus() {
        long count = missionRepository.countWithFilters(null, "PLANNING", null);

        assertEquals(1, count);
    }

    @Test
    @DisplayName("Подсчет с фильтрами - по типу миссии")
    void countWithFilters_ByMissionType() {
        long count = missionRepository.countWithFilters(null, null, "CARGO_TRANSPORT");

        assertEquals(1, count);
    }

    @Test
    @DisplayName("Подсчет с фильтрами - все миссии")
    void countWithFilters_All() {
        long count = missionRepository.countWithFilters(null, null, null);

        assertEquals(2, count);
    }

    @Test
    @DisplayName("Получение всех миссий")
    void findAll_Success() {
        List<Mission> result = (List<Mission>) missionRepository.findAll();

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Сохранение миссии")
    void save_Success() {
        Mission newMission = Mission.builder()
                .missionCode("MISS-003")
                .missionName("Science Expedition Gamma")
                .missionType(MissionType.SCIENCE_EXPEDITION)
                .status(MissionStatus.PLANNING)
                .priority(MissionPriority.CRITICAL)
                .commandingOfficerId(3L)
                .spacecraftId(3L)
                .scheduledDeparture(LocalDateTime.now().plusDays(3))
                .scheduledArrival(LocalDateTime.now().plusDays(30))
                .build();

        Mission saved = missionRepository.save(newMission);

        assertNotNull(saved.getId());
        assertEquals("MISS-003", saved.getMissionCode());
        assertTrue(missionRepository.existsByMissionCode("MISS-003"));
    }

    @Test
    @DisplayName("Удаление миссии")
    void delete_Success() {
        Long missionId = testMission1.getId();
        assertTrue(missionRepository.existsById(missionId));

        missionRepository.deleteById(missionId);

        assertFalse(missionRepository.existsById(missionId));
    }
}

