package org.orbitalLogistic.spacecraft.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.orbitalLogistic.spacecraft.TestcontainersConfiguration;
import org.orbitalLogistic.spacecraft.entities.SpacecraftType;
import org.orbitalLogistic.spacecraft.entities.enums.SpacecraftClassification;
import org.orbitalLogistic.spacecraft.repositories.SpacecraftTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
@Tag("integration-tests")
class SpacecraftTypeRepositoryTest {

    @Autowired
    private SpacecraftTypeRepository spacecraftTypeRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long testTypeId1;
    private Long testTypeId2;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM spacecraft");
        jdbcTemplate.execute("DELETE FROM spacecraft_type");

        SpacecraftType type1 = SpacecraftType.builder()
                .typeName("Cargo Hauler")
                .classification(SpacecraftClassification.CARGO_HAULER)
                .maxCrewCapacity(10)
                .build();
        SpacecraftType savedType1 = spacecraftTypeRepository.save(type1);
        testTypeId1 = savedType1.getId();

        SpacecraftType type2 = SpacecraftType.builder()
                .typeName("Personnel Transport")
                .classification(SpacecraftClassification.PERSONNEL_TRANSPORT)
                .maxCrewCapacity(50)
                .build();
        SpacecraftType savedType2 = spacecraftTypeRepository.save(type2);
        testTypeId2 = savedType2.getId();
    }

    @Test
    @DisplayName("Поиск типа корабля по ID - успешно")
    void findById_Success() {
        Optional<SpacecraftType> result = spacecraftTypeRepository.findById(testTypeId1);

        assertTrue(result.isPresent());
        assertEquals("Cargo Hauler", result.get().getTypeName());
        assertEquals(SpacecraftClassification.CARGO_HAULER, result.get().getClassification());
        assertEquals(10, result.get().getMaxCrewCapacity());
    }

    @Test
    @DisplayName("Поиск типа корабля по ID - не найден")
    void findById_NotFound() {
        Optional<SpacecraftType> result = spacecraftTypeRepository.findById(999L);

        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Поиск типа корабля по имени - успешно")
    void findByTypeName_Success() {
        Optional<SpacecraftType> result = spacecraftTypeRepository.findByTypeName("Cargo Hauler");

        assertTrue(result.isPresent());
        assertEquals(testTypeId1, result.get().getId());
        assertEquals(SpacecraftClassification.CARGO_HAULER, result.get().getClassification());
    }

    @Test
    @DisplayName("Поиск типа корабля по имени - не найден")
    void findByTypeName_NotFound() {
        Optional<SpacecraftType> result = spacecraftTypeRepository.findByTypeName("Nonexistent Type");

        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Проверка существования типа корабля по имени - существует")
    void existsByTypeName_True() {
        boolean exists = spacecraftTypeRepository.existsByTypeName("Cargo Hauler");

        assertTrue(exists);
    }

    @Test
    @DisplayName("Проверка существования типа корабля по имени - не существует")
    void existsByTypeName_False() {
        boolean exists = spacecraftTypeRepository.existsByTypeName("Nonexistent Type");

        assertFalse(exists);
    }

    @Test
    @DisplayName("Поиск типов кораблей по классификации")
    void findByClassification_Success() {
        List<SpacecraftType> result = spacecraftTypeRepository.findByClassification(SpacecraftClassification.CARGO_HAULER);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Cargo Hauler", result.get(0).getTypeName());
    }

    @Test
    @DisplayName("Поиск типов кораблей по классификации - пустой результат")
    void findByClassification_Empty() {
        List<SpacecraftType> result = spacecraftTypeRepository.findByClassification(SpacecraftClassification.SCIENCE_VESSEL);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Получение всех типов кораблей")
    void findAll_Success() {
        List<SpacecraftType> result = (List<SpacecraftType>) spacecraftTypeRepository.findAll();

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Поиск с фильтрами - по имени")
    void findWithFilters_ByTypeName() {
        List<SpacecraftType> result = spacecraftTypeRepository.findWithFilters("Cargo", null, 10, 0);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Cargo Hauler", result.get(0).getTypeName());
    }

    @Test
    @DisplayName("Поиск с фильтрами - по классификации")
    void findWithFilters_ByClassification() {
        List<SpacecraftType> result = spacecraftTypeRepository.findWithFilters(
                null, "PERSONNEL_TRANSPORT", 10, 0
        );

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Personnel Transport", result.get(0).getTypeName());
    }

    @Test
    @DisplayName("Поиск с фильтрами - все типы")
    void findWithFilters_All() {
        List<SpacecraftType> result = spacecraftTypeRepository.findWithFilters(null, null, 10, 0);

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Поиск с фильтрами - с пагинацией")
    void findWithFilters_WithPagination() {
        List<SpacecraftType> page1 = spacecraftTypeRepository.findWithFilters(null, null, 1, 0);
        List<SpacecraftType> page2 = spacecraftTypeRepository.findWithFilters(null, null, 1, 1);

        assertNotNull(page1);
        assertNotNull(page2);
        assertEquals(1, page1.size());
        assertEquals(1, page2.size());
        assertNotEquals(page1.get(0).getId(), page2.get(0).getId());
    }

    @Test
    @DisplayName("Подсчет с фильтрами - по имени")
    void countWithFilters_ByTypeName() {
        long count = spacecraftTypeRepository.countWithFilters("Cargo", null);

        assertEquals(1, count);
    }

    @Test
    @DisplayName("Подсчет с фильтрами - по классификации")
    void countWithFilters_ByClassification() {
        long count = spacecraftTypeRepository.countWithFilters(null, "PERSONNEL_TRANSPORT");

        assertEquals(1, count);
    }

    @Test
    @DisplayName("Подсчет с фильтрами - все типы")
    void countWithFilters_All() {
        long count = spacecraftTypeRepository.countWithFilters(null, null);

        assertEquals(2, count);
    }

    @Test
    @DisplayName("Поиск по минимальной вместимости экипажа")
    void findByMinCrewCapacity() {
        List<SpacecraftType> result = spacecraftTypeRepository.findByMinCrewCapacity(20);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Personnel Transport", result.get(0).getTypeName());
        assertTrue(result.get(0).getMaxCrewCapacity() >= 20);
    }

    @Test
    @DisplayName("Поиск по минимальной вместимости экипажа - все типы")
    void findByMinCrewCapacity_All() {
        List<SpacecraftType> result = spacecraftTypeRepository.findByMinCrewCapacity(5);

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Поиск по минимальной вместимости экипажа - пустой результат")
    void findByMinCrewCapacity_Empty() {
        List<SpacecraftType> result = spacecraftTypeRepository.findByMinCrewCapacity(100);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}

