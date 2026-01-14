package org.orbitalLogistic.mission.application.ports.out;

import org.orbitalLogistic.mission.domain.model.MissionAssignment;

import java.util.List;
import java.util.Optional;

public interface MissionAssignmentRepository {
    MissionAssignment save(MissionAssignment assignment);
    Optional<MissionAssignment> findById(Long id);
    List<MissionAssignment> findAll();
    List<MissionAssignment> findByMissionId(Long missionId);
    List<MissionAssignment> findByUserId(Long userId);
    List<MissionAssignment> findByMissionIdAndUserId(Long missionId, Long userId);
    List<MissionAssignment> findWithFilters(Long missionId, Long userId, int limit, int offset);
    long countWithFilters(Long missionId, Long userId);
    int countByMissionId(Long missionId);
    boolean existsById(Long id);
    void deleteById(Long id);
    void deleteAll();
}
