package org.orbitalLogistic.maintenance.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.orbitalLogistic.maintenance.TestcontainersConfiguration;
import org.orbitalLogistic.maintenance.entities.MaintenanceLog;
import org.orbitalLogistic.maintenance.entities.enums.MaintenanceStatus;
import org.orbitalLogistic.maintenance.entities.enums.MaintenanceType;
import org.orbitalLogistic.maintenance.repositories.MaintenanceLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DataR2dbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
@Tag("integration-tests")
@TestPropertySource(properties = {
        "spring.cloud.config.enabled=false"
})
class MaintenanceLogRepositoryTest {

    @Autowired
    private MaintenanceLogRepository maintenanceLogRepository;

    private MaintenanceLog testLog1;
    private MaintenanceLog testLog2;

    @BeforeEach
    void setUp() {
        maintenanceLogRepository.deleteAll().block();

        testLog1 = MaintenanceLog.builder()
                .spacecraftId(1L)
                .maintenanceType(MaintenanceType.ROUTINE)
                .performedByUserId(1L)
                .supervisedByUserId(2L)
                .startTime(LocalDateTime.now().minusDays(1))
                .endTime(LocalDateTime.now())
                .status(MaintenanceStatus.COMPLETED)
                .description("Routine maintenance check")
                .cost(new BigDecimal("1500.00"))
                .build();

        testLog2 = MaintenanceLog.builder()
                .spacecraftId(1L)
                .maintenanceType(MaintenanceType.REPAIR)
                .performedByUserId(2L)
                .startTime(LocalDateTime.now())
                .status(MaintenanceStatus.IN_PROGRESS)
                .description("Engine repair")
                .cost(new BigDecimal("5000.00"))
                .build();

        testLog1 = maintenanceLogRepository.save(testLog1).block();
        testLog2 = maintenanceLogRepository.save(testLog2).block();
    }

    @Test
    void save_Success() {
        MaintenanceLog newLog = MaintenanceLog.builder()
                .spacecraftId(2L)
                .maintenanceType(MaintenanceType.INSPECTION)
                .performedByUserId(1L)
                .startTime(LocalDateTime.now())
                .status(MaintenanceStatus.SCHEDULED)
                .description("Pre-flight inspection")
                .cost(new BigDecimal("500.00"))
                .build();

        StepVerifier.create(maintenanceLogRepository.save(newLog))
                .assertNext(saved -> {
                    assertNotNull(saved.getId());
                    assertEquals(2L, saved.getSpacecraftId());
                    assertEquals(MaintenanceType.INSPECTION, saved.getMaintenanceType());
                    assertEquals(MaintenanceStatus.SCHEDULED, saved.getStatus());
                })
                .verifyComplete();
    }

    @Test
    void findById_Success() {
        StepVerifier.create(maintenanceLogRepository.findById(testLog1.getId()))
                .assertNext(found -> {
                    assertEquals(testLog1.getId(), found.getId());
                    assertEquals(testLog1.getSpacecraftId(), found.getSpacecraftId());
                    assertEquals(testLog1.getMaintenanceType(), found.getMaintenanceType());
                })
                .verifyComplete();
    }

    @Test
    void findById_NotFound() {
        StepVerifier.create(maintenanceLogRepository.findById(999L))
                .verifyComplete();
    }

    @Test
    void findBySpacecraftId_Success() {
        StepVerifier.create(maintenanceLogRepository.findBySpacecraftId(1L))
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void findAllPaginated_Success() {
        StepVerifier.create(maintenanceLogRepository.findAllPaginated(0, 10))
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void findAllPaginated_WithOffset() {
        StepVerifier.create(maintenanceLogRepository.findAllPaginated(1, 1))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void countAll_Success() {
        StepVerifier.create(maintenanceLogRepository.countAll())
                .expectNext(2L)
                .verifyComplete();
    }

    @Test
    void findBySpacecraftIdPaginated_Success() {
        StepVerifier.create(maintenanceLogRepository.findBySpacecraftIdPaginated(1L, 10, 0))
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void countBySpacecraftId_Success() {
        StepVerifier.create(maintenanceLogRepository.countBySpacecraftId(1L))
                .expectNext(2L)
                .verifyComplete();
    }

    @Test
    void countBySpacecraftId_NoResults() {
        StepVerifier.create(maintenanceLogRepository.countBySpacecraftId(999L))
                .expectNext(0L)
                .verifyComplete();
    }

    @Test
    void update_Success() {
        testLog1.setStatus(MaintenanceStatus.IN_PROGRESS);
        testLog1.setDescription("Updated description");

        StepVerifier.create(maintenanceLogRepository.save(testLog1))
                .assertNext(updated -> {
                    assertEquals(MaintenanceStatus.IN_PROGRESS, updated.getStatus());
                    assertEquals("Updated description", updated.getDescription());
                })
                .verifyComplete();
    }

    @Test
    void delete_Success() {
        StepVerifier.create(
                        maintenanceLogRepository.deleteById(testLog1.getId())
                                .then(maintenanceLogRepository.findById(testLog1.getId()))
                )
                .verifyComplete();
    }

    @Test
    void deleteAll_Success() {
        StepVerifier.create(
                        maintenanceLogRepository.deleteAll()
                                .then(maintenanceLogRepository.countAll())
                )
                .expectNext(0L)
                .verifyComplete();
    }
}

