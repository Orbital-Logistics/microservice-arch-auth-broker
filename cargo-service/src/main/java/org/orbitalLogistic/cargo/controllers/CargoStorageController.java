package org.orbitalLogistic.cargo.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.orbitalLogistic.cargo.dto.common.PageResponseDTO;
import org.orbitalLogistic.cargo.dto.request.CargoStorageRequestDTO;
import org.orbitalLogistic.cargo.dto.response.CargoStorageResponseDTO;
import org.orbitalLogistic.cargo.services.CargoStorageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CargoStorageController {

    private final CargoStorageService cargoStorageService;

    @GetMapping("/cargo-storage")
    @PreAuthorize("hasRole('ADMIN') or #request.username == authentication.name")
    public ResponseEntity<PageResponseDTO<CargoStorageResponseDTO>> getAllCargoStorage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        PageResponseDTO<CargoStorageResponseDTO> response = cargoStorageService.getAllCargoStorage(page, size);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/cargo-storage")
    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS_OFFICER')")
    public ResponseEntity<CargoStorageResponseDTO> addCargoToStorage(
            @Valid @RequestBody CargoStorageRequestDTO request) {

        CargoStorageResponseDTO response = cargoStorageService.addCargoToStorage(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/cargo-storage/{id}/quantity")
    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS_OFFICER')")
    public ResponseEntity<CargoStorageResponseDTO> updateCargoQuantity(
            @PathVariable Long id,
            @Valid @RequestBody CargoStorageRequestDTO request) {

        CargoStorageResponseDTO response = cargoStorageService.updateQuantity(id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/storage-units/{id}/storage")
    @PreAuthorize("hasRole('ADMIN') or #request.username == authentication.name")
    public ResponseEntity<PageResponseDTO<CargoStorageResponseDTO>> getStorageUnitCargo(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        PageResponseDTO<CargoStorageResponseDTO> response = cargoStorageService.getStorageUnitCargo(id, page, size);
        return ResponseEntity.ok(response);
    }
}
