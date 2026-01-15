package org.orbitalLogistic.cargo.infrastructure.adapters.in.rest.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.orbitalLogistic.cargo.application.ports.in.CreateCargoUseCase;
import org.orbitalLogistic.cargo.application.ports.in.DeleteCargoUseCase;
import org.orbitalLogistic.cargo.application.ports.in.GetCargoUseCase;
import org.orbitalLogistic.cargo.application.ports.in.UpdateCargoUseCase;
import org.orbitalLogistic.cargo.domain.exception.CargoNotFoundException;
import org.orbitalLogistic.cargo.domain.model.Cargo;
import org.orbitalLogistic.cargo.domain.model.enums.CargoType;
import org.orbitalLogistic.cargo.domain.model.enums.HazardLevel;
import org.orbitalLogistic.cargo.infrastructure.adapters.in.rest.dto.request.CreateCargoRequest;
import org.orbitalLogistic.cargo.infrastructure.adapters.in.rest.dto.request.UpdateCargoRequest;
import org.orbitalLogistic.cargo.infrastructure.adapters.in.rest.dto.response.CargoResponse;
import org.orbitalLogistic.cargo.infrastructure.adapters.in.rest.mapper.CargoRestMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cargos")
@RequiredArgsConstructor
@Slf4j
public class CargoRestController {

    private final GetCargoUseCase getCargoUseCase;
    private final CreateCargoUseCase createCargoUseCase;
    private final UpdateCargoUseCase updateCargoUseCase;
    private final DeleteCargoUseCase deleteCargoUseCase;
    private final CargoRestMapper cargoMapper;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS_OFFICER')")
    public ResponseEntity<CargoResponse> createCargo(@Valid @RequestBody CreateCargoRequest request) {
        log.debug("Creating cargo with name: {}", request.getName());
        
        Cargo cargo = cargoMapper.toDomain(request);
        Cargo created = createCargoUseCase.createCargo(cargo);
        CargoResponse response = cargoMapper.toResponse(created);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CargoResponse> getCargoById(@PathVariable Long id) {
        log.debug("Getting cargo by id: {}", id);
        
        Cargo cargo = getCargoUseCase.getCargoById(id)
                .orElseThrow(() -> new CargoNotFoundException("Cargo not found with id: " + id));
        CargoResponse response = cargoMapper.toResponse(cargo);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<CargoResponse>> getAllCargos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.debug("Getting all cargos - page: {}, size: {}", page, size);
        
        if (size > 50) size = 50;
        
        List<Cargo> cargos = getCargoUseCase.getAllCargos(page, size);
        List<CargoResponse> response = cargos.stream()
                .map(cargoMapper::toResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<List<CargoResponse>> searchCargos(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) CargoType cargoType,
            @RequestParam(required = false) HazardLevel hazardLevel,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.debug("Searching cargos - name: {}, cargoType: {}, hazardLevel: {}, page: {}, size: {}",
                name, cargoType, hazardLevel, page, size);
        
        if (size > 50) size = 50;
        
        List<Cargo> cargos = getCargoUseCase.searchCargos(name, cargoType, hazardLevel, page, size);
        List<CargoResponse> response = cargos.stream()
                .map(cargoMapper::toResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS_OFFICER')")
    public ResponseEntity<CargoResponse> updateCargo(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCargoRequest request) {
        log.debug("Updating cargo with id: {}", id);
        
        Cargo cargo = cargoMapper.toDomain(request, id);
        Cargo updated = updateCargoUseCase.updateCargo(id, cargo);
        CargoResponse response = cargoMapper.toResponse(updated);
        
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS_OFFICER')")
    public ResponseEntity<Void> deleteCargo(@PathVariable Long id) {
        log.debug("Deleting cargo with id: {}", id);
        
        deleteCargoUseCase.deleteCargo(id);
        
        return ResponseEntity.noContent().build();
    }
}
