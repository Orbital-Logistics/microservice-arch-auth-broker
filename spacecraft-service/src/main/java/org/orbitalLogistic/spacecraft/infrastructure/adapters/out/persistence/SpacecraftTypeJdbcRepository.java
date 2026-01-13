package org.orbitalLogistic.spacecraft.infrastructure.adapters.out.persistence;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpacecraftTypeJdbcRepository extends CrudRepository<SpacecraftTypeEntity, Long> {

    @Query("SELECT * FROM spacecraft_type ORDER BY id LIMIT :limit OFFSET :offset")
    List<SpacecraftTypeEntity> findAllPaginated(@Param("limit") int limit, @Param("offset") int offset);

    @Query("SELECT COUNT(*) FROM spacecraft_type")
    long countAll();
}

