package org.orbitalLogistic.mission.repositories;

import org.orbitalLogistic.mission.entities.Mission;
import org.orbitalLogistic.mission.entities.enums.MissionStatus;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MissionRepository extends CrudRepository<Mission, Long> {

    Optional<Mission> findByMissionCode(String missionCode);

    boolean existsByMissionCode(String missionCode);

    List<Mission> findByStatus(MissionStatus status);

    List<Mission> findByCommandingOfficerId(Long commandingOfficerId);

    List<Mission> findBySpacecraftId(Long spacecraftId);

    @Query("""
        SELECT m.* FROM mission m
        WHERE (CAST(:missionCode AS TEXT) IS NULL OR LOWER(m.mission_code) LIKE LOWER(CONCAT('%', CAST(:missionCode AS TEXT), '%')))
        AND (CAST(:status AS TEXT) IS NULL OR CAST(m.status AS TEXT) = CAST(:status AS TEXT))
        AND (CAST(:missionType AS TEXT) IS NULL OR CAST(m.mission_type AS TEXT) = CAST(:missionType AS TEXT))
        ORDER BY m.scheduled_departure DESC NULLS LAST, m.id DESC
        LIMIT :limit OFFSET :offset
        """)
    List<Mission> findWithFilters(
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

