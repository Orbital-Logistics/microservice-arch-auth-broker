package org.orbitalLogistic.maintenance.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.orbitalLogistic.maintenance.TestcontainersConfiguration;
import org.orbitalLogistic.maintenance.clients.spacecraft.SpacecraftServiceClient;
import org.orbitalLogistic.maintenance.clients.user.UserServiceClient;
import org.orbitalLogistic.maintenance.dto.common.SpacecraftDTO;
import org.orbitalLogistic.maintenance.dto.common.UserDTO;
import org.orbitalLogistic.maintenance.dto.request.MaintenanceLogRequestDTO;
import org.orbitalLogistic.maintenance.entities.enums.MaintenanceStatus;
import org.orbitalLogistic.maintenance.entities.enums.MaintenanceType;
import org.orbitalLogistic.maintenance.repositories.MaintenanceLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.cloud.config.enabled=false"
})
class MaintenanceLogIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private MaintenanceLogRepository maintenanceLogRepository;

    @MockitoBean
    private SpacecraftServiceClient spacecraftServiceClient;

    @MockitoBean
    private UserServiceClient userServiceClient;

    @BeforeEach
    void setUp() {
        maintenanceLogRepository.deleteAll().block();

        when(spacecraftServiceClient.spacecraftExists(anyLong())).thenReturn(Mono.just(true));
        when(spacecraftServiceClient.getSpacecraftById(anyLong()))
                .thenReturn(Mono.just(new SpacecraftDTO(1L, "SC-001", "Star Carrier")));
        when(userServiceClient.userExists(anyLong())).thenReturn(Mono.just(true));
        when(userServiceClient.getUserById(1L))
                .thenReturn(Mono.just(new UserDTO(1L, "John Doe", "john@example.com")));
        when(userServiceClient.getUserById(2L))
                .thenReturn(Mono.just(new UserDTO(2L, "Jane Smith", "jane@example.com")));
    }

    @Test
    void maintenanceLogLifecycle_Integration() {
        LocalDateTime startTime = LocalDateTime.now().minusDays(1);
        LocalDateTime endTime = LocalDateTime.now();

        MaintenanceLogRequestDTO createRequest = new MaintenanceLogRequestDTO(
                1L,
                MaintenanceType.ROUTINE,
                1L,
                2L,
                startTime,
                null,
                MaintenanceStatus.SCHEDULED,
                "Initial maintenance",
                new BigDecimal("1500.00")
        );

        webTestClient.post()
                .uri("/maintenance-logs")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.spacecraftId").isEqualTo(1)
                .jsonPath("$.spacecraftName").isEqualTo("Star Carrier")
                .jsonPath("$.maintenanceType").isEqualTo("ROUTINE")
                .jsonPath("$.performedByUserId").isEqualTo(1)
                .jsonPath("$.performedByUserName").isEqualTo("John Doe")
                .jsonPath("$.supervisedByUserId").isEqualTo(2)
                .jsonPath("$.supervisedByUserName").isEqualTo("Jane Smith")
                .jsonPath("$.status").isEqualTo("SCHEDULED")
                .jsonPath("$.description").isEqualTo("Initial maintenance")
                .jsonPath("$.cost").isEqualTo(1500.00);

        Long logId = Objects.requireNonNull(maintenanceLogRepository.findAll()
                        .blockFirst())
                .getId();

        MaintenanceLogRequestDTO updateRequest = new MaintenanceLogRequestDTO(
                null, null, null, null, null,
                endTime, MaintenanceStatus.COMPLETED,
                "Maintenance completed successfully", new BigDecimal("2000.00")
        );

        webTestClient.put()
                .uri("/maintenance-logs/" + logId + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(logId)
                .jsonPath("$.status").isEqualTo("COMPLETED")
                .jsonPath("$.description").isEqualTo("Maintenance completed successfully")
                .jsonPath("$.cost").isEqualTo(2000.00);

        webTestClient.get()
                .uri("/maintenance-logs?page=0&size=20")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content.length()").isEqualTo(1)
                .jsonPath("$.content[0].id").isEqualTo(logId)
                .jsonPath("$.content[0].status").isEqualTo("COMPLETED");
    }

    @Test
    void createMultipleLogs_AndRetrieveBySpacecraft() {
        LocalDateTime now = LocalDateTime.now();

        MaintenanceLogRequestDTO request1 = new MaintenanceLogRequestDTO(
                1L, MaintenanceType.ROUTINE, 1L, null,
                now.minusDays(2), now.minusDays(1),
                MaintenanceStatus.COMPLETED,
                "First maintenance", new BigDecimal("1000.00")
        );

        MaintenanceLogRequestDTO request2 = new MaintenanceLogRequestDTO(
                1L, MaintenanceType.REPAIR, 1L, 2L,
                now.minusDays(1), null,
                MaintenanceStatus.IN_PROGRESS,
                "Second maintenance", new BigDecimal("2000.00")
        );

        webTestClient.post()
                .uri("/maintenance-logs")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request1)
                .exchange()
                .expectStatus().isCreated();

        webTestClient.post()
                .uri("/maintenance-logs")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request2)
                .exchange()
                .expectStatus().isCreated();

        webTestClient.get()
                .uri("/spacecrafts/1/maintenance?page=0&size=20")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content.length()").isEqualTo(2)
                .jsonPath("$.totalElements").isEqualTo(2)
                .jsonPath("$.content[0].spacecraftId").isEqualTo(1)
                .jsonPath("$.content[1].spacecraftId").isEqualTo(1);
    }

    @Test
    void getAllLogs_Pagination() {
        LocalDateTime now = LocalDateTime.now();

        for (int i = 0; i < 5; i++) {
            MaintenanceLogRequestDTO request = new MaintenanceLogRequestDTO(
                    1L, MaintenanceType.ROUTINE, 1L, null,
                    now.minusDays(i), null,
                    MaintenanceStatus.SCHEDULED,
                    "Maintenance " + i, new BigDecimal("1000.00")
            );

            webTestClient.post()
                    .uri("/maintenance-logs")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .exchange()
                    .expectStatus().isCreated();
        }

        webTestClient.get()
                .uri("/maintenance-logs?page=0&size=3")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content.length()").isEqualTo(3)
                .jsonPath("$.totalElements").isEqualTo(5)
                .jsonPath("$.totalPages").isEqualTo(2)
                .jsonPath("$.currentPage").isEqualTo(0)
                .jsonPath("$.pageSize").isEqualTo(3)
                .jsonPath("$.first").isEqualTo(true)
                .jsonPath("$.last").isEqualTo(false);

        webTestClient.get()
                .uri("/maintenance-logs?page=1&size=3")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content.length()").isEqualTo(2)
                .jsonPath("$.totalElements").isEqualTo(5)
                .jsonPath("$.currentPage").isEqualTo(1)
                .jsonPath("$.first").isEqualTo(false)
                .jsonPath("$.last").isEqualTo(true);
    }

    @Test
    void createLog_WithoutSupervisor() {
        MaintenanceLogRequestDTO request = new MaintenanceLogRequestDTO(
                1L, MaintenanceType.INSPECTION, 1L, null,
                LocalDateTime.now(), null,
                MaintenanceStatus.SCHEDULED,
                "No supervisor needed", new BigDecimal("500.00")
        );

        webTestClient.post()
                .uri("/maintenance-logs")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.supervisedByUserId").isEmpty()
                .jsonPath("$.supervisedByUserName").isEqualTo("");
    }

    @Test
    void createLog_InvalidSpacecraft() {
        when(spacecraftServiceClient.spacecraftExists(999L)).thenReturn(Mono.just(false));

        MaintenanceLogRequestDTO request = new MaintenanceLogRequestDTO(
                999L, MaintenanceType.ROUTINE, 1L, null,
                LocalDateTime.now(), null,
                MaintenanceStatus.SCHEDULED,
                "Test", new BigDecimal("1000.00")
        );

        webTestClient.post()
                .uri("/maintenance-logs")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void createLog_InvalidUser() {
        when(userServiceClient.userExists(999L)).thenReturn(Mono.just(false));

        MaintenanceLogRequestDTO request = new MaintenanceLogRequestDTO(
                1L, MaintenanceType.ROUTINE, 999L, null,
                LocalDateTime.now(), null,
                MaintenanceStatus.SCHEDULED,
                "Test", new BigDecimal("1000.00")
        );

        webTestClient.post()
                .uri("/maintenance-logs")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void updateLog_NotFound() {
        MaintenanceLogRequestDTO updateRequest = new MaintenanceLogRequestDTO(
                null, null, null, null, null,
                LocalDateTime.now(), MaintenanceStatus.COMPLETED,
                "Updated", new BigDecimal("2000.00")
        );

        webTestClient.put()
                .uri("/maintenance-logs/999/status")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateRequest)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void getAllLogs_EmptyDatabase() {
        webTestClient.get()
                .uri("/maintenance-logs?page=0&size=20")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content.length()").isEqualTo(0)
                .jsonPath("$.totalElements").isEqualTo(0)
                .jsonPath("$.totalPages").isEqualTo(0);
    }

    @Test
    void getSpacecraftMaintenance_NoResults() {
        webTestClient.get()
                .uri("/spacecrafts/999/maintenance?page=0&size=20")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content.length()").isEqualTo(0)
                .jsonPath("$.totalElements").isEqualTo(0);
    }
}

