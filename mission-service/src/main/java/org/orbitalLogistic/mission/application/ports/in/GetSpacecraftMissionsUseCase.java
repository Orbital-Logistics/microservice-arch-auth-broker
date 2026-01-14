package org.orbitalLogistic.mission.application.ports.in;

import org.orbitalLogistic.mission.domain.model.SpacecraftMission;

import java.util.List;

public interface GetSpacecraftMissionsUseCase {
    List<SpacecraftMission> getAllSpacecraftMissions();
    List<SpacecraftMission> getBySpacecraftId(Long spacecraftId);
    List<SpacecraftMission> getByMissionId(Long missionId);
}
