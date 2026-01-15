package org.orbitalLogistic.cargo.infrastructure.adapters.out.persistence.repository;

import org.orbitalLogistic.cargo.infrastructure.adapters.out.persistence.entity.CargoEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CargoJdbcRepository extends CrudRepository<CargoEntity, Long> {
    
    List<CargoEntity> findByName(String name);
    
    boolean existsByName(String name);
}
