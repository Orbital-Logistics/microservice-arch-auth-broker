package org.orbitalLogistic.maintenance.controllers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.orbitalLogistic.maintenance.dto.common.PageResponseDTO;
import org.orbitalLogistic.maintenance.dto.request.MaintenanceLogRequestDTO;
import org.orbitalLogistic.maintenance.dto.response.MaintenanceLogResponseDTO;
import org.orbitalLogistic.maintenance.services.MaintenanceLogService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@Validated
@RequestMapping("/")
@RequiredArgsConstructor
public class MaintenanceLogController {

    private final MaintenanceLogService maintenanceLogService;

    @GetMapping("/maintenance-logs")
    public Mono<ResponseEntity<PageResponseDTO<MaintenanceLogResponseDTO>>> getAllMaintenanceLogs(
            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "Page must be >= 0")
            int page,

            @RequestParam(defaultValue = "20")
            @Min(value = 0, message = "Size must be >= 0")
            @Max(value = 50, message = "Size must be <= 50")
            int size) {

        return maintenanceLogService.getAllMaintenanceLogs(page, size)
                .map(response ->
                        ResponseEntity.ok()
                        .header("X-Total-Count", String.valueOf(response.totalElements()))
                        .body(response));
    }

    @PostMapping("/maintenance-logs")
    public Mono<ResponseEntity<MaintenanceLogResponseDTO>> createMaintenanceLog(
            @Valid @RequestBody MaintenanceLogRequestDTO request) {

        return maintenanceLogService.createMaintenanceLog(request)
                .map(response ->
                        ResponseEntity
                        .status(HttpStatus.CREATED)
                        .body(response));
    }

    @PutMapping("/maintenance-logs/{id}/status")
    public Mono<ResponseEntity<MaintenanceLogResponseDTO>> updateMaintenanceStatus(
            @PathVariable Long id,
            @RequestBody MaintenanceLogRequestDTO request) {

        return maintenanceLogService.updateMaintenanceStatus(id, request)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/spacecrafts/{id}/maintenance")
    public Mono<ResponseEntity<PageResponseDTO<MaintenanceLogResponseDTO>>> getSpacecraftMaintenanceHistory(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "Page must be >= 0")
            int page,

            @RequestParam(defaultValue = "20")
            @Min(value = 0, message = "Size must be >= 0")
            @Max(value = 50, message = "Size must be <= 50")
            int size) {

        return maintenanceLogService.getSpacecraftMaintenanceHistory(id, page, size)
                .map(response ->
                        ResponseEntity.ok()
                        .header("X-Total-Count", String.valueOf(response.totalElements()))
                        .body(response));
    }
}
