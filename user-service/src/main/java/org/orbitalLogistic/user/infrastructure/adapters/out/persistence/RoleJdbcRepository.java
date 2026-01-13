package org.orbitalLogistic.user.infrastructure.adapters.out.persistence;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleJdbcRepository extends CrudRepository<RoleEntity, Long> {
    
    Optional<RoleEntity> findByName(String name);
    
    boolean existsByName(String name);
}
