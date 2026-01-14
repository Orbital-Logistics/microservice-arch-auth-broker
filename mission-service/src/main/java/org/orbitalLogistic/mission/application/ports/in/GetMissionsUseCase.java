package org.orbitalLogistic.mission.application.ports.in;

import org.orbitalLogistic.mission.domain.model.Mission;
import org.orbitalLogistic.mission.domain.model.enums.MissionStatus;

import java.util.List;

public interface GetMissionsUseCase {
    List<Mission> getAllMissions();
    List<Mission> getMissionsByStatus(MissionStatus status);
    List<Mission> getMissionsByCommandingOfficerId(Long commandingOfficerId);
    List<Mission> getMissionsBySpacecraftId(Long spacecraftId);
}
