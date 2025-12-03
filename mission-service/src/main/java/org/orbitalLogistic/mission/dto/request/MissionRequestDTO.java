package org.orbitalLogistic.mission.dto.request;

import java.time.LocalDateTime;

import org.orbitalLogistic.mission.entities.enums.*;
import jakarta.validation.constraints.*;

public record MissionRequestDTO(
    LocalDateTime scheduledArrival,
    LocalDateTime scheduledDeparture,

    @NotNull(message = "Spacecraft ID is required")
    Long spacecraftId,

    @NotNull(message = "Commanding officer ID is required")
    Long commandingOfficerId,

    @NotNull(message = "Priority is required")
    MissionPriority priority,

    @NotNull(message = "Mission type is required")
    MissionType missionType,

    @Size(max = 200, message = "Mission name must not exceed 200 characters")
    @NotBlank(message = "Mission name is required")
    String missionName,

    @Size(max = 20, message = "Mission code must not exceed 20 characters")
    @NotBlank(message = "Mission code is required")
    String missionCode
) {}


