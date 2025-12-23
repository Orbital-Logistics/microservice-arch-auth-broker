package org.orbitalLogistic.spacecraft.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.orbitalLogistic.spacecraft.dto.request.SpacecraftTypeRequestDTO;
import org.orbitalLogistic.spacecraft.dto.response.SpacecraftTypeResponseDTO;
import org.orbitalLogistic.spacecraft.services.SpacecraftTypeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/spacecraft-types")
@RequiredArgsConstructor
public class SpacecraftTypeController {

    private final SpacecraftTypeService spacecraftTypeService;

    @GetMapping
    public Mono<ResponseEntity<List<SpacecraftTypeResponseDTO>>> getAllSpacecraftTypes() {
        return spacecraftTypeService.getAllSpacecraftTypes()
                .map(ResponseEntity::ok);
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<SpacecraftTypeResponseDTO>> getSpacecraftTypeById(@PathVariable Long id) {
        return spacecraftTypeService.getSpacecraftTypeById(id)
                .map(ResponseEntity::ok);
    }

    @PostMapping
    public Mono<ResponseEntity<SpacecraftTypeResponseDTO>> createSpacecraftType(@Valid @RequestBody SpacecraftTypeRequestDTO request) {
        return spacecraftTypeService.createSpacecraftType(request)
                .map(response ->
                        ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(response)
                );
    }
}

