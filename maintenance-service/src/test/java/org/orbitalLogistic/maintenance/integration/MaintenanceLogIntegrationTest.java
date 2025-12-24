package org.orbitalLogistic.maintenance.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.orbitalLogistic.maintenance.clients.feign.SpacecraftServiceFeignClient;
import org.orbitalLogistic.maintenance.clients.feign.UserServiceFeignClient;
import org.orbitalLogistic.maintenance.dto.common.SpacecraftDTO;
import org.orbitalLogistic.maintenance.dto.common.UserDTO;
import org.orbitalLogistic.maintenance.dto.request.MaintenanceLogRequestDTO;
import org.orbitalLogistic.maintenance.entities.enums.MaintenanceStatus;
import org.orbitalLogistic.maintenance.entities.enums.MaintenanceType;
import org.orbitalLogistic.maintenance.repositories.MaintenanceLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockUser;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@Testcontainers
@ActiveProfiles("test")
@Tag("integration-tests")
@TestPropertySource(properties = {
        "spring.cloud.config.enabled=false"
})
@Import(org.orbitalLogistic.maintenance.config.TestSecurityConfig.class)
class MaintenanceLogIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:17-alpine"));

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.r2dbc.url", () -> "r2dbc:postgresql://" + postgres.getHost() + ":" + postgres.getFirstMappedPort() + "/" + postgres.getDatabaseName());
        registry.add("spring.r2dbc.username", postgres::getUsername);
        registry.add("spring.r2dbc.password", postgres::getPassword);

        registry.add("spring.flyway.url", postgres::getJdbcUrl);
        registry.add("spring.flyway.user", postgres::getUsername);
        registry.add("spring.flyway.password", postgres::getPassword);
        registry.add("spring.flyway.locations", () -> "classpath:db/migration");
    }

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private MaintenanceLogRepository maintenanceLogRepository;

    @MockitoBean
    private SpacecraftServiceFeignClient spacecraftServiceClient;

    @MockitoBean
    private UserServiceFeignClient userServiceClient;

    @BeforeEach
    void setUp() {
        maintenanceLogRepository.deleteAll().block();

        webTestClient = webTestClient.mutateWith(mockUser().roles("ADMIN"));

        when(spacecraftServiceClient.spacecraftExists(anyLong())).thenReturn(true);
        when(spacecraftServiceClient.getSpacecraftById(anyLong()))
                .thenReturn(new SpacecraftDTO(1L, "SC-001", "Star Carrier"));
        when(userServiceClient.userExists(anyLong())).thenReturn(true);
        when(userServiceClient.getUserById(1L))
                .thenReturn(new UserDTO(1L, "John Doe", "john@example.com"));
        when(userServiceClient.getUserById(2L))
                .thenReturn(new UserDTO(2L, "Jane Smith", "jane@example.com"));
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
                .expectHeader().valueEquals("X-Total-Count", "1")
                .expectBody()
                .jsonPath("$[0].id").isEqualTo(logId)
                .jsonPath("$[0].status").isEqualTo("COMPLETED");
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
                .expectHeader().valueEquals("X-Total-Count", "2")
                .expectBody()
                .jsonPath("$[0].spacecraftId").isEqualTo(1)
                .jsonPath("$[1].spacecraftId").isEqualTo(1);
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
                .expectHeader().valueEquals("X-Total-Count", "5")
                .expectBody()
                .jsonPath("$.length()").isEqualTo(3);

        webTestClient.get()
                .uri("/maintenance-logs?page=1&size=3")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("X-Total-Count", "5")
                .expectBody()
                .jsonPath("$.length()").isEqualTo(2);
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
        when(spacecraftServiceClient.spacecraftExists(999L)).thenReturn(false);

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
        when(userServiceClient.userExists(999L)).thenReturn(false);

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
                .expectHeader().valueEquals("X-Total-Count", "0")
                .expectBody()
                .jsonPath("$.length()").isEqualTo(0);
    }

    @Test
    void getSpacecraftMaintenance_NoResults() {
        webTestClient.get()
                .uri("/spacecrafts/999/maintenance?page=0&size=20")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("X-Total-Count", "0")
                .expectBody()
                .jsonPath("$.length()").isEqualTo(0);
    }
}
