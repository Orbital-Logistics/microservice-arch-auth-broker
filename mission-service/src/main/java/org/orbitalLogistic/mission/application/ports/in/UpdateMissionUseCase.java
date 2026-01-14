package org.orbitalLogistic.mission.application.ports.in;

import org.orbitalLogistic.mission.domain.model.Mission;
import org.orbitalLogistic.mission.domain.model.enums.MissionStatus;

public interface UpdateMissionUseCase {
    Mission updateMission(UpdateMissionCommand command);
    Mission updateMissionStatus(Long id, MissionStatus status);
}
