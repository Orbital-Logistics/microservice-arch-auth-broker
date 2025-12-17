package org.orbitalLogistic.maintenance.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.orbitalLogistic.maintenance.dto.common.PageResponseDTO;
import org.orbitalLogistic.maintenance.dto.request.MaintenanceLogRequestDTO;
import org.orbitalLogistic.maintenance.dto.response.MaintenanceLogResponseDTO;
import org.orbitalLogistic.maintenance.services.MaintenanceLogService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class MaintenanceLogController {

    private final MaintenanceLogService maintenanceLogService;

    @GetMapping("/maintenance-logs")
    public Mono<ResponseEntity<PageResponseDTO<MaintenanceLogResponseDTO>>> getAllMaintenanceLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        if (size > 50) size = 50;

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
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        if (size > 50) size = 50;

        return maintenanceLogService.getSpacecraftMaintenanceHistory(id, page, size)
                .map(response ->
                        ResponseEntity.ok()
                        .header("X-Total-Count", String.valueOf(response.totalElements()))
                        .body(response));
    }
}
