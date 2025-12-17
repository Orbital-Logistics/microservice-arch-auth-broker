package org.orbitalLogistic.inventory.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.orbitalLogistic.inventory.TestcontainersConfiguration;
import org.orbitalLogistic.inventory.entities.CargoManifest;
import org.orbitalLogistic.inventory.entities.enums.ManifestPriority;
import org.orbitalLogistic.inventory.entities.enums.ManifestStatus;
import org.orbitalLogistic.inventory.repositories.CargoManifestRepository;
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
class CargoManifestRepositoryTest {

    @Autowired
    private CargoManifestRepository cargoManifestRepository;

    private CargoManifest testManifest1;
    private CargoManifest testManifest2;

    @BeforeEach
    void setUp() {
        cargoManifestRepository.deleteAll();

        LocalDateTime now = LocalDateTime.now();

        testManifest1 = CargoManifest.builder()
                .spacecraftId(1L)
                .cargoId(1L)
                .storageUnitId(1L)
                .quantity(100)
                .loadedAt(now.minusDays(2))
                .loadedByUserId(1L)
                .manifestStatus(ManifestStatus.LOADED)
                .priority(ManifestPriority.HIGH)
                .build();
        testManifest1 = cargoManifestRepository.save(testManifest1);

        testManifest2 = CargoManifest.builder()
                .spacecraftId(2L)
                .cargoId(2L)
                .storageUnitId(2L)
                .quantity(200)
                .loadedAt(now.minusDays(1))
                .loadedByUserId(2L)
                .manifestStatus(ManifestStatus.IN_TRANSIT)
                .priority(ManifestPriority.NORMAL)
                .build();
        testManifest2 = cargoManifestRepository.save(testManifest2);
    }

    @Test
    @DisplayName("Поиск манифеста по ID - успешно")
    void findById_Success() {
        Optional<CargoManifest> result = cargoManifestRepository.findById(testManifest1.getId());

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getSpacecraftId());
        assertEquals(1L, result.get().getCargoId());
        assertEquals(100, result.get().getQuantity());
    }

    @Test
    @DisplayName("Поиск манифеста по ID - не найден")
    void findById_NotFound() {
        Optional<CargoManifest> result = cargoManifestRepository.findById(999L);

        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Поиск манифестов по кораблю")
    void findBySpacecraftId() {
        List<CargoManifest> result = cargoManifestRepository.findBySpacecraftId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testManifest1.getId(), result.get(0).getId());
    }

    @Test
    @DisplayName("Поиск манифестов по кораблю - пустой результат")
    void findBySpacecraftId_Empty() {
        List<CargoManifest> result = cargoManifestRepository.findBySpacecraftId(999L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Поиск манифестов по грузу")
    void findByCargoId() {
        List<CargoManifest> result = cargoManifestRepository.findByCargoId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testManifest1.getId(), result.get(0).getId());
    }

    @Test
    @DisplayName("Поиск манифестов по грузу - пустой результат")
    void findByCargoId_Empty() {
        List<CargoManifest> result = cargoManifestRepository.findByCargoId(999L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Получение всех манифестов с пагинацией")
    void findAllPaginated() {
        List<CargoManifest> result = cargoManifestRepository.findAllPaginated(10, 0);

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Получение всех манифестов с пагинацией - вторая страница")
    void findAllPaginated_SecondPage() {
        List<CargoManifest> page1 = cargoManifestRepository.findAllPaginated(1, 0);
        List<CargoManifest> page2 = cargoManifestRepository.findAllPaginated(1, 1);

        assertNotNull(page1);
        assertNotNull(page2);
        assertEquals(1, page1.size());
        assertEquals(1, page2.size());
        assertNotEquals(page1.get(0).getId(), page2.get(0).getId());
    }

    @Test
    @DisplayName("Подсчет всех манифестов")
    void countAll() {
        long count = cargoManifestRepository.countAll();

        assertEquals(2, count);
    }

    @Test
    @DisplayName("Поиск по кораблю с пагинацией")
    void findBySpacecraftIdPaginated() {
        List<CargoManifest> result = cargoManifestRepository.findBySpacecraftIdPaginated(1L, 10, 0);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testManifest1.getId(), result.get(0).getId());
    }

    @Test
    @DisplayName("Подсчет по кораблю")
    void countBySpacecraftId() {
        long count = cargoManifestRepository.countBySpacecraftId(1L);

        assertEquals(1, count);
    }

    @Test
    @DisplayName("Подсчет по кораблю - ноль")
    void countBySpacecraftId_Zero() {
        long count = cargoManifestRepository.countBySpacecraftId(999L);

        assertEquals(0, count);
    }

    @Test
    @DisplayName("Сохранение манифеста")
    void save_Success() {
        CargoManifest newManifest = CargoManifest.builder()
                .spacecraftId(3L)
                .cargoId(3L)
                .storageUnitId(3L)
                .quantity(300)
                .loadedAt(LocalDateTime.now())
                .loadedByUserId(3L)
                .manifestStatus(ManifestStatus.PENDING)
                .priority(ManifestPriority.CRITICAL)
                .build();

        CargoManifest saved = cargoManifestRepository.save(newManifest);

        assertNotNull(saved.getId());
        assertEquals(3L, saved.getSpacecraftId());
        assertEquals(300, saved.getQuantity());
    }

    @Test
    @DisplayName("Удаление манифеста")
    void delete_Success() {
        Long manifestId = testManifest1.getId();
        assertTrue(cargoManifestRepository.existsById(manifestId));

        cargoManifestRepository.deleteById(manifestId);

        assertFalse(cargoManifestRepository.existsById(manifestId));
    }

    @Test
    @DisplayName("Обновление манифеста")
    void update_Success() {
        testManifest1.setQuantity(150);
        testManifest1.setManifestStatus(ManifestStatus.IN_TRANSIT);

        CargoManifest updated = cargoManifestRepository.save(testManifest1);

        assertEquals(150, updated.getQuantity());
        assertEquals(ManifestStatus.IN_TRANSIT, updated.getManifestStatus());
    }
}

