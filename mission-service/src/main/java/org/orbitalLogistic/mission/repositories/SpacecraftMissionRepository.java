package org.orbitalLogistic.mission.repositories;

import org.orbitalLogistic.mission.entities.SpacecraftMission;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpacecraftMissionRepository extends CrudRepository<SpacecraftMission, Long> {

    List<SpacecraftMission> findBySpacecraftId(Long spacecraftId);

    List<SpacecraftMission> findByMissionId(Long missionId);
}

