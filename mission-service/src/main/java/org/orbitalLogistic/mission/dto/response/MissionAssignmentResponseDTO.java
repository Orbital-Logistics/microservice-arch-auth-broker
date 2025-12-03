package org.orbitalLogistic.mission.dto.response;

import org.orbitalLogistic.mission.entities.enums.AssignmentRole;

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

