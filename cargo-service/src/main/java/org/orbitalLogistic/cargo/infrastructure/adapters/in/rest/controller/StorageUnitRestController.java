package org.orbitalLogistic.cargo.infrastructure.adapters.in.rest.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.orbitalLogistic.cargo.application.ports.in.CreateStorageUnitUseCase;
import org.orbitalLogistic.cargo.application.ports.in.GetStorageUnitUseCase;
import org.orbitalLogistic.cargo.application.ports.in.UpdateStorageUnitUseCase;
import org.orbitalLogistic.cargo.domain.exception.StorageUnitNotFoundException;
import org.orbitalLogistic.cargo.domain.model.StorageUnit;
import org.orbitalLogistic.cargo.infrastructure.adapters.in.rest.dto.request.CreateStorageUnitRequest;
import org.orbitalLogistic.cargo.infrastructure.adapters.in.rest.dto.request.UpdateStorageUnitRequest;
import org.orbitalLogistic.cargo.infrastructure.adapters.in.rest.dto.response.StorageUnitResponse;
import org.orbitalLogistic.cargo.infrastructure.adapters.in.rest.mapper.StorageUnitRestMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/storage-units")
@RequiredArgsConstructor
@Slf4j
public class StorageUnitRestController {

    private final GetStorageUnitUseCase getStorageUnitUseCase;
    private final CreateStorageUnitUseCase createStorageUnitUseCase;
    private final UpdateStorageUnitUseCase updateStorageUnitUseCase;
    private final StorageUnitRestMapper unitMapper;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS_OFFICER')")
    public ResponseEntity<StorageUnitResponse> createUnit(@Valid @RequestBody CreateStorageUnitRequest request) {
        log.debug("Creating storage unit with code: {}", request.getUnitCode());
        
        StorageUnit unit = unitMapper.toDomain(request);
        StorageUnit created = createStorageUnitUseCase.createUnit(unit);
        StorageUnitResponse response = unitMapper.toResponse(created);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<StorageUnitResponse> getUnitById(@PathVariable Long id) {
        log.debug("Getting storage unit by id: {}", id);
        
        StorageUnit unit = getStorageUnitUseCase.getUnitById(id)
                .orElseThrow(() -> new StorageUnitNotFoundException("Storage unit not found with id: " + id));
        StorageUnitResponse response = unitMapper.toResponse(unit);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<StorageUnitResponse>> getAllUnits(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.debug("Getting all storage units - page: {}, size: {}", page, size);
        
        if (size > 50) size = 50;
        
        List<StorageUnit> units = getStorageUnitUseCase.getAllUnits(page, size);
        List<StorageUnitResponse> response = units.stream()
                .map(unitMapper::toResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS_OFFICER')")
    public ResponseEntity<StorageUnitResponse> updateUnit(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStorageUnitRequest request) {
        log.debug("Updating storage unit with id: {}", id);
        
        StorageUnit unit = unitMapper.toDomain(request, id);
        StorageUnit updated = updateStorageUnitUseCase.updateUnit(id, unit);
        StorageUnitResponse response = unitMapper.toResponse(updated);
        
        return ResponseEntity.ok(response);
    }
}
