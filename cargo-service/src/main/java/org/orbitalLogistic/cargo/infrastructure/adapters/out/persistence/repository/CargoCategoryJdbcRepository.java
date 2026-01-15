package org.orbitalLogistic.cargo.infrastructure.adapters.out.persistence.repository;

import org.orbitalLogistic.cargo.infrastructure.adapters.out.persistence.entity.CargoCategoryEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CargoCategoryJdbcRepository extends CrudRepository<CargoCategoryEntity, Long> {
    
    List<CargoCategoryEntity> findByParentCategoryIdIsNull();
    
    List<CargoCategoryEntity> findByParentCategoryId(Long parentId);
}
