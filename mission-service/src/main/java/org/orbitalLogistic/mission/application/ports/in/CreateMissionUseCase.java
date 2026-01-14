package org.orbitalLogistic.mission.application.ports.in;

import org.orbitalLogistic.mission.domain.model.Mission;

public interface CreateMissionUseCase {
    Mission createMission(CreateMissionCommand command);
}
