package org.orbitalLogistic.spacecraft.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.orbitalLogistic.spacecraft.dto.common.PageResponseDTO;
import org.orbitalLogistic.spacecraft.dto.request.SpacecraftRequestDTO;
import org.orbitalLogistic.spacecraft.dto.response.SpacecraftResponseDTO;
import org.orbitalLogistic.spacecraft.entities.enums.SpacecraftStatus;
import org.orbitalLogistic.spacecraft.services.SpacecraftService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/spacecrafts")
@RequiredArgsConstructor
public class SpacecraftController {

    private final SpacecraftService spacecraftService;

    @GetMapping
    public Mono<ResponseEntity<PageResponseDTO<SpacecraftResponseDTO>>> getAllSpacecrafts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        if (size > 50) {
            size = 50;
        }

        return spacecraftService.getSpacecrafts(name, status, page, size)
                .map(response ->
                        ResponseEntity.ok()
                                .header("X-Total-Count", String.valueOf(response.totalElements()))
                                .body(response)
                );
    }

    @GetMapping("/scroll")
    public Mono<ResponseEntity<List<SpacecraftResponseDTO>>> getSpacecraftsScroll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        if (size > 50) {
            size = 50;
        }

        return spacecraftService.getSpacecraftsScroll(page, size)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<SpacecraftResponseDTO>> getSpacecraftById(@PathVariable Long id) {
        return spacecraftService.getSpacecraftById(id)
                .map(ResponseEntity::ok);
    }

    @PostMapping
    public Mono<ResponseEntity<SpacecraftResponseDTO>> createSpacecraft(@Valid @RequestBody SpacecraftRequestDTO request) {
        return spacecraftService.createSpacecraft(request)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<SpacecraftResponseDTO>> updateSpacecraft(
            @PathVariable Long id,
            @Valid @RequestBody SpacecraftRequestDTO request) {

        return spacecraftService.updateSpacecraft(id, request)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/available")
    public Mono<ResponseEntity<List<SpacecraftResponseDTO>>> getAvailableSpacecrafts() {
        return spacecraftService.getAvailableSpacecrafts()
                .map(ResponseEntity::ok);
    }

    @PutMapping("/{id}/status")
    public Mono<ResponseEntity<SpacecraftResponseDTO>> updateSpacecraftStatus(
            @PathVariable Long id,
            @RequestParam SpacecraftStatus status) {

        return spacecraftService.updateSpacecraftStatus(id, status)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/{id}/exists")
    public Mono<ResponseEntity<Boolean>> spacecraftExists(@PathVariable Long id) {
        return spacecraftService.spacecraftExists(id)
                .map(ResponseEntity::ok);
    }
}
