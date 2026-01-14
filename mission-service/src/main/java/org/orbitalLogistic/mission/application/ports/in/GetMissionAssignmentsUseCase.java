package org.orbitalLogistic.mission.application.ports.in;

import org.orbitalLogistic.mission.domain.model.MissionAssignment;

import java.util.List;
import java.util.Optional;

public interface GetMissionAssignmentsUseCase {
    Optional<MissionAssignment> getById(Long id);
    List<MissionAssignment> getByMissionId(Long missionId);
    List<MissionAssignment> getByUserId(Long userId);
    List<MissionAssignment> searchAssignments(Long missionId, Long userId, int page, int size);
    long countAssignments(Long missionId, Long userId);
}
