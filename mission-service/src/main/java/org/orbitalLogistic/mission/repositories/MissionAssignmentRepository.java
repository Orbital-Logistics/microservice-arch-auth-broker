package org.orbitalLogistic.mission.repositories;

import org.orbitalLogistic.mission.entities.MissionAssignment;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MissionAssignmentRepository extends CrudRepository<MissionAssignment, Long> {

    List<MissionAssignment> findByMissionId(Long missionId);

    List<MissionAssignment> findByUserId(Long userId);

    @Query("SELECT COUNT(*) FROM mission_assignment WHERE mission_id = :missionId")
    int countByMissionId(@Param("missionId") Long missionId);

    @Query("""
        SELECT ma.* FROM mission_assignment ma
        WHERE mission_id = :missionId AND user_id = :userId
    """)
    List<MissionAssignment> findByMissionIdAndUserId(
        @Param("missionId") Long missionId,
        @Param("userId") Long userId
    );

    @Query("""
        SELECT ma.* FROM mission_assignment ma
        WHERE (CAST(:missionId AS BIGINT) IS NULL OR ma.mission_id = CAST(:missionId AS BIGINT))
        AND (CAST(:userId AS BIGINT) IS NULL OR ma.user_id = CAST(:userId AS BIGINT))
        ORDER BY ma.assigned_at DESC
        LIMIT :limit OFFSET :offset
    """)
    List<MissionAssignment> findWithFilters(
        @Param("missionId") Long missionId,
        @Param("userId") Long userId,
        @Param("limit") int limit,
        @Param("offset") int offset
    );

    @Query("""
        SELECT COUNT(*) FROM mission_assignment ma
        WHERE (CAST(:missionId AS BIGINT) IS NULL OR ma.mission_id = CAST(:missionId AS BIGINT))
        AND (CAST(:userId AS BIGINT) IS NULL OR ma.user_id = CAST(:userId AS BIGINT))
    """)
    long countWithFilters(
        @Param("missionId") Long missionId,
        @Param("userId") Long userId
    );
}

