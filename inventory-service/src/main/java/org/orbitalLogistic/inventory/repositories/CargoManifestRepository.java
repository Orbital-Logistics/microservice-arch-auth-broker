package org.orbitalLogistic.inventory.repositories;

import org.orbitalLogistic.inventory.entities.CargoManifest;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CargoManifestRepository extends CrudRepository<CargoManifest, Long> {

    List<CargoManifest> findBySpacecraftId(Long spacecraftId);

    List<CargoManifest> findByCargoId(Long cargoId);

    @Query("""
        SELECT m.* FROM cargo_manifest m
        ORDER BY m.loaded_at DESC NULLS LAST, m.id DESC
        LIMIT :limit OFFSET :offset
    """)
    List<CargoManifest> findAllPaginated(
        @Param("limit") int limit,
        @Param("offset") int offset
    );

    @Query("SELECT COUNT(*) FROM cargo_manifest")
    long countAll();

    @Query("""
        SELECT m.* FROM cargo_manifest m
        WHERE m.spacecraft_id = :spacecraftId
        ORDER BY m.loaded_at DESC NULLS LAST, m.id DESC
        LIMIT :limit OFFSET :offset
    """)
    List<CargoManifest> findBySpacecraftIdPaginated(
        @Param("spacecraftId") Long spacecraftId,
        @Param("limit") int limit,
        @Param("offset") int offset
    );

    @Query("""
        SELECT COUNT(*) FROM cargo_manifest
        WHERE spacecraft_id = :spacecraftId
    """)
    long countBySpacecraftId(@Param("spacecraftId") Long spacecraftId);
}

