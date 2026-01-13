package org.orbitalLogistic.spacecraft.infrastructure.adapters.in.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.orbitalLogistic.spacecraft.application.ports.in.CreateSpacecraftTypeCommand;
import org.orbitalLogistic.spacecraft.application.ports.in.CreateSpacecraftTypeUseCase;
import org.orbitalLogistic.spacecraft.application.ports.in.GetSpacecraftTypesUseCase;
import org.orbitalLogistic.spacecraft.application.ports.in.UpdateSpacecraftTypeCommand;
import org.orbitalLogistic.spacecraft.application.ports.in.UpdateSpacecraftTypeUseCase;
import org.orbitalLogistic.spacecraft.domain.model.SpacecraftType;
import org.orbitalLogistic.spacecraft.infrastructure.adapters.in.rest.dto.PageResponseDTO;
import org.orbitalLogistic.spacecraft.infrastructure.adapters.in.rest.dto.SpacecraftTypeRequestDTO;
import org.orbitalLogistic.spacecraft.infrastructure.adapters.in.rest.dto.SpacecraftTypeResponseDTO;
import org.orbitalLogistic.spacecraft.infrastructure.adapters.in.rest.mapper.SpacecraftTypeRestMapper;
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
@RequestMapping("/api/spacecraft-types")
@RequiredArgsConstructor
public class SpacecraftTypeRestController {

    private final CreateSpacecraftTypeUseCase createSpacecraftTypeUseCase;
    private final UpdateSpacecraftTypeUseCase updateSpacecraftTypeUseCase;
    private final GetSpacecraftTypesUseCase getSpacecraftTypesUseCase;
    private final SpacecraftTypeRestMapper spacecraftTypeRestMapper;

    @GetMapping
    public Mono<ResponseEntity<PageResponseDTO<SpacecraftTypeResponseDTO>>> getAllSpacecraftTypes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        if (size > 50) size = 50;

        int finalSize = size;
        return Mono.fromCallable(() -> {
            int offset = page * finalSize;
            List<SpacecraftType> types = getSpacecraftTypesUseCase.getAllSpacecraftTypes(finalSize, offset);
            long total = getSpacecraftTypesUseCase.countAllSpacecraftTypes();

            List<SpacecraftTypeResponseDTO> typeDTOs = types.stream()
                    .map(spacecraftTypeRestMapper::toResponseDTO)
                    .toList();

            int totalPages = (int) Math.ceil((double) total / finalSize);
            PageResponseDTO<SpacecraftTypeResponseDTO> response = new PageResponseDTO<>(
                    typeDTOs, page, finalSize, total, totalPages, page == 0, page >= totalPages - 1
            );

            return ResponseEntity.ok()
                    .header("X-Total-Count", String.valueOf(total))
                    .body(response);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<SpacecraftTypeResponseDTO>> getSpacecraftTypeById(@PathVariable Long id) {
        return Mono.fromCallable(() -> {
            SpacecraftType spacecraftType = getSpacecraftTypesUseCase.getSpacecraftTypeById(id);
            SpacecraftTypeResponseDTO response = spacecraftTypeRestMapper.toResponseDTO(spacecraftType);
            return ResponseEntity.ok(response);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS_OFFICER')")
    public Mono<ResponseEntity<SpacecraftTypeResponseDTO>> createSpacecraftType(
            @Valid @RequestBody SpacecraftTypeRequestDTO request) {

        return Mono.fromCallable(() -> {
            CreateSpacecraftTypeCommand command = spacecraftTypeRestMapper.toCreateCommand(request);
            SpacecraftType spacecraftType = createSpacecraftTypeUseCase.createSpacecraftType(command);
            SpacecraftTypeResponseDTO response = spacecraftTypeRestMapper.toResponseDTO(spacecraftType);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LOGISTICS_OFFICER')")
    public Mono<ResponseEntity<SpacecraftTypeResponseDTO>> updateSpacecraftType(
            @PathVariable Long id,
            @Valid @RequestBody SpacecraftTypeRequestDTO request) {

        return Mono.fromCallable(() -> {
            UpdateSpacecraftTypeCommand command = spacecraftTypeRestMapper.toUpdateCommand(id, request);
            SpacecraftType spacecraftType = updateSpacecraftTypeUseCase.updateSpacecraftType(command);
            SpacecraftTypeResponseDTO response = spacecraftTypeRestMapper.toResponseDTO(spacecraftType);
            return ResponseEntity.ok(response);
        }).subscribeOn(Schedulers.boundedElastic());
    }
}

