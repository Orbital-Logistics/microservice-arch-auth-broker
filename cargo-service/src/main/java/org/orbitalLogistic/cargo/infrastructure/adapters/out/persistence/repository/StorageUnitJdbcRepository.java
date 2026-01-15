package org.orbitalLogistic.cargo.infrastructure.adapters.out.persistence.repository;

import org.orbitalLogistic.cargo.infrastructure.adapters.out.persistence.entity.StorageUnitEntity;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StorageUnitJdbcRepository extends CrudRepository<StorageUnitEntity, Long> {
    
    Optional<StorageUnitEntity> findByUnitCode(String unitCode);
    
    boolean existsByUnitCode(String unitCode);
    
    List<StorageUnitEntity> findByLocation(String location);
    
    @Query("SELECT COUNT(*) FROM storage_unit WHERE location = :location")
    int countByLocation(@Param("location") String location);
}
