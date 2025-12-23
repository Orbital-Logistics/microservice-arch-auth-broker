package org.orbitalLogistic.maintenance.controllers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.orbitalLogistic.maintenance.dto.request.MaintenanceLogRequestDTO;
import org.orbitalLogistic.maintenance.dto.response.MaintenanceLogResponseDTO;
import org.orbitalLogistic.maintenance.services.MaintenanceLogService;
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
public class MaintenanceLogController {

    private final MaintenanceLogService maintenanceLogService;

    @GetMapping("/maintenance-logs")
    @PreAuthorize("hasRole('ADMIN') or #request.username == authentication.name")
    public Mono<ResponseEntity<Flux<MaintenanceLogResponseDTO>>> getAllMaintenanceLogs(
            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "Page must be >= 0")
            int page,

            @RequestParam(defaultValue = "20")
            @Min(value = 0, message = "Size must be >= 0")
            @Max(value = 50, message = "Size must be <= 50")
            int size) {

        Flux<MaintenanceLogResponseDTO> items = maintenanceLogService.getAllMaintenanceLogs(page, size);
        Mono<Long> totalMono = maintenanceLogService.countAll();

        return totalMono.map(total -> ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(total))
                .body(items));
    }

    @PostMapping("/maintenance-logs")
    @PreAuthorize("hasAnyRole('ADMIN', 'MAINTENANCE_ENGINEER')")
    public Mono<ResponseEntity<MaintenanceLogResponseDTO>> createMaintenanceLog(
            @Valid @RequestBody MaintenanceLogRequestDTO request) {

        return maintenanceLogService.createMaintenanceLog(request)
                .map(response ->
                        ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(response));
    }

    @PutMapping("/maintenance-logs/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS_OFFICER')")
    public Mono<ResponseEntity<MaintenanceLogResponseDTO>> updateMaintenanceStatus(
            @PathVariable Long id,
            @RequestBody MaintenanceLogRequestDTO request) {

        return maintenanceLogService.updateMaintenanceStatus(id, request)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/spacecrafts/{id}/maintenance")
    @PreAuthorize("hasRole('ADMIN') or #request.username == authentication.name")
    public Mono<ResponseEntity<Flux<MaintenanceLogResponseDTO>>> getSpacecraftMaintenanceHistory(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "Page must be >= 0")
            int page,

            @RequestParam(defaultValue = "20")
            @Min(value = 0, message = "Size must be >= 0")
            @Max(value = 50, message = "Size must be <= 50")
            int size) {

        Flux<MaintenanceLogResponseDTO> items = maintenanceLogService.getSpacecraftMaintenanceHistory(id, page, size);
        Mono<Long> totalMono = maintenanceLogService.countBySpacecraftId(id);

        return totalMono.map(total -> ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(total))
                .body(items));
    }
}
