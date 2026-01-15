package org.orbitalLogistic.cargo.application.ports.out;

import org.orbitalLogistic.cargo.domain.model.StorageUnit;

import java.util.List;
import java.util.Optional;

public interface StorageUnitRepository {
    StorageUnit save(StorageUnit storageUnit);
    Optional<StorageUnit> findById(Long id);
    List<StorageUnit> findAll();
    Optional<StorageUnit> findByUnitCode(String unitCode);
    boolean existsByUnitCode(String unitCode);
    List<StorageUnit> findByLocation(String location, int limit, int offset);
    long countByLocation(String location);
    void deleteById(Long id);
    boolean existsById(Long id);
}
