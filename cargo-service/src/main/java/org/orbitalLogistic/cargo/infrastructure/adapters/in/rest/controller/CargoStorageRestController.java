package org.orbitalLogistic.cargo.infrastructure.adapters.in.rest.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.orbitalLogistic.cargo.application.ports.in.CreateCargoStorageUseCase;
import org.orbitalLogistic.cargo.application.ports.in.DeleteCargoStorageUseCase;
import org.orbitalLogistic.cargo.application.ports.in.GetCargoStorageUseCase;
import org.orbitalLogistic.cargo.application.ports.in.UpdateCargoStorageUseCase;
import org.orbitalLogistic.cargo.domain.exception.CargoStorageNotFoundException;
import org.orbitalLogistic.cargo.domain.model.CargoStorage;
import org.orbitalLogistic.cargo.infrastructure.adapters.in.rest.dto.request.CreateCargoStorageRequest;
import org.orbitalLogistic.cargo.infrastructure.adapters.in.rest.dto.request.UpdateInventoryRequest;
import org.orbitalLogistic.cargo.infrastructure.adapters.in.rest.dto.response.CargoStorageResponse;
import org.orbitalLogistic.cargo.infrastructure.adapters.in.rest.mapper.CargoStorageRestMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cargo-storages")
@RequiredArgsConstructor
@Slf4j
public class CargoStorageRestController {

    private final GetCargoStorageUseCase getCargoStorageUseCase;
    private final CreateCargoStorageUseCase createCargoStorageUseCase;
    private final UpdateCargoStorageUseCase updateCargoStorageUseCase;
    private final DeleteCargoStorageUseCase deleteCargoStorageUseCase;
    private final CargoStorageRestMapper storageMapper;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS_OFFICER')")
    public ResponseEntity<CargoStorageResponse> createStorage(
            @Valid @RequestBody CreateCargoStorageRequest request) {
        log.debug("Creating cargo storage for cargoId: {}, storageUnitId: {}",
                request.getCargoId(), request.getStorageUnitId());
        
        CargoStorage storage = storageMapper.toDomain(request);
        CargoStorage created = createCargoStorageUseCase.createStorage(storage);
        CargoStorageResponse response = storageMapper.toResponse(created);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CargoStorageResponse> getStorageById(@PathVariable Long id) {
        log.debug("Getting cargo storage by id: {}", id);
        
        CargoStorage storage = getCargoStorageUseCase.getStorageById(id)
                .orElseThrow(() -> new CargoStorageNotFoundException("Cargo storage not found with id: " + id));
        CargoStorageResponse response = storageMapper.toResponse(storage);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<CargoStorageResponse>> getAllStorages(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.debug("Getting all cargo storages - page: {}, size: {}", page, size);
        
        if (size > 50) size = 50;
        
        List<CargoStorage> storages = getCargoStorageUseCase.getAllStorages(page, size);
        List<CargoStorageResponse> response = storages.stream()
                .map(storageMapper::toResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/quantity")
    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS_OFFICER')")
    public ResponseEntity<CargoStorageResponse> updateQuantity(
            @PathVariable Long id,
            @Valid @RequestBody UpdateInventoryRequest request) {
        log.debug("Updating quantity for storage id: {} with new quantity: {}", id, request.getNewQuantity());
        
        CargoStorage updated = updateCargoStorageUseCase.updateInventory(id, request.getNewQuantity());
        CargoStorageResponse response = storageMapper.toResponse(updated);
        
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStorage(@PathVariable Long id) {
        log.debug("Deleting cargo storage with id: {}", id);
        
        deleteCargoStorageUseCase.deleteStorage(id);
        
        return ResponseEntity.noContent().build();
    }
}
