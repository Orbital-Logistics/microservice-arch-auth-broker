package org.orbitalLogistic.mission.dto.request;

import jakarta.validation.constraints.*;
import org.orbitalLogistic.mission.entities.enums.AssignmentRole;

public record MissionAssignmentRequestDTO(
    @NotNull(message = "Mission ID is required")
    Long missionId,

    @NotNull(message = "User ID is required")
    Long userId,

    @NotNull(message = "Assignment role is required")
    AssignmentRole assignmentRole,

    @Size(max = 100, message = "Responsibility zone must not exceed 100 characters")
    String responsibilityZone
) {}

