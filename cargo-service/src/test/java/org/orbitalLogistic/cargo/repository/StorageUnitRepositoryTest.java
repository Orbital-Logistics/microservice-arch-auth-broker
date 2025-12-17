package org.orbitalLogistic.cargo.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.orbitalLogistic.cargo.CargoServiceApplication;
import org.orbitalLogistic.cargo.TestcontainersConfiguration;
import org.orbitalLogistic.cargo.entities.StorageUnit;
import org.orbitalLogistic.cargo.entities.enums.StorageTypeEnum;
import org.orbitalLogistic.cargo.repositories.StorageUnitRepository;
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
class StorageUnitRepositoryTest {

    @Autowired
    private StorageUnitRepository storageUnitRepository;

    private StorageUnit testUnit;

    @BeforeEach
    void setUp() {
        testUnit = storageUnitRepository.save(StorageUnit.builder()
                .unitCode("TEST-SU-001")
                .location("Test Warehouse")
                .storageType(StorageTypeEnum.AMBIENT)
                .totalMassCapacity(new BigDecimal("10000.00"))
                .totalVolumeCapacity(new BigDecimal("500.00"))
                .currentMass(new BigDecimal("0.00"))
                .currentVolume(new BigDecimal("0.00"))
                .build());
    }

    @Test
    void save_Success() {
        StorageUnit unit = StorageUnit.builder()
                .unitCode("SU-NEW-001")
                .location("New Warehouse")
                .storageType(StorageTypeEnum.PRESSURIZED)
                .totalMassCapacity(new BigDecimal("15000.00"))
                .totalVolumeCapacity(new BigDecimal("600.00"))
                .currentMass(new BigDecimal("0.00"))
                .currentVolume(new BigDecimal("0.00"))
                .build();

        StorageUnit saved = storageUnitRepository.save(unit);

        assertNotNull(saved.getId());
        assertEquals("SU-NEW-001", saved.getUnitCode());
        assertEquals("New Warehouse", saved.getLocation());
    }

    @Test
    void findById_Success() {
        Optional<StorageUnit> found = storageUnitRepository.findById(testUnit.getId());

        assertTrue(found.isPresent());
        assertEquals("TEST-SU-001", found.get().getUnitCode());
    }

    @Test
    void findById_NotFound() {
        Optional<StorageUnit> found = storageUnitRepository.findById(999L);
        assertFalse(found.isPresent());
    }

    @Test
    void findByUnitCode_Success() {
        Optional<StorageUnit> found = storageUnitRepository.findByUnitCode("TEST-SU-001");

        assertTrue(found.isPresent());
        assertEquals("TEST-SU-001", found.get().getUnitCode());
    }

    @Test
    void existsByUnitCode_True() {
        boolean exists = storageUnitRepository.existsByUnitCode("TEST-SU-001");
        assertTrue(exists);
    }

    @Test
    void existsByUnitCode_False() {
        boolean exists = storageUnitRepository.existsByUnitCode("NON-EXISTENT");
        assertFalse(exists);
    }

    @Test
    void findByStorageType_Success() {
        storageUnitRepository.save(StorageUnit.builder()
                .unitCode("SU-AMBIENT-002")
                .location("Warehouse 2")
                .storageType(StorageTypeEnum.AMBIENT)
                .totalMassCapacity(new BigDecimal("8000.00"))
                .totalVolumeCapacity(new BigDecimal("400.00"))
                .currentMass(new BigDecimal("0.00"))
                .currentVolume(new BigDecimal("0.00"))
                .build());

        List<StorageUnit> units = storageUnitRepository.findByStorageType(StorageTypeEnum.AMBIENT);

        assertTrue(units.size() >= 2);
    }

    @Test
    void findAllPaged_Success() {
        storageUnitRepository.save(StorageUnit.builder()
                .unitCode("SU-PAGE-001")
                .location("Warehouse Page")
                .storageType(StorageTypeEnum.AMBIENT)
                .totalMassCapacity(new BigDecimal("5000.00"))
                .totalVolumeCapacity(new BigDecimal("300.00"))
                .currentMass(new BigDecimal("0.00"))
                .currentVolume(new BigDecimal("0.00"))
                .build());

        List<StorageUnit> units = storageUnitRepository.findAllPaged(20, 0);

        assertFalse(units.isEmpty());
    }

    @Test
    void countAll_Success() {
        storageUnitRepository.save(StorageUnit.builder()
                .unitCode("SU-COUNT-001")
                .location("Count Warehouse")
                .storageType(StorageTypeEnum.AMBIENT)
                .totalMassCapacity(new BigDecimal("5000.00"))
                .totalVolumeCapacity(new BigDecimal("300.00"))
                .currentMass(new BigDecimal("0.00"))
                .currentVolume(new BigDecimal("0.00"))
                .build());

        long count = storageUnitRepository.countAll();

        assertTrue(count >= 2);
    }

    @Test
    void findSuitableForCargo_Success() {
        StorageUnit suitableUnit = storageUnitRepository.save(StorageUnit.builder()
                .unitCode("SU-SUITABLE-001")
                .location("Suitable Warehouse")
                .storageType(StorageTypeEnum.AMBIENT)
                .totalMassCapacity(new BigDecimal("10000.00"))
                .totalVolumeCapacity(new BigDecimal("500.00"))
                .currentMass(new BigDecimal("1000.00"))
                .currentVolume(new BigDecimal("50.00"))
                .build());

        List<StorageUnit> units = storageUnitRepository.findSuitableForCargo(500.0, 50.0);

        assertFalse(units.isEmpty());
        assertTrue(units.stream().anyMatch(u -> u.getId().equals(suitableUnit.getId())));
    }

    @Test
    void update_Success() {
        StorageUnit unit = storageUnitRepository.findById(testUnit.getId()).orElseThrow();
        unit.setLocation("Updated Warehouse");
        unit.setCurrentMass(new BigDecimal("1000.00"));

        StorageUnit updated = storageUnitRepository.save(unit);

        assertEquals("Updated Warehouse", updated.getLocation());
        assertEquals(new BigDecimal("1000.00"), updated.getCurrentMass());
    }

    @Test
    void deleteById_Success() {
        StorageUnit unit = storageUnitRepository.save(StorageUnit.builder()
                .unitCode("SU-DELETE-001")
                .location("Delete Warehouse")
                .storageType(StorageTypeEnum.AMBIENT)
                .totalMassCapacity(new BigDecimal("5000.00"))
                .totalVolumeCapacity(new BigDecimal("300.00"))
                .currentMass(new BigDecimal("0.00"))
                .currentVolume(new BigDecimal("0.00"))
                .build());

        storageUnitRepository.deleteById(unit.getId());

        assertFalse(storageUnitRepository.existsById(unit.getId()));
    }
}

