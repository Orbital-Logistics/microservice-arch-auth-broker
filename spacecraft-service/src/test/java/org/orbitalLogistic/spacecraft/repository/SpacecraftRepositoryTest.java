package org.orbitalLogistic.spacecraft.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.orbitalLogistic.spacecraft.TestcontainersConfiguration;
import org.orbitalLogistic.spacecraft.entities.Spacecraft;
import org.orbitalLogistic.spacecraft.entities.SpacecraftType;
import org.orbitalLogistic.spacecraft.entities.enums.SpacecraftClassification;
import org.orbitalLogistic.spacecraft.entities.enums.SpacecraftStatus;
import org.orbitalLogistic.spacecraft.repositories.SpacecraftRepository;
import org.orbitalLogistic.spacecraft.repositories.SpacecraftTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
@Tag("integration-tests")
class SpacecraftRepositoryTest {

    @Autowired
    private SpacecraftRepository spacecraftRepository;

    @Autowired
    private SpacecraftTypeRepository spacecraftTypeRepository;

    private SpacecraftType testSpacecraftType;

    @BeforeEach
    void setUp() {
        spacecraftRepository.deleteAll();
        spacecraftTypeRepository.deleteAll();

        testSpacecraftType = SpacecraftType.builder()
                .typeName("Cargo Hauler")
                .classification(SpacecraftClassification.CARGO_HAULER)
                .maxCrewCapacity(10)
                .build();
        testSpacecraftType = spacecraftTypeRepository.save(testSpacecraftType);
    }

    @Test
    @DisplayName("Сохранение и поиск корабля по ID")
    void saveAndFindById() {
        Spacecraft spacecraft = Spacecraft.builder()
                .registryCode("SC-001")
                .name("Star Carrier")
                .spacecraftTypeId(testSpacecraftType.getId())
                .massCapacity(new BigDecimal("10000.00"))
                .volumeCapacity(new BigDecimal("5000.00"))
                .status(SpacecraftStatus.DOCKED)
                .currentLocation("Mars Orbit")
                .build();

        Spacecraft saved = spacecraftRepository.save(spacecraft);
        Optional<Spacecraft> found = spacecraftRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getRegistryCode()).isEqualTo("SC-001");
        assertThat(found.get().getName()).isEqualTo("Star Carrier");
        assertThat(found.get().getStatus()).isEqualTo(SpacecraftStatus.DOCKED);
    }

    @Test
    @DisplayName("Поиск корабля по регистрационному коду")
    void findByRegistryCode() {
        Spacecraft spacecraft = Spacecraft.builder()
                .registryCode("SC-TEST-001")
                .name("Test Ship")
                .spacecraftTypeId(testSpacecraftType.getId())
                .massCapacity(new BigDecimal("5000.00"))
                .volumeCapacity(new BigDecimal("2500.00"))
                .status(SpacecraftStatus.DOCKED)
                .currentLocation("Earth Orbit")
                .build();
        spacecraftRepository.save(spacecraft);

        Optional<Spacecraft> found = spacecraftRepository.findByRegistryCode("SC-TEST-001");

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Test Ship");
    }

    @Test
    @DisplayName("Проверка существования по регистрационному коду")
    void existsByRegistryCode() {
        Spacecraft spacecraft = Spacecraft.builder()
                .registryCode("SC-EXISTS")
                .name("Exists Ship")
                .spacecraftTypeId(testSpacecraftType.getId())
                .massCapacity(new BigDecimal("5000.00"))
                .volumeCapacity(new BigDecimal("2500.00"))
                .status(SpacecraftStatus.DOCKED)
                .currentLocation("Moon Orbit")
                .build();
        spacecraftRepository.save(spacecraft);

        assertThat(spacecraftRepository.existsByRegistryCode("SC-EXISTS")).isTrue();
        assertThat(spacecraftRepository.existsByRegistryCode("SC-NOT-EXISTS")).isFalse();
    }

    @Test
    @DisplayName("Поиск кораблей по статусу")
    void findByStatus() {
        Spacecraft spacecraft1 = Spacecraft.builder()
                .registryCode("SC-AVAILABLE-1")
                .name("Available Ship 1")
                .spacecraftTypeId(testSpacecraftType.getId())
                .massCapacity(new BigDecimal("5000.00"))
                .volumeCapacity(new BigDecimal("2500.00"))
                .status(SpacecraftStatus.DOCKED)
                .currentLocation("Earth")
                .build();

        Spacecraft spacecraft2 = Spacecraft.builder()
                .registryCode("SC-IN-MISSION")
                .name("Mission Ship")
                .spacecraftTypeId(testSpacecraftType.getId())
                .massCapacity(new BigDecimal("6000.00"))
                .volumeCapacity(new BigDecimal("3000.00"))
                .status(SpacecraftStatus.IN_TRANSIT)
                .currentLocation("Mars")
                .build();

        spacecraftRepository.save(spacecraft1);
        spacecraftRepository.save(spacecraft2);

        List<Spacecraft> available = spacecraftRepository.findByStatus(SpacecraftStatus.DOCKED);
        List<Spacecraft> inMission = spacecraftRepository.findByStatus(SpacecraftStatus.IN_TRANSIT);

        assertThat(available).hasSize(1);
        assertThat(available.getFirst().getRegistryCode()).isEqualTo("SC-AVAILABLE-1");
        assertThat(inMission).hasSize(1);
        assertThat(inMission.getFirst().getRegistryCode()).isEqualTo("SC-IN-MISSION");
    }

    @Test
    @DisplayName("Поиск с фильтрами - по имени")
    void findWithFilters_ByName() {
        Spacecraft spacecraft1 = Spacecraft.builder()
                .registryCode("SC-001")
                .name("Star Carrier Alpha")
                .spacecraftTypeId(testSpacecraftType.getId())
                .massCapacity(new BigDecimal("10000.00"))
                .volumeCapacity(new BigDecimal("5000.00"))
                .status(SpacecraftStatus.DOCKED)
                .currentLocation("Earth")
                .build();

        Spacecraft spacecraft2 = Spacecraft.builder()
                .registryCode("SC-002")
                .name("Nova Transporter")
                .spacecraftTypeId(testSpacecraftType.getId())
                .massCapacity(new BigDecimal("8000.00"))
                .volumeCapacity(new BigDecimal("4000.00"))
                .status(SpacecraftStatus.DOCKED)
                .currentLocation("Mars")
                .build();

        spacecraftRepository.save(spacecraft1);
        spacecraftRepository.save(spacecraft2);

        List<Spacecraft> foundByName = spacecraftRepository.findWithFilters("Star", null, 10, 0);

        assertThat(foundByName).hasSize(1);
        assertThat(foundByName.getFirst().getName()).contains("Star");
    }

    @Test
    @DisplayName("Поиск с фильтрами - по статусу")
    void findWithFilters_ByStatus() {
        Spacecraft spacecraft1 = Spacecraft.builder()
                .registryCode("SC-AVAIL")
                .name("Available Ship")
                .spacecraftTypeId(testSpacecraftType.getId())
                .massCapacity(new BigDecimal("5000.00"))
                .volumeCapacity(new BigDecimal("2500.00"))
                .status(SpacecraftStatus.DOCKED)
                .currentLocation("Earth")
                .build();

        Spacecraft spacecraft2 = Spacecraft.builder()
                .registryCode("SC-MISSION")
                .name("Mission Ship")
                .spacecraftTypeId(testSpacecraftType.getId())
                .massCapacity(new BigDecimal("6000.00"))
                .volumeCapacity(new BigDecimal("3000.00"))
                .status(SpacecraftStatus.IN_TRANSIT)
                .currentLocation("Mars")
                .build();

        spacecraftRepository.save(spacecraft1);
        spacecraftRepository.save(spacecraft2);

        List<Spacecraft> available = spacecraftRepository.findWithFilters(null, "DOCKED", 10, 0);

        assertThat(available).hasSize(1);
        assertThat(available.getFirst().getStatus()).isEqualTo(SpacecraftStatus.DOCKED);
    }

    @Test
    @DisplayName("Поиск с фильтрами - пагинация")
    void findWithFilters_Pagination() {
        for (int i = 1; i <= 5; i++) {
            Spacecraft spacecraft = Spacecraft.builder()
                    .registryCode("SC-" + String.format("%03d", i))
                    .name("Ship " + i)
                    .spacecraftTypeId(testSpacecraftType.getId())
                    .massCapacity(new BigDecimal("5000.00"))
                    .volumeCapacity(new BigDecimal("2500.00"))
                    .status(SpacecraftStatus.DOCKED)
                    .currentLocation("Earth")
                    .build();
            spacecraftRepository.save(spacecraft);
        }

        List<Spacecraft> page1 = spacecraftRepository.findWithFilters(null, null, 2, 0);
        List<Spacecraft> page2 = spacecraftRepository.findWithFilters(null, null, 2, 2);

        assertThat(page1).hasSize(2);
        assertThat(page2).hasSize(2);
        assertThat(page1.getFirst().getId()).isNotEqualTo(page2.getFirst().getId());
    }

    @Test
    @DisplayName("Подсчет с фильтрами")
    void countWithFilters() {
        Spacecraft spacecraft1 = Spacecraft.builder()
                .registryCode("SC-STAR-1")
                .name("Star Ship Alpha")
                .spacecraftTypeId(testSpacecraftType.getId())
                .massCapacity(new BigDecimal("10000.00"))
                .volumeCapacity(new BigDecimal("5000.00"))
                .status(SpacecraftStatus.DOCKED)
                .currentLocation("Earth")
                .build();

        Spacecraft spacecraft2 = Spacecraft.builder()
                .registryCode("SC-STAR-2")
                .name("Star Ship Beta")
                .spacecraftTypeId(testSpacecraftType.getId())
                .massCapacity(new BigDecimal("8000.00"))
                .volumeCapacity(new BigDecimal("4000.00"))
                .status(SpacecraftStatus.IN_TRANSIT)
                .currentLocation("Mars")
                .build();

        Spacecraft spacecraft3 = Spacecraft.builder()
                .registryCode("SC-NOVA")
                .name("Nova Cruiser")
                .spacecraftTypeId(testSpacecraftType.getId())
                .massCapacity(new BigDecimal("7000.00"))
                .volumeCapacity(new BigDecimal("3500.00"))
                .status(SpacecraftStatus.DOCKED)
                .currentLocation("Moon")
                .build();

        spacecraftRepository.save(spacecraft1);
        spacecraftRepository.save(spacecraft2);
        spacecraftRepository.save(spacecraft3);

        long countByName = spacecraftRepository.countWithFilters("Star", null);
        long countByStatus = spacecraftRepository.countWithFilters(null, "DOCKED");
        long countAll = spacecraftRepository.countWithFilters(null, null);

        assertThat(countByName).isEqualTo(2);
        assertThat(countByStatus).isEqualTo(2);
        assertThat(countAll).isEqualTo(3);
    }

    @Test
    @DisplayName("Обновление корабля")
    void updateSpacecraft() {
        Spacecraft spacecraft = Spacecraft.builder()
                .registryCode("SC-UPDATE")
                .name("Original Name")
                .spacecraftTypeId(testSpacecraftType.getId())
                .massCapacity(new BigDecimal("5000.00"))
                .volumeCapacity(new BigDecimal("2500.00"))
                .status(SpacecraftStatus.DOCKED)
                .currentLocation("Earth")
                .build();
        Spacecraft saved = spacecraftRepository.save(spacecraft);

        saved.setName("Updated Name");
        saved.setStatus(SpacecraftStatus.IN_TRANSIT);
        spacecraftRepository.save(saved);

        Optional<Spacecraft> found = spacecraftRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Updated Name");
        assertThat(found.get().getStatus()).isEqualTo(SpacecraftStatus.IN_TRANSIT);
    }

    @Test
    @DisplayName("Удаление корабля")
    void deleteSpacecraft() {
        Spacecraft spacecraft = Spacecraft.builder()
                .registryCode("SC-DELETE")
                .name("To Delete")
                .spacecraftTypeId(testSpacecraftType.getId())
                .massCapacity(new BigDecimal("5000.00"))
                .volumeCapacity(new BigDecimal("2500.00"))
                .status(SpacecraftStatus.DOCKED)
                .currentLocation("Earth")
                .build();
        Spacecraft saved = spacecraftRepository.save(spacecraft);
        Long id = saved.getId();

        spacecraftRepository.deleteById(id);

        assertThat(spacecraftRepository.findById(id)).isEmpty();
    }

    @Test
    @DisplayName("Проверка существования по ID")
    void existsById() {
        Spacecraft spacecraft = Spacecraft.builder()
                .registryCode("SC-EXISTS-ID")
                .name("Exists Ship")
                .spacecraftTypeId(testSpacecraftType.getId())
                .massCapacity(new BigDecimal("5000.00"))
                .volumeCapacity(new BigDecimal("2500.00"))
                .status(SpacecraftStatus.DOCKED)
                .currentLocation("Earth")
                .build();
        Spacecraft saved = spacecraftRepository.save(spacecraft);

        assertThat(spacecraftRepository.existsById(saved.getId())).isTrue();
        assertThat(spacecraftRepository.existsById(999999L)).isFalse();
    }
}

