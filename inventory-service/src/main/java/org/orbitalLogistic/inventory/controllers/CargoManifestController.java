package org.orbitalLogistic.inventory.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.orbitalLogistic.inventory.dto.common.PageResponseDTO;
import org.orbitalLogistic.inventory.dto.request.CargoManifestRequestDTO;
import org.orbitalLogistic.inventory.dto.response.CargoManifestResponseDTO;
import org.orbitalLogistic.inventory.services.CargoManifestService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cargo-manifests")
@RequiredArgsConstructor
public class CargoManifestController {

    private final CargoManifestService cargoManifestService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or #request.username == authentication.name")
    public ResponseEntity<PageResponseDTO<CargoManifestResponseDTO>> getAllManifests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        if (size > 50) size = 50;

        PageResponseDTO<CargoManifestResponseDTO> response = cargoManifestService.getAllManifests(page, size);
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(response.totalElements()))
                .body(response);
    }

    @GetMapping("/spacecraft/{spacecraftId}")
    @PreAuthorize("hasRole('ADMIN') or #request.username == authentication.name")
    public ResponseEntity<PageResponseDTO<CargoManifestResponseDTO>> getManifestsBySpacecraft(
            @PathVariable Long spacecraftId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        if (size > 50) size = 50;

        PageResponseDTO<CargoManifestResponseDTO> response =
                cargoManifestService.getManifestsBySpacecraft(spacecraftId, page, size);
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(response.totalElements()))
                .body(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #request.username == authentication.name")
    public ResponseEntity<CargoManifestResponseDTO> getManifestById(@PathVariable Long id) {
        CargoManifestResponseDTO response = cargoManifestService.getManifestById(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS_OFFICER')")
    public ResponseEntity<CargoManifestResponseDTO> createManifest(
            @Valid @RequestBody CargoManifestRequestDTO request) {

        CargoManifestResponseDTO response = cargoManifestService.createManifest(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS_OFFICER')")
    public ResponseEntity<CargoManifestResponseDTO> updateManifest(
            @PathVariable Long id,
            @Valid @RequestBody CargoManifestRequestDTO request) {

        CargoManifestResponseDTO response = cargoManifestService.updateManifest(id, request);
        return ResponseEntity.ok(response);
    }
}

