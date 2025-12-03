package org.orbitalLogistic.mission.dto.response;

public record SpacecraftMissionResponseDTO(
    Long id,
    Long spacecraftId,
    String spacecraftName,
    Long missionId,
    String missionName
) {}

