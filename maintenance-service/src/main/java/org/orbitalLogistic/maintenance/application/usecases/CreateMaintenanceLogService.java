package org.orbitalLogistic.maintenance.application.usecases;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.orbitalLogistic.maintenance.application.ports.in.CreateMaintenanceLogCommand;
import org.orbitalLogistic.maintenance.application.ports.in.CreateMaintenanceLogUseCase;
import org.orbitalLogistic.maintenance.application.ports.out.MaintenanceLogRepository;
import org.orbitalLogistic.maintenance.application.ports.out.SpacecraftValidationPort;
import org.orbitalLogistic.maintenance.application.ports.out.UserValidationPort;
import org.orbitalLogistic.maintenance.domain.model.MaintenanceLog;
import org.orbitalLogistic.maintenance.domain.model.enums.MaintenanceStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateMaintenanceLogService implements CreateMaintenanceLogUseCase {

    private final MaintenanceLogRepository maintenanceLogRepository;
    private final SpacecraftValidationPort spacecraftValidationPort;
    private final UserValidationPort userValidationPort;

    @Override
    public Mono<MaintenanceLog> createMaintenanceLog(CreateMaintenanceLogCommand command) {
        log.debug("Creating maintenance log for spacecraft: {}", command.spacecraftId());

        Mono<Boolean> spacecraftExists = spacecraftValidationPort.spacecraftExists(command.spacecraftId())
                .doOnError(e -> log.error("Failed to validate spacecraft {}: {}", command.spacecraftId(), e.getMessage()))
                .onErrorMap(ex -> new IllegalStateException("Unable to validate spacecraft. Spacecraft service may be unavailable."))
                .flatMap(exists -> exists
                        ? Mono.just(true)
                        : Mono.error(new IllegalArgumentException("Spacecraft not found with id: " + command.spacecraftId())));

        Mono<Boolean> userExists = userValidationPort.userExists(command.performedByUserId())
                .doOnError(ex -> log.error("Failed to validate performed by user: {}", ex.getMessage()))
                .onErrorMap(ex -> new IllegalStateException("Unable to validate performed by user. User service may be unavailable."))
                .flatMap(exists -> exists
                        ? Mono.just(true)
                        : Mono.error(new IllegalArgumentException("Performed by user not found with id: " + command.performedByUserId())));

        Mono<Boolean> supervisedExists = command.supervisedByUserId() == null
                ? Mono.just(true)
                : userValidationPort.userExists(command.supervisedByUserId())
                .doOnError(ex -> log.error("Failed to validate supervised by user: {}", ex.getMessage()))
                .onErrorMap(ex -> !(ex instanceof IllegalArgumentException),
                        ex -> new IllegalStateException("Unable to validate supervised by user. User service may be unavailable."))
                .flatMap(exists -> exists
                        ? Mono.just(true)
                        : Mono.error(new IllegalArgumentException("Supervised by user not found with id: " + command.supervisedByUserId()))
                );

        return Mono.zip(spacecraftExists, userExists, supervisedExists)
                .then(Mono.fromSupplier(() -> buildMaintenanceLog(command)))
                .doOnNext(MaintenanceLog::validate)
                .flatMap(maintenanceLogRepository::save)
                .doOnSuccess(savedLog -> log.info("Maintenance log created with id: {}", savedLog.getId()));
    }

    private MaintenanceLog buildMaintenanceLog(CreateMaintenanceLogCommand command) {
        return MaintenanceLog.builder()
                .spacecraftId(command.spacecraftId())
                .maintenanceType(command.maintenanceType())
                .performedByUserId(command.performedByUserId())
                .supervisedByUserId(command.supervisedByUserId())
                .startTime(command.startTime())
                .endTime(command.endTime())
                .status(command.status() != null ? command.status() : MaintenanceStatus.SCHEDULED)
                .description(command.description())
                .cost(command.cost())
                .build();
    }
}
