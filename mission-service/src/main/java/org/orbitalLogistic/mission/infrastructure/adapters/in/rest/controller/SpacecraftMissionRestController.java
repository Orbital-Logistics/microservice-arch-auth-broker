package org.orbitalLogistic.mission.infrastructure.adapters.in.rest.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.orbitalLogistic.mission.application.ports.in.CreateSpacecraftMissionUseCase;
import org.orbitalLogistic.mission.application.ports.in.GetSpacecraftMissionsUseCase;
import org.orbitalLogistic.mission.domain.model.SpacecraftMission;
import org.orbitalLogistic.mission.infrastructure.adapters.in.rest.dto.request.SpacecraftMissionRequestDTO;
import org.orbitalLogistic.mission.infrastructure.adapters.in.rest.dto.response.SpacecraftMissionResponseDTO;
import org.orbitalLogistic.mission.infrastructure.adapters.in.rest.mapper.SpacecraftMissionDTOMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/spacecraft-missions")
@RequiredArgsConstructor
public class SpacecraftMissionRestController {

    private final GetSpacecraftMissionsUseCase getSpacecraftMissionsUseCase;
    private final CreateSpacecraftMissionUseCase createSpacecraftMissionUseCase;
    private final SpacecraftMissionDTOMapper spacecraftMissionMapper;

    @GetMapping
    public ResponseEntity<List<SpacecraftMissionResponseDTO>> getAllSpacecraftMissions() {
        List<SpacecraftMission> missions = getSpacecraftMissionsUseCase.getAllSpacecraftMissions();
        List<SpacecraftMissionResponseDTO> response = missions.stream()
                .map(spacecraftMissionMapper::toResponseDTO)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/spacecraft/{spacecraftId}")
    public ResponseEntity<List<SpacecraftMissionResponseDTO>> getBySpacecraft(@PathVariable Long spacecraftId) {
        List<SpacecraftMission> missions = getSpacecraftMissionsUseCase.getBySpacecraftId(spacecraftId);
        List<SpacecraftMissionResponseDTO> response = missions.stream()
                .map(spacecraftMissionMapper::toResponseDTO)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/mission/{missionId}")
    public ResponseEntity<List<SpacecraftMissionResponseDTO>> getByMission(@PathVariable Long missionId) {
        List<SpacecraftMission> missions = getSpacecraftMissionsUseCase.getByMissionId(missionId);
        List<SpacecraftMissionResponseDTO> response = missions.stream()
                .map(spacecraftMissionMapper::toResponseDTO)
                .toList();
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MISSION_COMMANDER')")
    public ResponseEntity<SpacecraftMissionResponseDTO> createSpacecraftMission(
            @Valid @RequestBody SpacecraftMissionRequestDTO request) {

        SpacecraftMission mission = SpacecraftMission.builder()
                .spacecraftId(request.spacecraftId())
                .missionId(request.missionId())
                .build();

        SpacecraftMission created = createSpacecraftMissionUseCase.createSpacecraftMission(mission);
        SpacecraftMissionResponseDTO response = spacecraftMissionMapper.toResponseDTO(created);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
