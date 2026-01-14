package org.orbitalLogistic.mission.application.ports.out;

import org.orbitalLogistic.mission.domain.model.Mission;
import org.orbitalLogistic.mission.domain.model.enums.MissionStatus;

import java.util.List;
import java.util.Optional;

public interface MissionRepository {
    Mission save(Mission mission);
    Optional<Mission> findById(Long id);
    Optional<Mission> findByMissionCode(String missionCode);
    List<Mission> findAll();
    List<Mission> findByStatus(MissionStatus status);
    List<Mission> findByCommandingOfficerId(Long commandingOfficerId);
    List<Mission> findBySpacecraftId(Long spacecraftId);
    List<Mission> findWithFilters(String missionCode, String status, String missionType, int limit, int offset);
    long countWithFilters(String missionCode, String status, String missionType);
    boolean existsById(Long id);
    boolean existsByMissionCode(String missionCode);
    void deleteById(Long id);
    void deleteAll();
}
