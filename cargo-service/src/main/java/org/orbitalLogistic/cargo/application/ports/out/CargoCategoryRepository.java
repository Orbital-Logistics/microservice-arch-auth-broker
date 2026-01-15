package org.orbitalLogistic.cargo.application.ports.out;

import org.orbitalLogistic.cargo.domain.model.CargoCategory;

import java.util.List;
import java.util.Optional;

public interface CargoCategoryRepository {
    CargoCategory save(CargoCategory cargoCategory);
    Optional<CargoCategory> findById(Long id);
    List<CargoCategory> findAll();
    List<CargoCategory> findByParentCategoryIdIsNull();
    List<CargoCategory> findByParentCategoryId(Long parentCategoryId);
    void deleteById(Long id);
    boolean existsById(Long id);
}
