package org.orbitalLogistic.mission.infrastructure.adapters.out.persistence.repository;

import org.orbitalLogistic.mission.infrastructure.adapters.out.persistence.entity.MissionAssignmentJpaEntity;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MissionAssignmentJdbcRepository extends CrudRepository<MissionAssignmentJpaEntity, Long> {

    List<MissionAssignmentJpaEntity> findByMissionId(Long missionId);

    List<MissionAssignmentJpaEntity> findByUserId(Long userId);

    List<MissionAssignmentJpaEntity> findByMissionIdAndUserId(Long missionId, Long userId);

    @Query("""
        SELECT ma.* FROM mission_assignment ma
        WHERE (:missionId IS NULL OR ma.mission_id = :missionId)
        AND (:userId IS NULL OR ma.user_id = :userId)
        ORDER BY ma.assigned_at DESC
        LIMIT :limit OFFSET :offset
        """)
    List<MissionAssignmentJpaEntity> findWithFilters(
        @Param("missionId") Long missionId,
        @Param("userId") Long userId,
        @Param("limit") int limit,
        @Param("offset") int offset
    );

    @Query("""
        SELECT COUNT(*) FROM mission_assignment ma
        WHERE (:missionId IS NULL OR ma.mission_id = :missionId)
        AND (:userId IS NULL OR ma.user_id = :userId)
        """)
    long countWithFilters(
        @Param("missionId") Long missionId,
        @Param("userId") Long userId
    );

    @Query("SELECT COUNT(*) FROM mission_assignment WHERE mission_id = :missionId")
    Integer countByMissionId(@Param("missionId") Long missionId);

    boolean existsByMissionIdAndUserId(Long missionId, Long userId);
}
