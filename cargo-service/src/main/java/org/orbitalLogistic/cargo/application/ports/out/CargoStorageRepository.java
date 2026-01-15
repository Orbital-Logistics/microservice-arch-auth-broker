package org.orbitalLogistic.cargo.application.ports.out;

import org.orbitalLogistic.cargo.domain.model.CargoStorage;

import java.util.List;
import java.util.Optional;

public interface CargoStorageRepository {
    CargoStorage save(CargoStorage cargoStorage);
    Optional<CargoStorage> findById(Long id);
    List<CargoStorage> findAll();
    List<CargoStorage> findByStorageUnitId(Long storageUnitId);
    List<CargoStorage> findByCargoId(Long cargoId);
    void deleteById(Long id);
    boolean existsById(Long id);
    List<CargoStorage> findWithFilters(Long storageUnitId, Long cargoId, Integer minQuantity, int limit, int offset);
    long countWithFilters(Long storageUnitId, Long cargoId, Integer minQuantity);
    Integer sumQuantityByCargoId(Long cargoId);
}
