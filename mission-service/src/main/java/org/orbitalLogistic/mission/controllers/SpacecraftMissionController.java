package org.orbitalLogistic.mission.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.orbitalLogistic.mission.dto.request.SpacecraftMissionRequestDTO;
import org.orbitalLogistic.mission.dto.response.SpacecraftMissionResponseDTO;
import org.orbitalLogistic.mission.services.SpacecraftMissionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/spacecraft-missions")
@RequiredArgsConstructor
public class SpacecraftMissionController {

    private final SpacecraftMissionService spacecraftMissionService;

    @GetMapping
    public ResponseEntity<List<SpacecraftMissionResponseDTO>> getAllSpacecraftMissions() {
        List<SpacecraftMissionResponseDTO> response = spacecraftMissionService.getAllSpacecraftMissions();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/spacecraft/{spacecraftId}")
    public ResponseEntity<List<SpacecraftMissionResponseDTO>> getBySpacecraft(@PathVariable Long spacecraftId) {
        List<SpacecraftMissionResponseDTO> response = spacecraftMissionService.getBySpacecraft(spacecraftId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/mission/{missionId}")
    public ResponseEntity<List<SpacecraftMissionResponseDTO>> getByMission(@PathVariable Long missionId) {
        List<SpacecraftMissionResponseDTO> response = spacecraftMissionService.getByMission(missionId);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<SpacecraftMissionResponseDTO> createSpacecraftMission(@Valid @RequestBody SpacecraftMissionRequestDTO request) {
        SpacecraftMissionResponseDTO response = spacecraftMissionService.createSpacecraftMission(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}

