package org.orbitalLogistic.mission.infrastructure.adapters.out.persistence.repository;

import org.orbitalLogistic.mission.domain.model.enums.MissionStatus;
import org.orbitalLogistic.mission.infrastructure.adapters.out.persistence.entity.MissionJpaEntity;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MissionJdbcRepository extends CrudRepository<MissionJpaEntity, Long> {

    Optional<MissionJpaEntity> findByMissionCode(String missionCode);

    boolean existsByMissionCode(String missionCode);

    List<MissionJpaEntity> findByStatus(MissionStatus status);

    List<MissionJpaEntity> findByCommandingOfficerId(Long commandingOfficerId);

    List<MissionJpaEntity> findBySpacecraftId(Long spacecraftId);

    @Query("""
        SELECT m.* FROM mission m
        WHERE (CAST(:missionCode AS TEXT) IS NULL OR LOWER(m.mission_code) LIKE LOWER(CONCAT('%', CAST(:missionCode AS TEXT), '%')))
        AND (CAST(:status AS TEXT) IS NULL OR CAST(m.status AS TEXT) = CAST(:status AS TEXT))
        AND (CAST(:missionType AS TEXT) IS NULL OR CAST(m.mission_type AS TEXT) = CAST(:missionType AS TEXT))
        ORDER BY m.scheduled_departure DESC NULLS LAST, m.id DESC
        LIMIT :limit OFFSET :offset
        """)
    List<MissionJpaEntity> findWithFilters(
        @Param("missionCode") String missionCode,
        @Param("status") String status,
        @Param("missionType") String missionType,
        @Param("limit") int limit,
        @Param("offset") int offset
    );

    @Query("""
        SELECT COUNT(*) FROM mission m
        WHERE (CAST(:missionCode AS TEXT) IS NULL OR LOWER(m.mission_code) LIKE LOWER(CONCAT('%', CAST(:missionCode AS TEXT), '%')))
        AND (CAST(:status AS TEXT) IS NULL OR CAST(m.status AS TEXT) = CAST(:status AS TEXT))
        AND (CAST(:missionType AS TEXT) IS NULL OR CAST(m.mission_type AS TEXT) = CAST(:missionType AS TEXT))
        """)
    long countWithFilters(
        @Param("missionCode") String missionCode,
        @Param("status") String status,
        @Param("missionType") String missionType
    );
}
