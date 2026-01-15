package org.orbitalLogistic.cargo.application.usecases;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.orbitalLogistic.cargo.application.ports.in.GetCargoCategoryUseCase;
import org.orbitalLogistic.cargo.application.ports.out.CargoCategoryRepository;
import org.orbitalLogistic.cargo.domain.model.CargoCategory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class GetCargoCategoryService implements GetCargoCategoryUseCase {

    private final CargoCategoryRepository cargoCategoryRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<CargoCategory> getCategoryById(Long id) {
        log.debug("Finding category by id: {}", id);
        return cargoCategoryRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CargoCategory> getAllCategories() {
        log.debug("Getting all categories");
        return cargoCategoryRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CargoCategory> getCategoryTree() {
        log.debug("Building category tree");
        List<CargoCategory> rootCategories = cargoCategoryRepository.findByParentCategoryIdIsNull();
        rootCategories.forEach(this::loadChildren);
        log.info("Category tree built with {} root categories", rootCategories.size());
        return rootCategories;
    }

    private void loadChildren(CargoCategory category) {
        List<CargoCategory> children = cargoCategoryRepository.findByParentCategoryId(category.getId());
        category.setChildren(children);
        children.forEach(this::loadChildren);
    }
}
