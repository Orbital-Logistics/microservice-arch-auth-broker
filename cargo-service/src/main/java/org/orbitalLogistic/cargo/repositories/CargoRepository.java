package org.orbitalLogistic.cargo.repositories;

import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.jdbc.repository.query.Query;
import org.orbitalLogistic.cargo.entities.Cargo;

import java.util.Optional;
import java.util.List;

@Repository
public interface CargoRepository extends CrudRepository<Cargo, Long> {

    Optional<Cargo> findByName(String name);
    boolean existsByName(String name);
    List<Cargo> findByCargoCategoryId(Long categoryId);

    @Query("""
        SELECT c.* FROM cargo c
        WHERE (CAST(:name AS TEXT) IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', CAST(:name AS TEXT), '%')))
        AND (CAST(:cargoType AS TEXT) IS NULL OR CAST(c.cargo_type AS TEXT) = CAST(:cargoType AS TEXT))
        AND (CAST(:hazardLevel AS TEXT) IS NULL OR CAST(c.hazard_level AS TEXT) = CAST(:hazardLevel AS TEXT))
        ORDER BY c.id
        LIMIT :limit OFFSET :offset
    """)
    List<Cargo> findWithFilters(
            @Param("name") String name,
            @Param("cargoType") String cargoType,
            @Param("hazardLevel") String hazardLevel,
            @Param("limit") int limit,
            @Param("offset") int offset
    );

    @Query("""
            SELECT COUNT(*) FROM cargo c
            WHERE (CAST(:name AS TEXT) IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', CAST(:name AS TEXT), '%')))
            AND (CAST(:cargoType AS TEXT) IS NULL OR CAST(c.cargo_type AS TEXT) = CAST(:cargoType AS TEXT))
            AND (CAST(:hazardLevel AS TEXT) IS NULL OR CAST(c.hazard_level AS TEXT) = CAST(:hazardLevel AS TEXT))
        """)
    long countWithFilters(
            @Param("name") String name,
            @Param("cargoType") String cargoType,
            @Param("hazardLevel") String hazardLevel
    );
}


