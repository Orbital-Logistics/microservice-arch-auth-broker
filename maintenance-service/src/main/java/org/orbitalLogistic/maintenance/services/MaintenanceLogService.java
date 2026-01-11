package org.orbitalLogistic.maintenance.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.orbitalLogistic.maintenance.clients.SpacecraftServiceClient;
import org.orbitalLogistic.maintenance.clients.UserServiceClient;
import org.orbitalLogistic.maintenance.dto.common.SpacecraftDTO;
import org.orbitalLogistic.maintenance.dto.common.UserDTO;
import org.orbitalLogistic.maintenance.dto.request.MaintenanceLogRequestDTO;
import org.orbitalLogistic.maintenance.dto.response.MaintenanceLogResponseDTO;
import org.orbitalLogistic.maintenance.entities.MaintenanceLog;
import org.orbitalLogistic.maintenance.exceptions.InvalidOperationException;
import org.orbitalLogistic.maintenance.exceptions.MaintenanceLogNotFoundException;
import org.orbitalLogistic.maintenance.mappers.MaintenanceLogMapper;
import org.orbitalLogistic.maintenance.repositories.MaintenanceLogRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class MaintenanceLogService {
    private final MaintenanceLogRepository maintenanceLogRepository;
    private final MaintenanceLogMapper maintenanceLogMapper;
    private final UserServiceClient userServiceClient;
    private final SpacecraftServiceClient spacecraftServiceClient;

    public Flux<MaintenanceLogResponseDTO> getAllMaintenanceLogs(int page, int size) {
        int offset = page * size;

        return maintenanceLogRepository
                .findAllPaginated(offset, size)
                .flatMap(this::toResponseDTO);
    }

    public Mono<Long> countAll() {
        return maintenanceLogRepository.countAll();
    }

    public Flux<MaintenanceLogResponseDTO> getSpacecraftMaintenanceHistory(Long spacecraftId, int page, int size) {
        int offset = page * size;

        return maintenanceLogRepository
                .findBySpacecraftIdPaginated(spacecraftId, size, offset)
                .flatMap(this::toResponseDTO);
    }

    public Mono<Long> countBySpacecraftId(Long spacecraftId) {
        return maintenanceLogRepository.countBySpacecraftId(spacecraftId);
    }

    public Mono<MaintenanceLogResponseDTO> createMaintenanceLog(MaintenanceLogRequestDTO request) {
        Mono<Boolean> spacecraftExists = spacecraftServiceClient.spacecraftExists(request.spacecraftId())
                .doOnError(e -> log.error("Failed to validate spacecraft {}: {}", request.spacecraftId(), e.getMessage()))
                .onErrorMap(ex -> new InvalidOperationException("Unable to validate spacecraft. Spacecraft service may be unavailable."))
                .flatMap(exists -> exists
                    ? Mono.just(true)
                    : Mono.error(new InvalidOperationException("Spacecraft not found with id: " + request.spacecraftId())));

        Mono<Boolean> userExists = userServiceClient.userExists(request.performedByUserId())
                .doOnError(ex -> log.error("Failed to validate performed by user: {}", ex.getMessage()))
                .onErrorMap(ex -> new InvalidOperationException("Unable to validate performed by user. User service may be unavailable."))
                .flatMap(exists -> exists
                    ? Mono.just(true)
                    : Mono.error(new InvalidOperationException("Performed by user not found with id: " + request.performedByUserId())));

        Mono<Boolean> supervisedExists =
                request.supervisedByUserId() == null
                    ? Mono.just(true)
                    : userServiceClient.userExists(request.supervisedByUserId())
                        .doOnError(ex -> log.error("Failed to validate supervised by user: {}", ex.getMessage()))
                        .onErrorMap(ex -> !(ex instanceof InvalidOperationException), 
                                ex -> new InvalidOperationException("Unable to validate supervised by user. User service may be unavailable."))
                        .flatMap(exists -> exists
                            ? Mono.just(true)
                            : Mono.error(new InvalidOperationException("Supervised by user not found with id: " + request.supervisedByUserId()))
                        );

        return Mono.zip(spacecraftExists, userExists, supervisedExists)
                .then(Mono.fromSupplier(() -> maintenanceLogMapper.toEntity(request)))
                .flatMap(maintenanceLogRepository::save)
                .flatMap(this::toResponseDTO);

    }

    public Mono<MaintenanceLogResponseDTO> updateMaintenanceStatus(Long id, MaintenanceLogRequestDTO request) {
        return maintenanceLogRepository.findById(id)
                .switchIfEmpty(Mono.error(new MaintenanceLogNotFoundException("Maintenance log not found with id: " + id)))
                .flatMap(existing -> {
                    if (request.status() != null) existing.setStatus(request.status());
                    if (request.endTime() != null) existing.setEndTime(request.endTime());
                    if (request.cost() != null) existing.setCost(request.cost());
                    if (request.description() != null) existing.setDescription(request.description());
                    return maintenanceLogRepository.save(existing);
                })
                .flatMap(this::toResponseDTO);
    }

    private Mono<MaintenanceLogResponseDTO> toResponseDTO(MaintenanceLog lg) {
        Mono<String> spacecraftName = spacecraftServiceClient.getSpacecraftById(lg.getSpacecraftId())
                .map(SpacecraftDTO::name)
                .onErrorResume(ex -> {
                    log.warn("Fallback: Unable to fetch spacecraft with id: {}, error: {}", lg.getSpacecraftId(), ex.getMessage());
                    return Mono.just("Unknown");
                })
                .defaultIfEmpty("Unknown");

        Mono<String> performedByName = userServiceClient.getUserById(lg.getPerformedByUserId())
                .map(UserDTO::username)
                .onErrorResume(ex -> {
                    log.warn("Fallback: Unable to fetch user with id: {}, error: {}", lg.getPerformedByUserId(), ex.getMessage());
                    return Mono.just("Unknown");
                })
                .defaultIfEmpty("Unknown");

        Mono<String> supervisedByName =
                lg.getSupervisedByUserId() == null
                    ? Mono.just("")
                    : userServiceClient.getUserById(lg.getSupervisedByUserId())
                    .map(UserDTO::username)
                    .onErrorResume(ex -> {
                        log.warn("Fallback: Unable to fetch supervised user with id: {}, error: {}", lg.getSupervisedByUserId(), ex.getMessage());
                        return Mono.just("Unknown");
                    })
                    .defaultIfEmpty("");

        return Mono.zip(spacecraftName, performedByName, supervisedByName)
                .map(tuple -> maintenanceLogMapper.toResponseDTO(
                        lg,
                        tuple.getT1(),
                        tuple.getT2(),
                        tuple.getT3()
                ));
    }
}
