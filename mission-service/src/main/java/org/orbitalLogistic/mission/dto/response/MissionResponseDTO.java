package org.orbitalLogistic.mission.dto.response;

import org.orbitalLogistic.mission.entities.enums.*;

import java.time.LocalDateTime;

public record MissionResponseDTO(
    Long id,
    String missionCode,
    String missionName,
    MissionType missionType,
    MissionStatus status,
    MissionPriority priority,
    Long commandingOfficerId,
    String commanderName,
    Long spacecraftId,
    String spacecraftName,
    LocalDateTime scheduledDeparture,
    LocalDateTime scheduledArrival,
    Integer assignedCrewCount
) {}

