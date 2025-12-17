package org.orbitalLogistic.maintenance.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.orbitalLogistic.maintenance.clients.spacecraft.SpacecraftServiceClient;
import org.orbitalLogistic.maintenance.clients.user.UserServiceClient;
import org.orbitalLogistic.maintenance.dto.common.SpacecraftDTO;
import org.orbitalLogistic.maintenance.dto.common.UserDTO;
import org.orbitalLogistic.maintenance.dto.request.MaintenanceLogRequestDTO;
import org.orbitalLogistic.maintenance.dto.response.MaintenanceLogResponseDTO;
import org.orbitalLogistic.maintenance.entities.MaintenanceLog;
import org.orbitalLogistic.maintenance.entities.enums.MaintenanceStatus;
import org.orbitalLogistic.maintenance.entities.enums.MaintenanceType;
import org.orbitalLogistic.maintenance.exceptions.InvalidOperationException;
import org.orbitalLogistic.maintenance.exceptions.MaintenanceLogNotFoundException;
import org.orbitalLogistic.maintenance.mappers.MaintenanceLogMapper;
import org.orbitalLogistic.maintenance.repositories.MaintenanceLogRepository;
import org.orbitalLogistic.maintenance.services.MaintenanceLogService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MaintenanceLogServiceTest {

    @Mock
    private MaintenanceLogRepository maintenanceLogRepository;

    @Mock
    private MaintenanceLogMapper maintenanceLogMapper;

    @Mock
    private UserServiceClient userServiceClient;

    @Mock
    private SpacecraftServiceClient spacecraftServiceClient;

    @InjectMocks
    private MaintenanceLogService maintenanceLogService;

    private MaintenanceLog testLog;
    private MaintenanceLogRequestDTO requestDTO;
    private MaintenanceLogResponseDTO responseDTO;
    private SpacecraftDTO spacecraftDTO;
    private UserDTO userDTO;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();

        testLog = MaintenanceLog.builder()
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

        spacecraftDTO = new SpacecraftDTO(1L, "SC-001", "Star Carrier");
        userDTO = new UserDTO(1L, "John Doe", "john@example.com");
    }

    @Test
    void getAllMaintenanceLogs_Success() {
        when(maintenanceLogRepository.countAll()).thenReturn(Mono.just(1L));
        when(maintenanceLogRepository.findAllPaginated(0, 20)).thenReturn(Flux.just(testLog));
        when(spacecraftServiceClient.getSpacecraftById(1L)).thenReturn(Mono.just(spacecraftDTO));
        when(userServiceClient.getUserById(1L)).thenReturn(Mono.just(userDTO));
        when(userServiceClient.getUserById(2L)).thenReturn(Mono.just(new UserDTO(2L, "Jane Smith", "jane@example.com")));
        when(maintenanceLogMapper.toResponseDTO(any(), anyString(), anyString(), anyString())).thenReturn(responseDTO);

        StepVerifier.create(maintenanceLogService.getAllMaintenanceLogs(0, 20))
                .assertNext(page -> {
                    assertEquals(1L, page.totalElements());
                    assertEquals(1, page.totalPages());
                    assertEquals(1, page.content().size());
                    assertTrue(page.first());
                    assertTrue(page.last());
                })
                .verifyComplete();

        verify(maintenanceLogRepository).countAll();
        verify(maintenanceLogRepository).findAllPaginated(0, 20);
    }

    @Test
    void getSpacecraftMaintenanceHistory_Success() {
        when(maintenanceLogRepository.countBySpacecraftId(1L)).thenReturn(Mono.just(1L));
        when(maintenanceLogRepository.findBySpacecraftIdPaginated(1L, 20, 0)).thenReturn(Flux.just(testLog));
        when(spacecraftServiceClient.getSpacecraftById(1L)).thenReturn(Mono.just(spacecraftDTO));
        when(userServiceClient.getUserById(1L)).thenReturn(Mono.just(userDTO));
        when(userServiceClient.getUserById(2L)).thenReturn(Mono.just(new UserDTO(2L, "Jane Smith", "jane@example.com")));
        when(maintenanceLogMapper.toResponseDTO(any(), anyString(), anyString(), anyString())).thenReturn(responseDTO);

        StepVerifier.create(maintenanceLogService.getSpacecraftMaintenanceHistory(1L, 0, 20))
                .assertNext(page -> {
                    assertEquals(1L, page.totalElements());
                    assertEquals(1, page.content().size());
                })
                .verifyComplete();

        verify(maintenanceLogRepository).countBySpacecraftId(1L);
        verify(maintenanceLogRepository).findBySpacecraftIdPaginated(1L, 20, 0);
    }

    @Test
    void createMaintenanceLog_Success() {
        when(spacecraftServiceClient.spacecraftExists(1L)).thenReturn(Mono.just(true));
        when(userServiceClient.userExists(1L)).thenReturn(Mono.just(true));
        when(userServiceClient.userExists(2L)).thenReturn(Mono.just(true));
        when(maintenanceLogMapper.toEntity(any())).thenReturn(testLog);
        when(maintenanceLogRepository.save(any())).thenReturn(Mono.just(testLog));
        when(spacecraftServiceClient.getSpacecraftById(1L)).thenReturn(Mono.just(spacecraftDTO));
        when(userServiceClient.getUserById(1L)).thenReturn(Mono.just(userDTO));
        when(userServiceClient.getUserById(2L)).thenReturn(Mono.just(new UserDTO(2L, "Jane Smith", "jane@example.com")));
        when(maintenanceLogMapper.toResponseDTO(any(), anyString(), anyString(), anyString())).thenReturn(responseDTO);

        StepVerifier.create(maintenanceLogService.createMaintenanceLog(requestDTO))
                .assertNext(result -> {
                    assertNotNull(result);
                    assertEquals(1L, result.id());
                    assertEquals("Star Carrier", result.spacecraftName());
                    assertEquals(MaintenanceType.ROUTINE, result.maintenanceType());
                })
                .verifyComplete();

        verify(spacecraftServiceClient).spacecraftExists(1L);
        verify(userServiceClient).userExists(1L);
        verify(userServiceClient).userExists(2L);
        verify(maintenanceLogRepository).save(any());
    }

    @Test
    void createMaintenanceLog_SpacecraftNotFound() {
        when(spacecraftServiceClient.spacecraftExists(1L)).thenReturn(Mono.just(false));
        when(userServiceClient.userExists(anyLong())).thenReturn(Mono.just(true));

        StepVerifier.create(maintenanceLogService.createMaintenanceLog(requestDTO))
                .expectErrorMatches(throwable ->
                        throwable instanceof InvalidOperationException &&
                                throwable.getMessage().contains("Spacecraft not found"))
                .verify();

        verify(spacecraftServiceClient).spacecraftExists(1L);
        verify(maintenanceLogRepository, never()).save(any());
    }

    @Test
    void createMaintenanceLog_UserNotFound() {
        when(spacecraftServiceClient.spacecraftExists(1L)).thenReturn(Mono.just(true));
        when(userServiceClient.userExists(1L)).thenReturn(Mono.just(false));
        when(userServiceClient.userExists(2L)).thenReturn(Mono.just(true));

        StepVerifier.create(maintenanceLogService.createMaintenanceLog(requestDTO))
                .expectErrorMatches(throwable ->
                        throwable instanceof InvalidOperationException &&
                                throwable.getMessage().contains("Performed by user not found"))
                .verify();

        verify(spacecraftServiceClient).spacecraftExists(1L);
        verify(userServiceClient).userExists(1L);
        verify(maintenanceLogRepository, never()).save(any());
    }

    @Test
    void createMaintenanceLog_SupervisedUserNotFound() {
        when(spacecraftServiceClient.spacecraftExists(1L)).thenReturn(Mono.just(true));
        when(userServiceClient.userExists(1L)).thenReturn(Mono.just(true));
        when(userServiceClient.userExists(2L)).thenReturn(Mono.just(false));

        StepVerifier.create(maintenanceLogService.createMaintenanceLog(requestDTO))
                .expectErrorMatches(throwable ->
                        throwable instanceof InvalidOperationException &&
                                throwable.getMessage().contains("Supervised by user not found"))
                .verify();

        verify(spacecraftServiceClient).spacecraftExists(1L);
        verify(userServiceClient).userExists(1L);
        verify(userServiceClient).userExists(2L);
        verify(maintenanceLogRepository, never()).save(any());
    }

    @Test
    void createMaintenanceLog_WithoutSupervisor_Success() {
        MaintenanceLogRequestDTO requestWithoutSupervisor = new MaintenanceLogRequestDTO(
                1L, MaintenanceType.ROUTINE, 1L, null,
                LocalDateTime.now(), null, MaintenanceStatus.SCHEDULED,
                "Test", new BigDecimal("1000.00")
        );

        MaintenanceLog logWithoutSupervisor = MaintenanceLog.builder()
                .id(1L)
                .spacecraftId(1L)
                .maintenanceType(MaintenanceType.ROUTINE)
                .performedByUserId(1L)
                .status(MaintenanceStatus.SCHEDULED)
                .build();

        when(spacecraftServiceClient.spacecraftExists(1L)).thenReturn(Mono.just(true));
        when(userServiceClient.userExists(1L)).thenReturn(Mono.just(true));
        when(maintenanceLogMapper.toEntity(any())).thenReturn(logWithoutSupervisor);
        when(maintenanceLogRepository.save(any())).thenReturn(Mono.just(logWithoutSupervisor));
        when(spacecraftServiceClient.getSpacecraftById(1L)).thenReturn(Mono.just(spacecraftDTO));
        when(userServiceClient.getUserById(1L)).thenReturn(Mono.just(userDTO));
        when(maintenanceLogMapper.toResponseDTO(any(), anyString(), anyString(), anyString())).thenReturn(responseDTO);

        StepVerifier.create(maintenanceLogService.createMaintenanceLog(requestWithoutSupervisor))
                .assertNext(Assertions::assertNotNull)
                .verifyComplete();

        verify(spacecraftServiceClient).spacecraftExists(1L);
        verify(userServiceClient).userExists(1L);
        verify(userServiceClient, never()).userExists(2L);
    }

    @Test
    void updateMaintenanceStatus_Success() {
        MaintenanceLogRequestDTO updateRequest = new MaintenanceLogRequestDTO(
                null, null, null, null, null,
                LocalDateTime.now(), MaintenanceStatus.COMPLETED,
                "Updated description", new BigDecimal("2000.00")
        );

        when(maintenanceLogRepository.findById(1L)).thenReturn(Mono.just(testLog));
        when(maintenanceLogRepository.save(any())).thenReturn(Mono.just(testLog));
        when(spacecraftServiceClient.getSpacecraftById(1L)).thenReturn(Mono.just(spacecraftDTO));
        when(userServiceClient.getUserById(1L)).thenReturn(Mono.just(userDTO));
        when(userServiceClient.getUserById(2L)).thenReturn(Mono.just(new UserDTO(2L, "Jane Smith", "jane@example.com")));
        when(maintenanceLogMapper.toResponseDTO(any(), anyString(), anyString(), anyString())).thenReturn(responseDTO);

        StepVerifier.create(maintenanceLogService.updateMaintenanceStatus(1L, updateRequest))
                .assertNext(result -> {
                    assertNotNull(result);
                    assertEquals(1L, result.id());
                })
                .verifyComplete();

        verify(maintenanceLogRepository).findById(1L);
        verify(maintenanceLogRepository).save(any());
    }

    @Test
    void updateMaintenanceStatus_NotFound() {
        MaintenanceLogRequestDTO updateRequest = new MaintenanceLogRequestDTO(
                null, null, null, null, null,
                LocalDateTime.now(), MaintenanceStatus.COMPLETED,
                "Updated", new BigDecimal("2000.00")
        );

        when(maintenanceLogRepository.findById(999L)).thenReturn(Mono.empty());

        StepVerifier.create(maintenanceLogService.updateMaintenanceStatus(999L, updateRequest))
                .expectErrorMatches(throwable ->
                        throwable instanceof MaintenanceLogNotFoundException &&
                                throwable.getMessage().contains("Maintenance log not found"))
                .verify();

        verify(maintenanceLogRepository).findById(999L);
        verify(maintenanceLogRepository, never()).save(any());
    }

    @Test
    void toResponseDTO_SpacecraftServiceError() {
        when(spacecraftServiceClient.getSpacecraftById(1L)).thenReturn(Mono.error(new RuntimeException("Service error")));
        when(userServiceClient.getUserById(1L)).thenReturn(Mono.just(userDTO));
        when(userServiceClient.getUserById(2L)).thenReturn(Mono.just(new UserDTO(2L, "Jane Smith", "jane@example.com")));
        when(maintenanceLogMapper.toResponseDTO(any(), eq("Unknown"), anyString(), anyString())).thenReturn(responseDTO);

        when(spacecraftServiceClient.spacecraftExists(1L)).thenReturn(Mono.just(true));
        when(userServiceClient.userExists(1L)).thenReturn(Mono.just(true));
        when(userServiceClient.userExists(2L)).thenReturn(Mono.just(true));
        when(maintenanceLogMapper.toEntity(any())).thenReturn(testLog);
        when(maintenanceLogRepository.save(any())).thenReturn(Mono.just(testLog));

        StepVerifier.create(maintenanceLogService.createMaintenanceLog(requestDTO))
                .assertNext(Assertions::assertNotNull)
                .verifyComplete();
    }

    @Test
    void toResponseDTO_UserServiceError() {
        when(spacecraftServiceClient.getSpacecraftById(1L)).thenReturn(Mono.just(spacecraftDTO));
        when(userServiceClient.getUserById(1L)).thenReturn(Mono.error(new RuntimeException("Service error")));
        when(userServiceClient.getUserById(2L)).thenReturn(Mono.just(new UserDTO(2L, "Jane Smith", "jane@example.com")));
        when(maintenanceLogMapper.toResponseDTO(any(), anyString(), eq("Unknown"), anyString())).thenReturn(responseDTO);

        when(spacecraftServiceClient.spacecraftExists(1L)).thenReturn(Mono.just(true));
        when(userServiceClient.userExists(1L)).thenReturn(Mono.just(true));
        when(userServiceClient.userExists(2L)).thenReturn(Mono.just(true));
        when(maintenanceLogMapper.toEntity(any())).thenReturn(testLog);
        when(maintenanceLogRepository.save(any())).thenReturn(Mono.just(testLog));

        StepVerifier.create(maintenanceLogService.createMaintenanceLog(requestDTO))
                .assertNext(Assertions::assertNotNull)
                .verifyComplete();
    }
}

