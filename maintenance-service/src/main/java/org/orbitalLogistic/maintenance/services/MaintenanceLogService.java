package org.orbitalLogistic.maintenance.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.orbitalLogistic.maintenance.clients.spacecraft.SpacecraftServiceClient;
import org.orbitalLogistic.maintenance.clients.user.UserServiceClient;
import org.orbitalLogistic.maintenance.dto.common.PageResponseDTO;
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

    public Mono<PageResponseDTO<MaintenanceLogResponseDTO>> getAllMaintenanceLogs(int page, int size) {
        int offset = page * size;

        Mono<Long> totalMono = maintenanceLogRepository.countAll();

        Flux<MaintenanceLogResponseDTO> itemsMono = maintenanceLogRepository
                .findAllPaginated(offset, size)
                .flatMap(this::toResponseDTO);

        return totalMono.zipWith(itemsMono.collectList(), (total, items) -> {
            int totalPages = (int) Math.ceil((double) total / size);

            return new PageResponseDTO<>(
                    items,
                    page,
                    size,
                    total,
                    totalPages,
                    page == 0,
                    page >= totalPages - 1
            );
        });
    }

    public Mono<PageResponseDTO<MaintenanceLogResponseDTO>> getSpacecraftMaintenanceHistory(Long spacecraftId, int page, int size) {
        int offset = page * size;

        Mono<Long> totalMono = maintenanceLogRepository.countBySpacecraftId(spacecraftId);

        Flux<MaintenanceLogResponseDTO> itemsMono = maintenanceLogRepository
                .findBySpacecraftIdPaginated(spacecraftId, size, offset)
                .flatMap(this::toResponseDTO);

        return totalMono.zipWith(itemsMono.collectList(), (total, items) -> {
            int totalPages = (int) Math.ceil((double) total / size);

            return new PageResponseDTO<>(
                    items,
                    page,
                    size,
                    total,
                    totalPages,
                    page == 0,
                    page >= totalPages - 1
            );
        });
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

    private Mono<MaintenanceLogResponseDTO> toResponseDTO(MaintenanceLog log) {
        Mono<String> spacecraftName = spacecraftServiceClient.getSpacecraftById(log.getSpacecraftId())
                .map(SpacecraftDTO::name)
                .switchIfEmpty(Mono.just("Unknown"))
                .onErrorReturn("Unknown");

        Mono<String> performedByName = userServiceClient.getUserById(log.getPerformedByUserId())
                .map(UserDTO::username)
                .switchIfEmpty(Mono.just("Unknown"))
                .onErrorReturn("Unknown");

        Mono<String> supervisedByName =
                log.getSupervisedByUserId() == null
                    ? Mono.just("")
                    : userServiceClient.getUserById(log.getSupervisedByUserId())
                    .map(UserDTO::username)
                    .onErrorReturn("")
                    .defaultIfEmpty("");

        return Mono.zip(spacecraftName, performedByName, supervisedByName)
                .map(tuple -> maintenanceLogMapper.toResponseDTO(
                        log,
                        tuple.getT1(),
                        tuple.getT2(),
                        tuple.getT3()
                ));
    }
}
