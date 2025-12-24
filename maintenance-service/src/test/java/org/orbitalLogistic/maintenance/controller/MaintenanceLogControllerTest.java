package org.orbitalLogistic.maintenance.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.orbitalLogistic.maintenance.controllers.MaintenanceLogController;
import org.orbitalLogistic.maintenance.dto.request.MaintenanceLogRequestDTO;
import org.orbitalLogistic.maintenance.dto.response.MaintenanceLogResponseDTO;
import org.orbitalLogistic.maintenance.entities.enums.MaintenanceStatus;
import org.orbitalLogistic.maintenance.entities.enums.MaintenanceType;
import org.orbitalLogistic.maintenance.exceptions.InvalidOperationException;
import org.orbitalLogistic.maintenance.exceptions.MaintenanceLogNotFoundException;
import org.orbitalLogistic.maintenance.services.JwtService;
import org.orbitalLogistic.maintenance.services.MaintenanceLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.context.annotation.Import;

import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockUser;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@WebFluxTest(MaintenanceLogController.class)
@TestPropertySource(properties = {
        "spring.cloud.config.enabled=false"
})
@Import(org.orbitalLogistic.maintenance.config.TestSecurityConfig.class)
class MaintenanceLogControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private MaintenanceLogService maintenanceLogService;

    @MockitoBean
    private JwtService jwtService;

    private MaintenanceLogResponseDTO responseDTO;
    private MaintenanceLogRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();

        webTestClient = webTestClient.mutateWith(mockUser().roles("ADMIN"));

        responseDTO = new MaintenanceLogResponseDTO(
                1L,
                1L,
                "Star Carrier",
                MaintenanceType.ROUTINE,
                1L,
                "John Doe",
                2L,
                "Jane Smith",
                now.minusDays(1),
                now,
                MaintenanceStatus.COMPLETED,
                "Routine maintenance",
                new BigDecimal("1500.00")
        );

        requestDTO = new MaintenanceLogRequestDTO(
                1L,
                MaintenanceType.ROUTINE,
                1L,
                2L,
                now.minusDays(1),
                now,
                MaintenanceStatus.COMPLETED,
                "Routine maintenance",
                new BigDecimal("1500.00")
        );
    }

    @Test
    void getAllMaintenanceLogs_Success() {
        when(maintenanceLogService.getAllMaintenanceLogs(0, 20))
                .thenReturn(Flux.just(responseDTO));
        when(maintenanceLogService.countAll()).thenReturn(Mono.just(1L));

        webTestClient.get()
                .uri("/maintenance-logs?page=0&size=20")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("X-Total-Count", "1")
                .expectBodyList(MaintenanceLogResponseDTO.class)
                .hasSize(1)
                .consumeWith(result -> {
                    List<MaintenanceLogResponseDTO> body = result.getResponseBody();
                    assert body != null;
                    assert body.getFirst().id() == 1L;
                    assert "Star Carrier".equals(body.getFirst().spacecraftName());
                });

        verify(maintenanceLogService).getAllMaintenanceLogs(0, 20);
        verify(maintenanceLogService).countAll();
    }

    @Test
    void getAllMaintenanceLogs_WithCustomPagination() {
        when(maintenanceLogService.getAllMaintenanceLogs(1, 10))
                .thenReturn(Flux.just(responseDTO));
        when(maintenanceLogService.countAll()).thenReturn(Mono.just(20L));

        webTestClient.get()
                .uri("/maintenance-logs?page=1&size=10")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("X-Total-Count", "20")
                .expectBodyList(MaintenanceLogResponseDTO.class)
                .hasSize(1);

        verify(maintenanceLogService).getAllMaintenanceLogs(1, 10);
        verify(maintenanceLogService).countAll();
    }

    @Test
    void getAllMaintenanceLogs_ExceedsMaxSize() {
        when(maintenanceLogService.getAllMaintenanceLogs(0, 50))
                .thenReturn(Flux.just(responseDTO));

        webTestClient.get()
                .uri("/maintenance-logs?page=0&size=100")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void createMaintenanceLog_Success() {
        when(maintenanceLogService.createMaintenanceLog(any()))
                .thenReturn(Mono.just(responseDTO));

        webTestClient.post()
                .uri("/maintenance-logs")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDTO)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isEqualTo(1)
                .jsonPath("$.spacecraftName").isEqualTo("Star Carrier")
                .jsonPath("$.maintenanceType").isEqualTo("ROUTINE")
                .jsonPath("$.status").isEqualTo("COMPLETED");

        verify(maintenanceLogService).createMaintenanceLog(any());
    }

    @Test
    void createMaintenanceLog_InvalidData_MissingSpacecraftId() {
        MaintenanceLogRequestDTO invalidRequest = new MaintenanceLogRequestDTO(
                null,
                MaintenanceType.ROUTINE,
                1L,
                null,
                LocalDateTime.now(),
                null,
                MaintenanceStatus.SCHEDULED,
                "Test",
                new BigDecimal("1000.00")
        );

        webTestClient.post()
                .uri("/maintenance-logs")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidRequest)
                .exchange()
                .expectStatus().isBadRequest();

        verify(maintenanceLogService, never()).createMaintenanceLog(any());
    }

    @Test
    void createMaintenanceLog_InvalidData_MissingMaintenanceType() {
        MaintenanceLogRequestDTO invalidRequest = new MaintenanceLogRequestDTO(
                1L,
                null,
                1L,
                null,
                LocalDateTime.now(),
                null,
                MaintenanceStatus.SCHEDULED,
                "Test",
                new BigDecimal("1000.00")
        );

        webTestClient.post()
                .uri("/maintenance-logs")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidRequest)
                .exchange()
                .expectStatus().isBadRequest();

        verify(maintenanceLogService, never()).createMaintenanceLog(any());
    }

    @Test
    void createMaintenanceLog_InvalidData_MissingPerformedByUserId() {
        MaintenanceLogRequestDTO invalidRequest = new MaintenanceLogRequestDTO(
                1L,
                MaintenanceType.ROUTINE,
                null,
                null,
                LocalDateTime.now(),
                null,
                MaintenanceStatus.SCHEDULED,
                "Test",
                new BigDecimal("1000.00")
        );

        webTestClient.post()
                .uri("/maintenance-logs")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidRequest)
                .exchange()
                .expectStatus().isBadRequest();

        verify(maintenanceLogService, never()).createMaintenanceLog(any());
    }

    @Test
    void createMaintenanceLog_InvalidData_NegativeCost() {
        MaintenanceLogRequestDTO invalidRequest = new MaintenanceLogRequestDTO(
                1L,
                MaintenanceType.ROUTINE,
                1L,
                null,
                LocalDateTime.now(),
                null,
                MaintenanceStatus.SCHEDULED,
                "Test",
                new BigDecimal("-100.00")
        );

        webTestClient.post()
                .uri("/maintenance-logs")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidRequest)
                .exchange()
                .expectStatus().isBadRequest();

        verify(maintenanceLogService, never()).createMaintenanceLog(any());
    }

    @Test
    void createMaintenanceLog_SpacecraftNotFound() {
        when(maintenanceLogService.createMaintenanceLog(any()))
                .thenReturn(Mono.error(new InvalidOperationException("Spacecraft not found with id: 1")));

        webTestClient.post()
                .uri("/maintenance-logs")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDTO)
                .exchange()
                .expectStatus().isBadRequest();

        verify(maintenanceLogService).createMaintenanceLog(any());
    }

    @Test
    void updateMaintenanceStatus_Success() {
        MaintenanceLogRequestDTO updateRequest = new MaintenanceLogRequestDTO(
                null, null, null, null, null,
                LocalDateTime.now(), MaintenanceStatus.COMPLETED,
                "Updated", new BigDecimal("2000.00")
        );

        MaintenanceLogResponseDTO updatedResponse = new MaintenanceLogResponseDTO(
                1L, 1L, "Star Carrier", MaintenanceType.ROUTINE,
                1L, "John Doe", 2L, "Jane Smith",
                LocalDateTime.now().minusDays(1), LocalDateTime.now(),
                MaintenanceStatus.COMPLETED, "Updated", new BigDecimal("2000.00")
        );

        when(maintenanceLogService.updateMaintenanceStatus(eq(1L), any()))
                .thenReturn(Mono.just(updatedResponse));

        webTestClient.put()
                .uri("/maintenance-logs/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(1)
                .jsonPath("$.status").isEqualTo("COMPLETED")
                .jsonPath("$.description").isEqualTo("Updated");

        verify(maintenanceLogService).updateMaintenanceStatus(eq(1L), any());
    }

    @Test
    void updateMaintenanceStatus_NotFound() {
        MaintenanceLogRequestDTO updateRequest = new MaintenanceLogRequestDTO(
                null, null, null, null, null,
                LocalDateTime.now(), MaintenanceStatus.COMPLETED,
                "Updated", new BigDecimal("2000.00")
        );

        when(maintenanceLogService.updateMaintenanceStatus(eq(999L), any()))
                .thenReturn(Mono.error(new MaintenanceLogNotFoundException("Maintenance log not found with id: 999")));

        webTestClient.put()
                .uri("/maintenance-logs/999/status")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateRequest)
                .exchange()
                .expectStatus().isNotFound();

        verify(maintenanceLogService).updateMaintenanceStatus(eq(999L), any());
    }

    @Test
    void getSpacecraftMaintenanceHistory_Success() {
        when(maintenanceLogService.getSpacecraftMaintenanceHistory(1L, 0, 20))
                .thenReturn(Flux.just(responseDTO));
        when(maintenanceLogService.countBySpacecraftId(1L)).thenReturn(Mono.just(1L));

        webTestClient.get()
                .uri("/spacecrafts/1/maintenance?page=0&size=20")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("X-Total-Count", "1")
                .expectBodyList(MaintenanceLogResponseDTO.class)
                .hasSize(1)
                .consumeWith(result -> {
                    List<MaintenanceLogResponseDTO> body = result.getResponseBody();
                    assert body != null;
                    assert body.getFirst().spacecraftId() == 1L;
                });

        verify(maintenanceLogService).getSpacecraftMaintenanceHistory(1L, 0, 20);
        verify(maintenanceLogService).countBySpacecraftId(1L);
    }

    @Test
    void getSpacecraftMaintenanceHistory_EmptyResult() {
        when(maintenanceLogService.getSpacecraftMaintenanceHistory(999L, 0, 20))
                .thenReturn(Flux.empty());
        when(maintenanceLogService.countBySpacecraftId(999L)).thenReturn(Mono.just(0L));

        webTestClient.get()
                .uri("/spacecrafts/999/maintenance?page=0&size=20")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(MaintenanceLogResponseDTO.class)
                .hasSize(0);

        verify(maintenanceLogService).getSpacecraftMaintenanceHistory(999L, 0, 20);
        verify(maintenanceLogService).countBySpacecraftId(999L);
    }
}
