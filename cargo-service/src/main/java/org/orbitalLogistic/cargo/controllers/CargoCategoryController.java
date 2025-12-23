package org.orbitalLogistic.cargo.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.orbitalLogistic.cargo.dto.request.CargoCategoryRequestDTO;
import org.orbitalLogistic.cargo.dto.response.CargoCategoryResponseDTO;
import org.orbitalLogistic.cargo.services.CargoCategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cargo-categories")
@RequiredArgsConstructor
public class CargoCategoryController {

    private final CargoCategoryService cargoCategoryService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or #request.username == authentication.name")
    public ResponseEntity<List<CargoCategoryResponseDTO>> getAllCategories() {
        List<CargoCategoryResponseDTO> response = cargoCategoryService.getAllCategories();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #request.username == authentication.name")
    public ResponseEntity<CargoCategoryResponseDTO> getCategoryById(@PathVariable Long id) {
        CargoCategoryResponseDTO response = cargoCategoryService.getCategoryById(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS_OFFICER')")
    public ResponseEntity<CargoCategoryResponseDTO> createCategory(@Valid @RequestBody CargoCategoryRequestDTO request) {
        CargoCategoryResponseDTO response = cargoCategoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/tree")
    @PreAuthorize("hasRole('ADMIN') or #request.username == authentication.name")
    public ResponseEntity<List<CargoCategoryResponseDTO>> getCategoryTree() {
        List<CargoCategoryResponseDTO> response = cargoCategoryService.getCategoryTree();
        return ResponseEntity.ok(response);
    }
}

