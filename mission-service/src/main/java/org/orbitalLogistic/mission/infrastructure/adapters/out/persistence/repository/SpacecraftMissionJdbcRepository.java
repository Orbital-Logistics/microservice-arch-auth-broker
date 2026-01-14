package org.orbitalLogistic.mission.infrastructure.adapters.out.persistence.repository;

import org.orbitalLogistic.mission.infrastructure.adapters.out.persistence.entity.SpacecraftMissionJpaEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpacecraftMissionJdbcRepository extends CrudRepository<SpacecraftMissionJpaEntity, Long> {

    List<SpacecraftMissionJpaEntity> findBySpacecraftId(Long spacecraftId);

    List<SpacecraftMissionJpaEntity> findByMissionId(Long missionId);

    boolean existsBySpacecraftIdAndMissionId(Long spacecraftId, Long missionId);
}
