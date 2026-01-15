package org.orbitalLogistic.cargo.infrastructure.adapters.in.rest.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.orbitalLogistic.cargo.application.ports.in.CreateCargoCategoryUseCase;
import org.orbitalLogistic.cargo.application.ports.in.GetCargoCategoryUseCase;
import org.orbitalLogistic.cargo.domain.exception.CargoCategoryNotFoundException;
import org.orbitalLogistic.cargo.domain.model.CargoCategory;
import org.orbitalLogistic.cargo.infrastructure.adapters.in.rest.dto.request.CreateCargoCategoryRequest;
import org.orbitalLogistic.cargo.infrastructure.adapters.in.rest.dto.response.CargoCategoryResponse;
import org.orbitalLogistic.cargo.infrastructure.adapters.in.rest.mapper.CargoCategoryRestMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cargo-categories")
@RequiredArgsConstructor
@Slf4j
public class CargoCategoryRestController {

    private final GetCargoCategoryUseCase getCargoCategoryUseCase;
    private final CreateCargoCategoryUseCase createCargoCategoryUseCase;
    private final CargoCategoryRestMapper categoryMapper;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS_OFFICER')")
    public ResponseEntity<CargoCategoryResponse> createCategory(
            @Valid @RequestBody CreateCargoCategoryRequest request) {
        log.debug("Creating cargo category with name: {}", request.getName());
        
        CargoCategory category = categoryMapper.toDomain(request);
        CargoCategory created = createCargoCategoryUseCase.createCategory(category);
        CargoCategoryResponse response = categoryMapper.toResponse(created);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CargoCategoryResponse> getCategoryById(@PathVariable Long id) {
        log.debug("Getting cargo category by id: {}", id);
        
        CargoCategory category = getCargoCategoryUseCase.getCategoryById(id)
                .orElseThrow(() -> new CargoCategoryNotFoundException("Cargo category not found with id: " + id));
        CargoCategoryResponse response = categoryMapper.toResponse(category);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<CargoCategoryResponse>> getAllCategories() {
        log.debug("Getting all cargo categories");
        
        List<CargoCategory> categories = getCargoCategoryUseCase.getAllCategories();
        List<CargoCategoryResponse> response = categories.stream()
                .map(categoryMapper::toResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/tree")
    public ResponseEntity<List<CargoCategoryResponse>> getCategoryTree() {
        log.debug("Getting cargo category tree");
        
        List<CargoCategory> rootCategories = getCargoCategoryUseCase.getCategoryTree();
        List<CargoCategoryResponse> response = rootCategories.stream()
                .map(categoryMapper::toResponseWithChildren)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }
}
