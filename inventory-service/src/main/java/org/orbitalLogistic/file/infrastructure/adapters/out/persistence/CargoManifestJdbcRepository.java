package org.orbitalLogistic.file.infrastructure.adapters.out.persistence;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CargoManifestJdbcRepository extends CrudRepository<CargoManifestEntity, Long> {

    @Query("""
        SELECT m.* FROM cargo_manifest m
        ORDER BY m.loaded_at DESC NULLS LAST, m.id DESC
        LIMIT :limit OFFSET :offset
    """)
    List<CargoManifestEntity> findAllPaginated(
            @Param("limit") int limit,
            @Param("offset") int offset
    );

    @Query("""
        SELECT m.* FROM cargo_manifest m
        WHERE m.spacecraft_id = :spacecraftId
        ORDER BY m.loaded_at DESC NULLS LAST, m.id DESC
        LIMIT :limit OFFSET :offset
    """)
    List<CargoManifestEntity> findBySpacecraftIdPaginated(
            @Param("spacecraftId") Long spacecraftId,
            @Param("limit") int limit,
            @Param("offset") int offset
    );

    @Query("SELECT COUNT(*) FROM cargo_manifest")
    long countAll();

    @Query("SELECT COUNT(*) FROM cargo_manifest WHERE spacecraft_id = :spacecraftId")
    long countBySpacecraftId(@Param("spacecraftId") Long spacecraftId);
}
