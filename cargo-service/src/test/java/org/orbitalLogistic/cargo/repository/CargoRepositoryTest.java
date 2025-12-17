package org.orbitalLogistic.cargo.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.orbitalLogistic.cargo.CargoServiceApplication;
import org.orbitalLogistic.cargo.TestcontainersConfiguration;
import org.orbitalLogistic.cargo.entities.Cargo;
import org.orbitalLogistic.cargo.entities.CargoCategory;
import org.orbitalLogistic.cargo.entities.enums.CargoType;
import org.orbitalLogistic.cargo.entities.enums.HazardLevel;
import org.orbitalLogistic.cargo.repositories.CargoCategoryRepository;
import org.orbitalLogistic.cargo.repositories.CargoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJdbcTest
@Import(TestcontainersConfiguration.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = CargoServiceApplication.class)
@ActiveProfiles("test")
@Tag("integration-tests")
@TestPropertySource(properties = {
        "spring.cloud.config.enabled=false"
})
class CargoRepositoryTest {

    @Autowired
    private CargoRepository cargoRepository;

    @Autowired
    private CargoCategoryRepository cargoCategoryRepository;

    private CargoCategory testCategory;

    @BeforeEach
    void setUp() {

        testCategory = cargoCategoryRepository.save(CargoCategory.builder()
                .name("Electronics")
                .description("Electronic components")
                .build());
    }

    @Test
    void save_Success() {
        Cargo cargo = Cargo.builder()
                .name("Microchips")
                .cargoCategoryId(testCategory.getId())
                .massPerUnit(new BigDecimal("0.50"))
                .volumePerUnit(new BigDecimal("0.01"))
                .cargoType(CargoType.EQUIPMENT)
                .hazardLevel(HazardLevel.NONE)
                .build();

        Cargo saved = cargoRepository.save(cargo);

        assertNotNull(saved.getId());
        assertEquals("Microchips", saved.getName());
        assertEquals(testCategory.getId(), saved.getCargoCategoryId());
    }

    @Test
    void findById_Success() {
        Cargo cargo = cargoRepository.save(Cargo.builder()
                .name("Microchips")
                .cargoCategoryId(testCategory.getId())
                .massPerUnit(new BigDecimal("0.50"))
                .volumePerUnit(new BigDecimal("0.01"))
                .cargoType(CargoType.EQUIPMENT)
                .hazardLevel(HazardLevel.NONE)
                .build());

        Optional<Cargo> found = cargoRepository.findById(cargo.getId());

        assertTrue(found.isPresent());
        assertEquals("Microchips", found.get().getName());
    }

    @Test
    void findById_NotFound() {
        Optional<Cargo> found = cargoRepository.findById(999L);
        assertFalse(found.isPresent());
    }

    @Test
    void findByName_Success() {
        cargoRepository.save(Cargo.builder()
                .name("Microchips")
                .cargoCategoryId(testCategory.getId())
                .massPerUnit(new BigDecimal("0.50"))
                .volumePerUnit(new BigDecimal("0.01"))
                .cargoType(CargoType.EQUIPMENT)
                .hazardLevel(HazardLevel.NONE)
                .build());

        Optional<Cargo> found = cargoRepository.findByName("Microchips");

        assertTrue(found.isPresent());
        assertEquals("Microchips", found.get().getName());
    }

    @Test
    void existsByName_True() {
        cargoRepository.save(Cargo.builder()
                .name("Microchips")
                .cargoCategoryId(testCategory.getId())
                .massPerUnit(new BigDecimal("0.50"))
                .volumePerUnit(new BigDecimal("0.01"))
                .cargoType(CargoType.EQUIPMENT)
                .hazardLevel(HazardLevel.NONE)
                .build());

        boolean exists = cargoRepository.existsByName("Microchips");
        assertTrue(exists);
    }

    @Test
    void existsByName_False() {
        boolean exists = cargoRepository.existsByName("NonExistent");
        assertFalse(exists);
    }

    @Test
    void findByCargoCategoryId_Success() {
        cargoRepository.save(Cargo.builder()
                .name("Microchips")
                .cargoCategoryId(testCategory.getId())
                .massPerUnit(new BigDecimal("0.50"))
                .volumePerUnit(new BigDecimal("0.01"))
                .cargoType(CargoType.EQUIPMENT)
                .hazardLevel(HazardLevel.NONE)
                .build());

        cargoRepository.save(Cargo.builder()
                .name("Processors")
                .cargoCategoryId(testCategory.getId())
                .massPerUnit(new BigDecimal("0.60"))
                .volumePerUnit(new BigDecimal("0.02"))
                .cargoType(CargoType.EQUIPMENT)
                .hazardLevel(HazardLevel.NONE)
                .build());

        List<Cargo> cargos = cargoRepository.findByCargoCategoryId(testCategory.getId());

        assertEquals(2, cargos.size());
    }

    @Test
    void findWithFilters_All() {
        Cargo savedCargo = cargoRepository.save(Cargo.builder()
                .name("Microchips")
                .cargoCategoryId(testCategory.getId())
                .massPerUnit(new BigDecimal("0.50"))
                .volumePerUnit(new BigDecimal("0.01"))
                .cargoType(CargoType.EQUIPMENT)
                .hazardLevel(HazardLevel.NONE)
                .build());

        List<Cargo> cargos = cargoRepository.findWithFilters(null, null, null, 20, 0);

        assertFalse(cargos.isEmpty());
        assertTrue(cargos.stream().anyMatch(c -> c.getId().equals(savedCargo.getId())));
    }

    @Test
    void findWithFilters_ByName() {
        cargoRepository.save(Cargo.builder()
                .name("Microchips")
                .cargoCategoryId(testCategory.getId())
                .massPerUnit(new BigDecimal("0.50"))
                .volumePerUnit(new BigDecimal("0.01"))
                .cargoType(CargoType.EQUIPMENT)
                .hazardLevel(HazardLevel.NONE)
                .build());

        cargoRepository.save(Cargo.builder()
                .name("Processors")
                .cargoCategoryId(testCategory.getId())
                .massPerUnit(new BigDecimal("0.60"))
                .volumePerUnit(new BigDecimal("0.02"))
                .cargoType(CargoType.EQUIPMENT)
                .hazardLevel(HazardLevel.NONE)
                .build());

        List<Cargo> cargos = cargoRepository.findWithFilters("Micro", null, null, 20, 0);

        assertEquals(1, cargos.size());
        assertEquals("Microchips", cargos.getFirst().getName());
    }

    @Test
    void countWithFilters_All() {
        cargoRepository.save(Cargo.builder()
                .name("Microchips")
                .cargoCategoryId(testCategory.getId())
                .massPerUnit(new BigDecimal("0.50"))
                .volumePerUnit(new BigDecimal("0.01"))
                .cargoType(CargoType.EQUIPMENT)
                .hazardLevel(HazardLevel.NONE)
                .build());

        cargoRepository.save(Cargo.builder()
                .name("Processors")
                .cargoCategoryId(testCategory.getId())
                .massPerUnit(new BigDecimal("0.60"))
                .volumePerUnit(new BigDecimal("0.02"))
                .cargoType(CargoType.EQUIPMENT)
                .hazardLevel(HazardLevel.NONE)
                .build());

        long count = cargoRepository.countWithFilters(null, null, null);

        assertTrue(count >= 2L);
    }

    @Test
    void deleteById_Success() {
        Cargo cargo = cargoRepository.save(Cargo.builder()
                .name("Microchips")
                .cargoCategoryId(testCategory.getId())
                .massPerUnit(new BigDecimal("0.50"))
                .volumePerUnit(new BigDecimal("0.01"))
                .cargoType(CargoType.EQUIPMENT)
                .hazardLevel(HazardLevel.NONE)
                .build());

        cargoRepository.deleteById(cargo.getId());

        assertFalse(cargoRepository.existsById(cargo.getId()));
    }

    @Test
    void update_Success() {
        Cargo cargo = cargoRepository.save(Cargo.builder()
                .name("Microchips")
                .cargoCategoryId(testCategory.getId())
                .massPerUnit(new BigDecimal("0.50"))
                .volumePerUnit(new BigDecimal("0.01"))
                .cargoType(CargoType.EQUIPMENT)
                .hazardLevel(HazardLevel.NONE)
                .build());

        cargo.setName("Updated Microchips");
        cargo.setMassPerUnit(new BigDecimal("0.60"));
        Cargo updated = cargoRepository.save(cargo);

        assertEquals("Updated Microchips", updated.getName());
        assertEquals(new BigDecimal("0.60"), updated.getMassPerUnit());
    }
}

