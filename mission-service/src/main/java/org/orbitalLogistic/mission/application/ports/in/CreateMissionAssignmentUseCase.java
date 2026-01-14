package org.orbitalLogistic.mission.application.ports.in;

import org.orbitalLogistic.mission.domain.model.MissionAssignment;

public interface CreateMissionAssignmentUseCase {
    MissionAssignment createAssignment(MissionAssignment assignment);
}
