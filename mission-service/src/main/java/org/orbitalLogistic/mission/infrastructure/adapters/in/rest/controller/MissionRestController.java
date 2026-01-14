package org.orbitalLogistic.mission.infrastructure.adapters.in.rest.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.orbitalLogistic.mission.application.ports.in.*;
import org.orbitalLogistic.mission.domain.exception.MissionNotFoundException;
import org.orbitalLogistic.mission.domain.model.Mission;
import org.orbitalLogistic.mission.domain.model.enums.MissionStatus;
import org.orbitalLogistic.mission.infrastructure.adapters.in.rest.dto.common.PageResponseDTO;
import org.orbitalLogistic.mission.infrastructure.adapters.in.rest.dto.request.MissionRequestDTO;
import org.orbitalLogistic.mission.infrastructure.adapters.in.rest.dto.response.MissionResponseDTO;
import org.orbitalLogistic.mission.infrastructure.adapters.in.rest.mapper.MissionDTOMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/missions")
@RequiredArgsConstructor
public class MissionRestController {

    private final GetMissionsUseCase getMissionsUseCase;
    private final GetMissionByIdUseCase getMissionByIdUseCase;
    private final CreateMissionUseCase createMissionUseCase;
    private final UpdateMissionUseCase updateMissionUseCase;
    private final DeleteMissionUseCase deleteMissionUseCase;
    private final SearchMissionsUseCase searchMissionsUseCase;
    private final MissionDTOMapper missionMapper;

    @GetMapping
    public ResponseEntity<PageResponseDTO<MissionResponseDTO>> getAllMissions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request) {

        if (size > 50) size = 50;

        List<Mission> missions = getMissionsUseCase.getAllMissions();
        long total = searchMissionsUseCase.countMissions(null, null, null);

        List<MissionResponseDTO> missionDTOs = missions.stream()
                .map(missionMapper::toResponseDTO)
                .toList();

        int totalPages = (int) Math.ceil((double) total / size);
        PageResponseDTO<MissionResponseDTO> response = new PageResponseDTO<>(
                missionDTOs, page, size, total, totalPages, page == 0, page >= totalPages - 1
        );

        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(total))
                .body(response);
    }

    @GetMapping("/search")
    public ResponseEntity<PageResponseDTO<MissionResponseDTO>> searchMissions(
            @RequestParam(required = false) String missionCode,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String missionType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request) {

        if (size > 50) size = 50;

        List<Mission> missions = searchMissionsUseCase.searchMissions(missionCode, status, missionType, page, size);
        long total = searchMissionsUseCase.countMissions(missionCode, status, missionType);

        List<MissionResponseDTO> missionDTOs = missions.stream()
                .map(missionMapper::toResponseDTO)
                .toList();

        int totalPages = (int) Math.ceil((double) total / size);
        PageResponseDTO<MissionResponseDTO> response = new PageResponseDTO<>(
                missionDTOs, page, size, total, totalPages, page == 0, page >= totalPages - 1
        );

        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(total))
                .body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MissionResponseDTO> getMissionById(
            @PathVariable Long id,
            HttpServletRequest request) {

        Mission mission = getMissionByIdUseCase.getMissionById(id)
                .orElseThrow(() -> new MissionNotFoundException("Mission not found with id: " + id));
        MissionResponseDTO response = missionMapper.toResponseDTO(mission);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MISSION_COMMANDER')")
    public ResponseEntity<MissionResponseDTO> createMission(
            @Valid @RequestBody MissionRequestDTO request,
            HttpServletRequest request_http) {

        CreateMissionCommand command = new CreateMissionCommand(
                request.missionCode(),
                request.missionName(),
                request.missionType(),
                request.priority(),
                request.commandingOfficerId(),
                request.spacecraftId(),
                request.scheduledDeparture(),
                request.scheduledArrival()
        );

        Mission mission = createMissionUseCase.createMission(command);
        MissionResponseDTO response = missionMapper.toResponseDTO(mission);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MISSION_COMMANDER')")
    public ResponseEntity<MissionResponseDTO> updateMission(
            @PathVariable Long id,
            @Valid @RequestBody MissionRequestDTO request) {

        UpdateMissionCommand command = new UpdateMissionCommand(
                id,
                request.missionCode(),
                request.missionName(),
                request.missionType(),
                request.priority(),
                request.commandingOfficerId(),
                request.spacecraftId(),
                request.scheduledDeparture(),
                request.scheduledArrival()
        );

        Mission mission = updateMissionUseCase.updateMission(command);
        MissionResponseDTO response = missionMapper.toResponseDTO(mission);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'MISSION_COMMANDER')")
    public ResponseEntity<MissionResponseDTO> updateMissionStatus(
            @PathVariable Long id,
            @RequestParam MissionStatus status) {

        Mission mission = updateMissionUseCase.updateMissionStatus(id, status);
        MissionResponseDTO response = missionMapper.toResponseDTO(mission);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MISSION_COMMANDER')")
    public ResponseEntity<Void> deleteMission(@PathVariable Long id) {
        deleteMissionUseCase.deleteMission(id);
        return ResponseEntity.noContent().build();
    }
}
