package org.orbitalLogistic.mission.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.orbitalLogistic.mission.dto.common.PageResponseDTO;
import org.orbitalLogistic.mission.dto.request.MissionRequestDTO;
import org.orbitalLogistic.mission.dto.response.MissionResponseDTO;
import org.orbitalLogistic.mission.entities.enums.MissionStatus;
import org.orbitalLogistic.mission.services.MissionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/missions")
@RequiredArgsConstructor
public class MissionController {

    private final MissionService missionService;
    


    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or #request.username == authentication.name")
    public ResponseEntity<PageResponseDTO<MissionResponseDTO>> getAllMissions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        if (size > 50) size = 50;

        PageResponseDTO<MissionResponseDTO> response = missionService.getAllMissions(page, size);

        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(response.totalElements()))
                .body(response);
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN') or #request.username == authentication.name")
    public ResponseEntity<PageResponseDTO<MissionResponseDTO>> searchMissions(
            @RequestParam(required = false) String missionCode,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String missionType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        if (size > 50) size = 50;

        PageResponseDTO<MissionResponseDTO> response = missionService.searchMissions(missionCode, status, missionType, page, size);

        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(response.totalElements()))
                .body(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MISSION_COMMANDER')")
    public ResponseEntity<MissionResponseDTO> getMissionById(@PathVariable Long id) {
        MissionResponseDTO response = missionService.getMissionById(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MISSION_COMMANDER')")
    public ResponseEntity<MissionResponseDTO> createMission(@Valid @RequestBody MissionRequestDTO request) {
        MissionResponseDTO response = missionService.createMission(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MISSION_COMMANDER')")
    public ResponseEntity<MissionResponseDTO> updateMission(
            @PathVariable Long id,
            @Valid @RequestBody MissionRequestDTO request) {
        MissionResponseDTO response = missionService.updateMission(id, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'MISSION_COMMANDER')")
    public ResponseEntity<MissionResponseDTO> updateMissionStatus(
            @PathVariable Long id,
            @RequestParam MissionStatus status) {
        MissionResponseDTO response = missionService.updateMissionStatus(id, status);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MISSION_COMMANDER')")
    public ResponseEntity<Void> deleteMission(@PathVariable Long id) {
        missionService.deleteMission(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/commander/{commanderId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MISSION_COMMANDER')")
    public ResponseEntity<List<MissionResponseDTO>> getMissionsByCommander(@PathVariable Long commanderId) {
        List<MissionResponseDTO> response = missionService.getMissionsByCommander(commanderId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/spacecraft/{spacecraftId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MISSION_COMMANDER')")
    public ResponseEntity<List<MissionResponseDTO>> getMissionsBySpacecraft(@PathVariable Long spacecraftId) {
        List<MissionResponseDTO> response = missionService.getMissionsBySpacecraft(spacecraftId);
        return ResponseEntity.ok(response);
    }
}

