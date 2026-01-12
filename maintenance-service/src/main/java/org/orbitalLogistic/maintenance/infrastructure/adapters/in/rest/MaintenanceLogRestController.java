package org.orbitalLogistic.maintenance.infrastructure.adapters.in.rest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.orbitalLogistic.maintenance.application.ports.in.*;
import org.orbitalLogistic.maintenance.domain.model.MaintenanceLog;
import org.orbitalLogistic.maintenance.infrastructure.adapters.in.rest.dto.MaintenanceLogRequestDTO;
import org.orbitalLogistic.maintenance.infrastructure.adapters.in.rest.dto.MaintenanceLogResponseDTO;
import org.orbitalLogistic.maintenance.infrastructure.adapters.in.rest.mapper.MaintenanceLogRestMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@Validated
@RequestMapping("/")
@RequiredArgsConstructor
public class MaintenanceLogRestController {

    private final CreateMaintenanceLogUseCase createMaintenanceLogUseCase;
    private final GetMaintenanceLogsUseCase getMaintenanceLogsUseCase;
    private final UpdateMaintenanceStatusUseCase updateMaintenanceStatusUseCase;
    private final MaintenanceLogRestMapper mapper;

    @GetMapping("/maintenance-logs")
    public Mono<ResponseEntity<Flux<MaintenanceLogResponseDTO>>> getAllMaintenanceLogs(
            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "Page must be >= 0")
            int page,

            @RequestParam(defaultValue = "20")
            @Min(value = 0, message = "Size must be >= 0")
            @Max(value = 50, message = "Size must be <= 50")
            int size) {

        Flux<MaintenanceLog> domainLogs = getMaintenanceLogsUseCase.getAllMaintenanceLogs(page, size);
        Flux<MaintenanceLogResponseDTO> items = domainLogs.flatMap(mapper::toResponseDTO);
        Mono<Long> totalMono = getMaintenanceLogsUseCase.countAll();

        return totalMono.map(total -> ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(total))
                .body(items));
    }

    @PostMapping("/maintenance-logs")
    @PreAuthorize("hasAnyRole('ADMIN', 'MAINTENANCE_ENGINEER')")
    public Mono<ResponseEntity<MaintenanceLogResponseDTO>> createMaintenanceLog(
            @Valid @RequestBody MaintenanceLogRequestDTO request) {

        CreateMaintenanceLogCommand command = mapper.toCreateCommand(request);
        
        return createMaintenanceLogUseCase.createMaintenanceLog(command)
                .flatMap(mapper::toResponseDTO)
                .map(response -> ResponseEntity
                        .status(HttpStatus.CREATED)
                        .body(response));
    }

    @PutMapping("/maintenance-logs/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS_OFFICER')")
    public Mono<ResponseEntity<MaintenanceLogResponseDTO>> updateMaintenanceStatus(
            @PathVariable Long id,
            @RequestBody MaintenanceLogRequestDTO request) {

        UpdateMaintenanceStatusCommand command = mapper.toUpdateCommand(id, request);
        
        return updateMaintenanceStatusUseCase.updateMaintenanceStatus(command)
                .flatMap(mapper::toResponseDTO)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/spacecrafts/{id}/maintenance")
    public Mono<ResponseEntity<Flux<MaintenanceLogResponseDTO>>> getSpacecraftMaintenanceHistory(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "Page must be >= 0")
            int page,

            @RequestParam(defaultValue = "20")
            @Min(value = 0, message = "Size must be >= 0")
            @Max(value = 50, message = "Size must be <= 50")
            int size) {

        Flux<MaintenanceLog> domainLogs = getMaintenanceLogsUseCase.getSpacecraftMaintenanceHistory(id, page, size);
        Flux<MaintenanceLogResponseDTO> items = domainLogs.flatMap(mapper::toResponseDTO);
        Mono<Long> totalMono = getMaintenanceLogsUseCase.countBySpacecraftId(id);

        return totalMono.map(total -> ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(total))
                .body(items));
    }
}
