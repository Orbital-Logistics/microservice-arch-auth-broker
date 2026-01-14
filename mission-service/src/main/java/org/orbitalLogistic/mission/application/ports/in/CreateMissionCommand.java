package org.orbitalLogistic.mission.application.ports.in;

import org.orbitalLogistic.mission.domain.model.enums.MissionPriority;
import org.orbitalLogistic.mission.domain.model.enums.MissionType;

import java.time.LocalDateTime;

public record CreateMissionCommand(
    String missionCode,
    String missionName,
    MissionType missionType,
    MissionPriority priority,
    Long commandingOfficerId,
    Long spacecraftId,
    LocalDateTime scheduledDeparture,
    LocalDateTime scheduledArrival
) {}
