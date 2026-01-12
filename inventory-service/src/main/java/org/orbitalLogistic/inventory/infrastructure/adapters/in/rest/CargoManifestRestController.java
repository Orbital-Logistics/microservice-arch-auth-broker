package org.orbitalLogistic.inventory.infrastructure.adapters.in.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.orbitalLogistic.inventory.application.ports.in.CreateCargoManifestCommand;
import org.orbitalLogistic.inventory.application.ports.in.CreateCargoManifestUseCase;
import org.orbitalLogistic.inventory.application.ports.in.GetCargoManifestsUseCase;
import org.orbitalLogistic.inventory.application.ports.in.UpdateCargoManifestCommand;
import org.orbitalLogistic.inventory.application.ports.in.UpdateCargoManifestUseCase;
import org.orbitalLogistic.inventory.domain.model.CargoManifest;
import org.orbitalLogistic.inventory.infrastructure.adapters.in.rest.dto.CargoManifestRequestDTO;
import org.orbitalLogistic.inventory.infrastructure.adapters.in.rest.dto.PageResponseDTO;
import org.orbitalLogistic.inventory.infrastructure.adapters.in.rest.dto.CargoManifestResponseDTO;
import org.orbitalLogistic.inventory.infrastructure.adapters.in.rest.mapper.CargoManifestRestMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cargo-manifests")
@RequiredArgsConstructor
public class CargoManifestRestController {

    private final CreateCargoManifestUseCase createCargoManifestUseCase;
    private final GetCargoManifestsUseCase getCargoManifestsUseCase;
    private final UpdateCargoManifestUseCase updateCargoManifestUseCase;
    private final CargoManifestRestMapper cargoManifestRestMapper;

    @GetMapping
    public ResponseEntity<PageResponseDTO<CargoManifestResponseDTO>> getAllManifests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        if (size > 50) size = 50;

        List<CargoManifest> manifests = getCargoManifestsUseCase.getAllManifests(page, size);
        long total = getCargoManifestsUseCase.countAllManifests();

        List<CargoManifestResponseDTO> manifestDTOs = manifests.stream()
                .map(cargoManifestRestMapper::toResponseDTO)
                .toList();

        int totalPages = (int) Math.ceil((double) total / size);
        PageResponseDTO<CargoManifestResponseDTO> response = new PageResponseDTO<>(
                manifestDTOs, page, size, total, totalPages, page == 0, page >= totalPages - 1
        );

        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(total))
                .body(response);
    }

    @GetMapping("/spacecraft/{spacecraftId}")
    public ResponseEntity<PageResponseDTO<CargoManifestResponseDTO>> getManifestsBySpacecraft(
            @PathVariable Long spacecraftId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        if (size > 50) size = 50;

        List<CargoManifest> manifests = getCargoManifestsUseCase.getManifestsBySpacecraft(spacecraftId, page, size);
        long total = getCargoManifestsUseCase.countManifestsBySpacecraft(spacecraftId);

        List<CargoManifestResponseDTO> manifestDTOs = manifests.stream()
                .map(cargoManifestRestMapper::toResponseDTO)
                .toList();

        int totalPages = (int) Math.ceil((double) total / size);
        PageResponseDTO<CargoManifestResponseDTO> response = new PageResponseDTO<>(
                manifestDTOs, page, size, total, totalPages, page == 0, page >= totalPages - 1
        );

        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(total))
                .body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CargoManifestResponseDTO> getManifestById(@PathVariable Long id) {
        CargoManifest manifest = getCargoManifestsUseCase.getManifestById(id);
        CargoManifestResponseDTO response = cargoManifestRestMapper.toResponseDTO(manifest);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS_OFFICER')")
    public ResponseEntity<CargoManifestResponseDTO> createManifest(
            @Valid @RequestBody CargoManifestRequestDTO request) {

        CreateCargoManifestCommand command = cargoManifestRestMapper.toCreateCommand(request);
        CargoManifest manifest = createCargoManifestUseCase.createManifest(command);
        CargoManifestResponseDTO response = cargoManifestRestMapper.toResponseDTO(manifest);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS_OFFICER')")
    public ResponseEntity<CargoManifestResponseDTO> updateManifest(
            @PathVariable Long id,
            @Valid @RequestBody CargoManifestRequestDTO request) {

        UpdateCargoManifestCommand command = cargoManifestRestMapper.toUpdateCommand(id, request);
        CargoManifest manifest = updateCargoManifestUseCase.updateManifest(command);
        CargoManifestResponseDTO response = cargoManifestRestMapper.toResponseDTO(manifest);

        return ResponseEntity.ok(response);
    }
}
