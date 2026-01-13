package org.orbitalLogistic.maintenance.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.orbitalLogistic.maintenance.application.ports.in.*;
import org.orbitalLogistic.maintenance.domain.model.MaintenanceLog;
import org.orbitalLogistic.maintenance.domain.model.enums.MaintenanceStatus;
import org.orbitalLogistic.maintenance.domain.model.enums.MaintenanceType;
import org.orbitalLogistic.maintenance.infrastructure.adapters.in.rest.MaintenanceLogRestController;
import org.orbitalLogistic.maintenance.infrastructure.adapters.in.rest.dto.MaintenanceLogRequestDTO;
import org.orbitalLogistic.maintenance.infrastructure.adapters.in.rest.dto.MaintenanceLogResponseDTO;
import org.orbitalLogistic.maintenance.infrastructure.adapters.in.rest.mapper.MaintenanceLogRestMapper;
import org.orbitalLogistic.maintenance.jwt.JwtService;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@WebFluxTest(MaintenanceLogRestController.class)
@TestPropertySource(properties = {
        "spring.cloud.config.enabled=false"
})
@Import(org.orbitalLogistic.maintenance.config.TestSecurityConfig.class)
class MaintenanceLogRestControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private CreateMaintenanceLogUseCase createMaintenanceLogUseCase;

    @MockitoBean
    private GetMaintenanceLogsUseCase getMaintenanceLogsUseCase;

    @MockitoBean
    private UpdateMaintenanceStatusUseCase updateMaintenanceStatusUseCase;

    @MockitoBean
    private MaintenanceLogRestMapper mapper;

    @MockitoBean
    private JwtService jwtService;

    private MaintenanceLog domainLog;
    private MaintenanceLogResponseDTO responseDTO;
    private MaintenanceLogRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();

        webTestClient = webTestClient.mutateWith(mockUser().roles("ADMIN"));

        domainLog = MaintenanceLog.builder()
                .id(1L)
                .spacecraftId(1L)
                .maintenanceType(MaintenanceType.ROUTINE)
                .performedByUserId(1L)
                .supervisedByUserId(2L)
                .startTime(now.minusDays(1))
                .endTime(now)
                .status(MaintenanceStatus.COMPLETED)
                .description("Routine maintenance")
                .cost(new BigDecimal("1500.00"))
                .build();

        responseDTO = new MaintenanceLogResponseDTO(
                1L,
                1L,
                "Star Carrier",
                org.orbitalLogistic.maintenance.infrastructure.adapters.in.rest.dto.MaintenanceType.ROUTINE,
                1L,
                "John Doe",
                2L,
                "Jane Smith",
                now.minusDays(1),
                now,
                org.orbitalLogistic.maintenance.infrastructure.adapters.in.rest.dto.MaintenanceStatus.COMPLETED,
                "Routine maintenance",
                new BigDecimal("1500.00")
        );

        requestDTO = new MaintenanceLogRequestDTO(
                1L,
                org.orbitalLogistic.maintenance.infrastructure.adapters.in.rest.dto.MaintenanceType.ROUTINE,
                1L,
                2L,
                now.minusDays(1),
                now,
                org.orbitalLogistic.maintenance.infrastructure.adapters.in.rest.dto.MaintenanceStatus.COMPLETED,
                "Routine maintenance",
                new BigDecimal("1500.00")
        );
    }

    @Test
    void getAllMaintenanceLogs_Success() {
        when(getMaintenanceLogsUseCase.getAllMaintenanceLogs(0, 20))
                .thenReturn(Flux.just(domainLog));
        when(getMaintenanceLogsUseCase.countAll()).thenReturn(Mono.just(1L));
        when(mapper.toResponseDTO(any(MaintenanceLog.class))).thenReturn(Mono.just(responseDTO));

        webTestClient.get()
                .uri("/maintenance-logs?page=0&size=20")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("X-Total-Count", "1");

        verify(getMaintenanceLogsUseCase).getAllMaintenanceLogs(0, 20);
        verify(getMaintenanceLogsUseCase).countAll();
    }

    @Test
    void createMaintenanceLog_Success() {
        CreateMaintenanceLogCommand command = CreateMaintenanceLogCommand.builder().build();
        
        when(mapper.toCreateCommand(any())).thenReturn(command);
        when(createMaintenanceLogUseCase.createMaintenanceLog(any()))
                .thenReturn(Mono.just(domainLog));
        when(mapper.toResponseDTO(any(MaintenanceLog.class))).thenReturn(Mono.just(responseDTO));

        webTestClient.post()
                .uri("/maintenance-logs")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDTO)
                .exchange()
                .expectStatus().isCreated();

        verify(createMaintenanceLogUseCase).createMaintenanceLog(any());
    }

    @Test
    void updateMaintenanceStatus_Success() {
        UpdateMaintenanceStatusCommand command = UpdateMaintenanceStatusCommand.builder().id(1L).build();
        
        when(mapper.toUpdateCommand(eq(1L), any())).thenReturn(command);
        when(updateMaintenanceStatusUseCase.updateMaintenanceStatus(any()))
                .thenReturn(Mono.just(domainLog));
        when(mapper.toResponseDTO(any(MaintenanceLog.class))).thenReturn(Mono.just(responseDTO));

        webTestClient.put()
                .uri("/maintenance-logs/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDTO)
                .exchange()
                .expectStatus().isOk();

        verify(updateMaintenanceStatusUseCase).updateMaintenanceStatus(any());
    }

    @Test
    void getSpacecraftMaintenanceHistory_Success() {
        when(getMaintenanceLogsUseCase.getSpacecraftMaintenanceHistory(1L, 0, 20))
                .thenReturn(Flux.just(domainLog));
        when(getMaintenanceLogsUseCase.countBySpacecraftId(1L)).thenReturn(Mono.just(1L));
        when(mapper.toResponseDTO(any(MaintenanceLog.class))).thenReturn(Mono.just(responseDTO));

        webTestClient.get()
                .uri("/spacecrafts/1/maintenance?page=0&size=20")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("X-Total-Count", "1");

        verify(getMaintenanceLogsUseCase).getSpacecraftMaintenanceHistory(1L, 0, 20);
        verify(getMaintenanceLogsUseCase).countBySpacecraftId(1L);
    }
}
