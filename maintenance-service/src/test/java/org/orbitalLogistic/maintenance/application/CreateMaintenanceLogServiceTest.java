package org.orbitalLogistic.maintenance.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.orbitalLogistic.maintenance.application.ports.in.CreateMaintenanceLogCommand;
import org.orbitalLogistic.maintenance.application.ports.out.MaintenanceLogRepository;
import org.orbitalLogistic.maintenance.application.ports.out.SpacecraftValidationPort;
import org.orbitalLogistic.maintenance.application.ports.out.UserValidationPort;
import org.orbitalLogistic.maintenance.application.usecases.CreateMaintenanceLogService;
import org.orbitalLogistic.maintenance.domain.model.MaintenanceLog;
import org.orbitalLogistic.maintenance.domain.model.enums.MaintenanceStatus;
import org.orbitalLogistic.maintenance.domain.model.enums.MaintenanceType;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateMaintenanceLogServiceTest {

    @Mock
    private MaintenanceLogRepository maintenanceLogRepository;

    @Mock
    private SpacecraftValidationPort spacecraftValidationPort;

    @Mock
    private UserValidationPort userValidationPort;

    @InjectMocks
    private CreateMaintenanceLogService createMaintenanceLogService;

    private CreateMaintenanceLogCommand command;
    private MaintenanceLog savedLog;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();

        command = CreateMaintenanceLogCommand.builder()
                .spacecraftId(1L)
                .maintenanceType(MaintenanceType.ROUTINE)
                .performedByUserId(1L)
                .supervisedByUserId(2L)
                .startTime(now.minusDays(1))
                .endTime(now)
                .status(MaintenanceStatus.SCHEDULED)
                .description("Test maintenance")
                .cost(new BigDecimal("1000.00"))
                .build();

        savedLog = MaintenanceLog.builder()
                .id(1L)
                .spacecraftId(1L)
                .maintenanceType(MaintenanceType.ROUTINE)
                .performedByUserId(1L)
                .supervisedByUserId(2L)
                .startTime(now.minusDays(1))
                .endTime(now)
                .status(MaintenanceStatus.SCHEDULED)
                .description("Test maintenance")
                .cost(new BigDecimal("1000.00"))
                .build();
    }

    @Test
    void createMaintenanceLog_Success() {
        when(spacecraftValidationPort.spacecraftExists(1L)).thenReturn(Mono.just(true));
        when(userValidationPort.userExists(1L)).thenReturn(Mono.just(true));
        when(userValidationPort.userExists(2L)).thenReturn(Mono.just(true));
        when(maintenanceLogRepository.save(any(MaintenanceLog.class))).thenReturn(Mono.just(savedLog));

        Mono<MaintenanceLog> result = createMaintenanceLogService.createMaintenanceLog(command);

        StepVerifier.create(result)
                .expectNextMatches(log -> log.getId() == 1L && log.getSpacecraftId() == 1L)
                .verifyComplete();

        verify(spacecraftValidationPort).spacecraftExists(1L);
        verify(userValidationPort).userExists(1L);
        verify(userValidationPort).userExists(2L);
        verify(maintenanceLogRepository).save(any(MaintenanceLog.class));
    }

    @Test
    void createMaintenanceLog_SpacecraftNotFound() {
        when(spacecraftValidationPort.spacecraftExists(1L)).thenReturn(Mono.just(false));
        when(userValidationPort.userExists(1L)).thenReturn(Mono.just(true));
        when(userValidationPort.userExists(2L)).thenReturn(Mono.just(true));

        Mono<MaintenanceLog> result = createMaintenanceLogService.createMaintenanceLog(command);

        StepVerifier.create(result)
                .expectErrorMatches(error -> error instanceof IllegalArgumentException &&
                        error.getMessage().contains("Spacecraft not found"))
                .verify();

        verify(spacecraftValidationPort).spacecraftExists(1L);
        verify(maintenanceLogRepository, never()).save(any());
    }

    @Test
    void createMaintenanceLog_UserNotFound() {
        when(spacecraftValidationPort.spacecraftExists(1L)).thenReturn(Mono.just(true));
        when(userValidationPort.userExists(1L)).thenReturn(Mono.just(false));
        when(userValidationPort.userExists(2L)).thenReturn(Mono.just(true));

        Mono<MaintenanceLog> result = createMaintenanceLogService.createMaintenanceLog(command);

        StepVerifier.create(result)
                .expectErrorMatches(error -> error instanceof IllegalArgumentException &&
                        error.getMessage().contains("Performed by user not found"))
                .verify();

        verify(maintenanceLogRepository, never()).save(any());
    }
}
