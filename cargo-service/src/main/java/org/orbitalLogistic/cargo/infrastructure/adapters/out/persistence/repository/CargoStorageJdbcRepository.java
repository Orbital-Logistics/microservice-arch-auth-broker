package org.orbitalLogistic.cargo.infrastructure.adapters.out.persistence.repository;

import org.orbitalLogistic.cargo.infrastructure.adapters.out.persistence.entity.CargoStorageEntity;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CargoStorageJdbcRepository extends CrudRepository<CargoStorageEntity, Long> {
    
    List<CargoStorageEntity> findByStorageUnitId(Long storageUnitId);
    
    List<CargoStorageEntity> findByCargoId(Long cargoId);
    
    @Query("SELECT SUM(quantity) FROM cargo_storage WHERE cargo_id = :cargoId")
    Integer sumQuantityByCargoId(@Param("cargoId") Long cargoId);
}
