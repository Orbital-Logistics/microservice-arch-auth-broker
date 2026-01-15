package org.orbitalLogistic.cargo.application.ports.in;

import org.orbitalLogistic.cargo.domain.model.CargoCategory;

import java.util.List;
import java.util.Optional;

public interface GetCargoCategoryUseCase {
    Optional<CargoCategory> getCategoryById(Long id);
    List<CargoCategory> getAllCategories();
    List<CargoCategory> getCategoryTree();
}
