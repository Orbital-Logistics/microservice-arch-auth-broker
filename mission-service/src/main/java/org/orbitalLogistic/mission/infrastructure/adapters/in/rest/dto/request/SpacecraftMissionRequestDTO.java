package org.orbitalLogistic.mission.infrastructure.adapters.in.rest.dto.request;

import jakarta.validation.constraints.*;

public record SpacecraftMissionRequestDTO(
    @NotNull(message = "Spacecraft ID is required")
    Long spacecraftId,

    @NotNull(message = "Mission ID is required")
    Long missionId
) {}

