package org.orbitalLogistic.mission.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.orbitalLogistic.mission.dto.common.PageResponseDTO;
import org.orbitalLogistic.mission.dto.request.MissionAssignmentRequestDTO;
import org.orbitalLogistic.mission.dto.response.MissionAssignmentResponseDTO;
import org.orbitalLogistic.mission.services.MissionAssignmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mission-assignments")
@RequiredArgsConstructor
public class MissionAssignmentController {

    private final MissionAssignmentService missionAssignmentService;
    private final CircuitBreakerRegistry registry;

    @GetMapping("/get-cb-config")
    public Map<String, Object> getConfigCB(@RequestParam String name){
        CircuitBreaker cb = registry.circuitBreaker(name);
        if (cb == null) {
            return Map.of("error", "CircuitBreaker not found: " + name);
        }   
        
        return Map.of(
            "name", name,
            "state", cb.getState(),
            "config", Map.of(
                "failureRateThreshold", cb.getCircuitBreakerConfig().getFailureRateThreshold(),
                "slidingWindowSize", cb.getCircuitBreakerConfig().getSlidingWindowSize(),
                "minimumNumberOfCalls", cb.getCircuitBreakerConfig().getMinimumNumberOfCalls(),
                "waitDurationInOpenState", cb.getCircuitBreakerConfig().getWaitIntervalFunctionInOpenState(),
                "permittedNumberOfCallsInHalfOpenState", cb.getCircuitBreakerConfig().getPermittedNumberOfCallsInHalfOpenState(),
                "slidingWindowType", cb.getCircuitBreakerConfig().getSlidingWindowType()
            )
        );
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or #request.username == authentication.name")
    public ResponseEntity<PageResponseDTO<MissionAssignmentResponseDTO>> getAllAssignments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        if (size > 50) size = 50;

        PageResponseDTO<MissionAssignmentResponseDTO> response = missionAssignmentService.getAllAssignments(page, size);
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(response.totalElements()))
                .body(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #request.username == authentication.name")
    public ResponseEntity<MissionAssignmentResponseDTO> getAssignmentById(@PathVariable Long id) {
        MissionAssignmentResponseDTO response = missionAssignmentService.getAssignmentById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/mission/{missionId}")
    @PreAuthorize("hasRole('ADMIN') or #request.username == authentication.name")
    public ResponseEntity<List<MissionAssignmentResponseDTO>> getAssignmentsByMission(@PathVariable Long missionId) {
        List<MissionAssignmentResponseDTO> response = missionAssignmentService.getAssignmentsByMission(missionId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or #request.username == authentication.name")
    public ResponseEntity<List<MissionAssignmentResponseDTO>> getAssignmentsByUser(@PathVariable Long userId) {
        List<MissionAssignmentResponseDTO> response = missionAssignmentService.getAssignmentsByUser(userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MISSION_COMMANDER')")
    public ResponseEntity<MissionAssignmentResponseDTO> createAssignment(@Valid @RequestBody MissionAssignmentRequestDTO request) {
        MissionAssignmentResponseDTO response = missionAssignmentService.createAssignment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MISSION_COMMANDER')")
    public ResponseEntity<Void> deleteAssignment(@PathVariable Long id) {
        missionAssignmentService.deleteAssignment(id);
        return ResponseEntity.noContent().build();
    }
}

