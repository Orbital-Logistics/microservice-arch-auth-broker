package org.orbitalLogistic.cargo.application.usecases;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.orbitalLogistic.cargo.application.ports.in.DeleteCargoCategoryUseCase;
import org.orbitalLogistic.cargo.application.ports.out.CargoCategoryRepository;
import org.orbitalLogistic.cargo.domain.exception.CargoCategoryNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeleteCargoCategoryService implements DeleteCargoCategoryUseCase {

    private final CargoCategoryRepository cargoCategoryRepository;

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        log.debug("Deleting category with id: {}", id);

        if (!cargoCategoryRepository.existsById(id)) {
            throw new CargoCategoryNotFoundException("Category not found with id: " + id);
        }

        cargoCategoryRepository.deleteById(id);
        log.info("Category deleted with id: {}", id);
    }
}
