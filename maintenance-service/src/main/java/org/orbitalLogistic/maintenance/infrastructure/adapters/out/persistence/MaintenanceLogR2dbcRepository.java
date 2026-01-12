package org.orbitalLogistic.maintenance.infrastructure.adapters.out.persistence;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface MaintenanceLogR2dbcRepository extends ReactiveCrudRepository<MaintenanceLogEntity, Long> {

    Flux<MaintenanceLogEntity> findBySpacecraftId(Long spacecraftId);

    @Query("""
        SELECT m.* FROM maintenance_log m
        ORDER BY m.start_time DESC NULLS LAST, m.id DESC
        LIMIT :limit OFFSET :offset
    """)
    Flux<MaintenanceLogEntity> findAllPaginated(
            @Param("offset") int offset,
            @Param("limit") int limit
    );

    @Query("SELECT COUNT(*) FROM maintenance_log")
    Mono<Long> countAll();

    @Query("""
        SELECT m.* FROM maintenance_log m
        WHERE m.spacecraft_id = :spacecraftId
        ORDER BY m.start_time DESC NULLS LAST, m.id DESC
        LIMIT :limit OFFSET :offset
    """)
    Flux<MaintenanceLogEntity> findBySpacecraftIdPaginated(
            @Param("spacecraftId") Long spacecraftId,
            @Param("limit") int limit,
            @Param("offset") int offset
    );

    @Query("""
        SELECT COUNT(*) FROM maintenance_log
        WHERE spacecraft_id = :spacecraftId
    """)
    Mono<Long> countBySpacecraftId(@Param("spacecraftId") Long spacecraftId);
}
