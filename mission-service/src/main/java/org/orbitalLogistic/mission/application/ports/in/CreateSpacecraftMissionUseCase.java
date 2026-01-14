package org.orbitalLogistic.mission.application.ports.in;

import org.orbitalLogistic.mission.domain.model.SpacecraftMission;

public interface CreateSpacecraftMissionUseCase {
    SpacecraftMission createSpacecraftMission(SpacecraftMission spacecraftMission);
}
