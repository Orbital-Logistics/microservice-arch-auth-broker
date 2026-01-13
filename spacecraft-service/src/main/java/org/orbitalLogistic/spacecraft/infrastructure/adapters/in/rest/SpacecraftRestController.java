package org.orbitalLogistic.spacecraft.infrastructure.adapters.in.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.orbitalLogistic.spacecraft.application.ports.in.CreateSpacecraftCommand;
import org.orbitalLogistic.spacecraft.application.ports.in.CreateSpacecraftUseCase;
import org.orbitalLogistic.spacecraft.application.ports.in.GetSpacecraftsUseCase;
import org.orbitalLogistic.spacecraft.application.ports.in.UpdateSpacecraftCommand;
import org.orbitalLogistic.spacecraft.application.ports.in.UpdateSpacecraftStatusUseCase;
import org.orbitalLogistic.spacecraft.application.ports.in.UpdateSpacecraftUseCase;
import org.orbitalLogistic.spacecraft.domain.model.Spacecraft;
import org.orbitalLogistic.spacecraft.domain.model.enums.SpacecraftStatus;
import org.orbitalLogistic.spacecraft.infrastructure.adapters.in.rest.dto.PageResponseDTO;
import org.orbitalLogistic.spacecraft.infrastructure.adapters.in.rest.dto.SpacecraftRequestDTO;
import org.orbitalLogistic.spacecraft.infrastructure.adapters.in.rest.dto.SpacecraftResponseDTO;
import org.orbitalLogistic.spacecraft.infrastructure.adapters.in.rest.mapper.SpacecraftRestMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

@RestController
@RequestMapping("/api/spacecrafts")
@RequiredArgsConstructor
public class SpacecraftRestController {

    private final CreateSpacecraftUseCase createSpacecraftUseCase;
    private final UpdateSpacecraftUseCase updateSpacecraftUseCase;
    private final GetSpacecraftsUseCase getSpacecraftsUseCase;
    private final UpdateSpacecraftStatusUseCase updateSpacecraftStatusUseCase;
    private final SpacecraftRestMapper spacecraftRestMapper;

    @GetMapping
    public Mono<ResponseEntity<PageResponseDTO<SpacecraftResponseDTO>>> getAllSpacecrafts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        if (size > 50) size = 50;

        int finalSize = size;
        return Mono.fromCallable(() -> {
            int offset = page * finalSize;
            List<Spacecraft> spacecrafts = getSpacecraftsUseCase.getSpacecrafts(name, status, finalSize, offset);
            long total = getSpacecraftsUseCase.countSpacecrafts(name, status);

            List<SpacecraftResponseDTO> spacecraftDTOs = spacecrafts.stream()
                    .map(spacecraftRestMapper::toResponseDTO)
                    .toList();

            int totalPages = (int) Math.ceil((double) total / finalSize);
            PageResponseDTO<SpacecraftResponseDTO> response = new PageResponseDTO<>(
                    spacecraftDTOs, page, finalSize, total, totalPages, page == 0, page >= totalPages - 1
            );

            return ResponseEntity.ok()
                    .header("X-Total-Count", String.valueOf(total))
                    .body(response);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @GetMapping("/scroll")
    public Mono<ResponseEntity<List<SpacecraftResponseDTO>>> getSpacecraftsScroll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        if (size > 50) size = 50;

        int finalSize = size;
        return Mono.fromCallable(() -> {
            int offset = page * finalSize;
            List<Spacecraft> spacecrafts = getSpacecraftsUseCase.getSpacecrafts(null, null, finalSize + 1, offset);

            List<SpacecraftResponseDTO> spacecraftDTOs = spacecrafts.stream()
                    .limit(finalSize)
                    .map(spacecraftRestMapper::toResponseDTO)
                    .toList();

            return ResponseEntity.ok(spacecraftDTOs);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<SpacecraftResponseDTO>> getSpacecraftById(@PathVariable Long id) {
        return Mono.fromCallable(() -> {
            Spacecraft spacecraft = getSpacecraftsUseCase.getSpacecraftById(id);
            SpacecraftResponseDTO response = spacecraftRestMapper.toResponseDTO(spacecraft);
            return ResponseEntity.ok(response);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS_OFFICER')")
    public Mono<ResponseEntity<SpacecraftResponseDTO>> createSpacecraft(
            @Valid @RequestBody SpacecraftRequestDTO request) {

        return Mono.fromCallable(() -> {
            CreateSpacecraftCommand command = spacecraftRestMapper.toCreateCommand(request);
            Spacecraft spacecraft = createSpacecraftUseCase.createSpacecraft(command);
            SpacecraftResponseDTO response = spacecraftRestMapper.toResponseDTO(spacecraft);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS_OFFICER')")
    public Mono<ResponseEntity<SpacecraftResponseDTO>> updateSpacecraft(
            @PathVariable Long id,
            @Valid @RequestBody SpacecraftRequestDTO request) {

        return Mono.fromCallable(() -> {
            UpdateSpacecraftCommand command = spacecraftRestMapper.toUpdateCommand(id, request);
            Spacecraft spacecraft = updateSpacecraftUseCase.updateSpacecraft(command);
            SpacecraftResponseDTO response = spacecraftRestMapper.toResponseDTO(spacecraft);
            return ResponseEntity.ok(response);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @GetMapping("/available")
    public Mono<ResponseEntity<List<SpacecraftResponseDTO>>> getAvailableSpacecrafts() {
        return Mono.fromCallable(() -> {
            List<Spacecraft> spacecrafts = getSpacecraftsUseCase.getAvailableSpacecrafts();
            List<SpacecraftResponseDTO> response = spacecrafts.stream()
                    .map(spacecraftRestMapper::toResponseDTO)
                    .toList();
            return ResponseEntity.ok(response);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS_OFFICER')")
    public Mono<ResponseEntity<SpacecraftResponseDTO>> updateSpacecraftStatus(
            @PathVariable Long id,
            @RequestParam SpacecraftStatus status) {

        return Mono.fromCallable(() -> {
            Spacecraft spacecraft = updateSpacecraftStatusUseCase.updateStatus(id, status);
            SpacecraftResponseDTO response = spacecraftRestMapper.toResponseDTO(spacecraft);
            return ResponseEntity.ok(response);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @GetMapping("/{id}/exists")
    public Mono<ResponseEntity<Boolean>> spacecraftExists(@PathVariable Long id) {
        return Mono.fromCallable(() -> {
            boolean exists = getSpacecraftsUseCase.spacecraftExists(id);
            return ResponseEntity.ok(exists);
        }).subscribeOn(Schedulers.boundedElastic());
    }
}

