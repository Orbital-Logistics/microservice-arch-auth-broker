package org.orbitalLogistic.mission.infrastructure.adapters.in.rest.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.orbitalLogistic.mission.application.ports.in.CreateMissionAssignmentUseCase;
import org.orbitalLogistic.mission.application.ports.in.DeleteMissionAssignmentUseCase;
import org.orbitalLogistic.mission.application.ports.in.GetMissionAssignmentsUseCase;
import org.orbitalLogistic.mission.domain.exception.MissionAssignmentNotFoundException;
import org.orbitalLogistic.mission.domain.model.MissionAssignment;
import org.orbitalLogistic.mission.infrastructure.adapters.in.rest.dto.request.MissionAssignmentRequestDTO;
import org.orbitalLogistic.mission.infrastructure.adapters.in.rest.dto.response.MissionAssignmentResponseDTO;
import org.orbitalLogistic.mission.infrastructure.adapters.in.rest.mapper.MissionAssignmentDTOMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mission-assignments")
@RequiredArgsConstructor
public class MissionAssignmentRestController {

    private final GetMissionAssignmentsUseCase getMissionAssignmentsUseCase;
    private final CreateMissionAssignmentUseCase createMissionAssignmentUseCase;
    private final DeleteMissionAssignmentUseCase deleteMissionAssignmentUseCase;
    private final MissionAssignmentDTOMapper missionAssignmentMapper;

    @GetMapping
    public ResponseEntity<List<MissionAssignmentResponseDTO>> searchAssignments(
            @RequestParam(required = false) Long missionId,
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<MissionAssignment> assignments = getMissionAssignmentsUseCase.searchAssignments(missionId, userId, page, size);
        List<MissionAssignmentResponseDTO> response = assignments.stream()
                .map(missionAssignmentMapper::toResponseDTO)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MissionAssignmentResponseDTO> getAssignmentById(@PathVariable Long id) {
        MissionAssignment assignment = getMissionAssignmentsUseCase.getById(id)
                .orElseThrow(() -> new MissionAssignmentNotFoundException("Mission assignment not found with id: " + id));
        MissionAssignmentResponseDTO response = missionAssignmentMapper.toResponseDTO(assignment);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/mission/{missionId}")
    public ResponseEntity<List<MissionAssignmentResponseDTO>> getAssignmentsByMission(@PathVariable Long missionId) {
        List<MissionAssignment> assignments = getMissionAssignmentsUseCase.getByMissionId(missionId);
        List<MissionAssignmentResponseDTO> response = assignments.stream()
                .map(missionAssignmentMapper::toResponseDTO)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<MissionAssignmentResponseDTO>> getAssignmentsByUser(@PathVariable Long userId) {
        List<MissionAssignment> assignments = getMissionAssignmentsUseCase.getByUserId(userId);
        List<MissionAssignmentResponseDTO> response = assignments.stream()
                .map(missionAssignmentMapper::toResponseDTO)
                .toList();
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MISSION_COMMANDER')")
    public ResponseEntity<MissionAssignmentResponseDTO> createAssignment(
            @Valid @RequestBody MissionAssignmentRequestDTO request) {

        MissionAssignment assignment = MissionAssignment.builder()
                .missionId(request.missionId())
                .userId(request.userId())
                .assignedAt(java.time.LocalDateTime.now())
                .assignmentRole(request.assignmentRole())
                .responsibilityZone(request.responsibilityZone())
                .build();

        MissionAssignment created = createMissionAssignmentUseCase.createAssignment(assignment);
        MissionAssignmentResponseDTO response = missionAssignmentMapper.toResponseDTO(created);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MISSION_COMMANDER')")
    public ResponseEntity<Void> deleteAssignment(@PathVariable Long id) {
        deleteMissionAssignmentUseCase.deleteAssignment(id);
        return ResponseEntity.noContent().build();
    }
}
