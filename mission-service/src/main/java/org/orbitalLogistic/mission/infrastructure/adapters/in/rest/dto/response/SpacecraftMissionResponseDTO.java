package org.orbitalLogistic.mission.infrastructure.adapters.in.rest.dto.response;

public record SpacecraftMissionResponseDTO(
    Long id,
    Long spacecraftId,
    String spacecraftName,
    Long missionId,
    String missionName
) {}

