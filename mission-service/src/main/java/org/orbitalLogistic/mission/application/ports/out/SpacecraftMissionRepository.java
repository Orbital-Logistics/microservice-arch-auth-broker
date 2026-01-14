package org.orbitalLogistic.mission.application.ports.out;

import org.orbitalLogistic.mission.domain.model.SpacecraftMission;

import java.util.List;
import java.util.Optional;

public interface SpacecraftMissionRepository {
    SpacecraftMission save(SpacecraftMission spacecraftMission);
    Optional<SpacecraftMission> findById(Long id);
    List<SpacecraftMission> findAll();
    List<SpacecraftMission> findBySpacecraftId(Long spacecraftId);
    List<SpacecraftMission> findByMissionId(Long missionId);
    boolean existsById(Long id);
    boolean existsBySpacecraftIdAndMissionId(Long spacecraftId, Long missionId);
    void deleteById(Long id);
    void deleteAll();
}
