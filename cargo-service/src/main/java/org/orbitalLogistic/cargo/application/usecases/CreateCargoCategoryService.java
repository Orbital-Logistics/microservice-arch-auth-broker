package org.orbitalLogistic.cargo.application.usecases;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.orbitalLogistic.cargo.application.ports.in.CreateCargoCategoryUseCase;
import org.orbitalLogistic.cargo.application.ports.out.CargoCategoryRepository;
import org.orbitalLogistic.cargo.domain.exception.CargoCategoryNotFoundException;
import org.orbitalLogistic.cargo.domain.model.CargoCategory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreateCargoCategoryService implements CreateCargoCategoryUseCase {

    private final CargoCategoryRepository cargoCategoryRepository;

    @Override
    @Transactional
    public CargoCategory createCategory(CargoCategory category) {
        log.debug("Creating cargo category with name: {}", category.getName());

        if (category.getParentCategoryId() != null && 
            !cargoCategoryRepository.existsById(category.getParentCategoryId())) {
            throw new CargoCategoryNotFoundException(
                    "Parent category not found with id: " + category.getParentCategoryId()
            );
        }

        CargoCategory saved = cargoCategoryRepository.save(category);
        log.info("Cargo category created with id: {}", saved.getId());
        return saved;
    }
}
