package org.orbitalLogistic.cargo.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.orbitalLogistic.cargo.dto.common.PageResponseDTO;
import org.orbitalLogistic.cargo.dto.request.StorageUnitRequestDTO;
import org.orbitalLogistic.cargo.dto.response.CargoStorageResponseDTO;
import org.orbitalLogistic.cargo.dto.response.StorageUnitResponseDTO;
import org.orbitalLogistic.cargo.services.StorageUnitService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/storage-units")
@RequiredArgsConstructor
@Validated
public class StorageUnitController {

    private final StorageUnitService storageUnitService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or #request.username == authentication.name")
    public ResponseEntity<PageResponseDTO<StorageUnitResponseDTO>> getAllStorageUnits(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        if (size > 50) size = 50;

        PageResponseDTO<StorageUnitResponseDTO> response = storageUnitService.getStorageUnits(page, size);
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(response.totalElements()))
                .body(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #request.username == authentication.name")
    public ResponseEntity<StorageUnitResponseDTO> getStorageUnitById(@PathVariable Long id) {
        StorageUnitResponseDTO response = storageUnitService.getStorageUnitById(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS_OFFICER')")
    public ResponseEntity<StorageUnitResponseDTO> createStorageUnit(@Valid @RequestBody StorageUnitRequestDTO request) {
        StorageUnitResponseDTO response = storageUnitService.createStorageUnit(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS_OFFICER')")
    public ResponseEntity<StorageUnitResponseDTO> updateStorageUnit(
            @PathVariable Long id,
            @Valid @RequestBody StorageUnitRequestDTO request) {

        StorageUnitResponseDTO response = storageUnitService.updateStorageUnit(id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/inventory")
    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS_OFFICER', 'MISSION_COMMANDER')")
    public ResponseEntity<PageResponseDTO<CargoStorageResponseDTO>> getStorageUnitInventory(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        if (size > 50) size = 50;

        PageResponseDTO<CargoStorageResponseDTO> response = storageUnitService.getStorageUnitInventory(id, page, size);
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(response.totalElements()))
                .body(response);
    }

    @GetMapping("/{id}/exists")
    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS_OFFICER', 'MISSION_COMMANDER')")
    public ResponseEntity<Boolean> storageUnitExists(@PathVariable Long id) {
        boolean exists = storageUnitService.storageUnitExists(id);
        return ResponseEntity.ok(exists);
    }
}
