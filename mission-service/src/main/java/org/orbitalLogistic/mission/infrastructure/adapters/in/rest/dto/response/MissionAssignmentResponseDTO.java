package org.orbitalLogistic.mission.infrastructure.adapters.in.rest.dto.response;

import org.orbitalLogistic.mission.domain.model.enums.AssignmentRole;

import java.time.LocalDateTime;

public record MissionAssignmentResponseDTO(
    Long id,
    Long missionId,
    String missionName,
    Long userId,
    String userName,
    LocalDateTime assignedAt,
    AssignmentRole assignmentRole,
    String responsibilityZone
) {}

