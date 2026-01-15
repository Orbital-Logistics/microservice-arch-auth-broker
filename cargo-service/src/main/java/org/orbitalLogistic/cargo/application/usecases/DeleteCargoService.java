package org.orbitalLogistic.cargo.application.usecases;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.orbitalLogistic.cargo.application.ports.in.DeleteCargoUseCase;
import org.orbitalLogistic.cargo.application.ports.out.CargoRepository;
import org.orbitalLogistic.cargo.application.ports.out.CargoStorageRepository;
import org.orbitalLogistic.cargo.domain.exception.CargoInUseException;
import org.orbitalLogistic.cargo.domain.exception.CargoNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeleteCargoService implements DeleteCargoUseCase {

    private final CargoRepository cargoRepository;
    private final CargoStorageRepository cargoStorageRepository;

    @Override
    @Transactional
    public void deleteCargo(Long id) {
        log.debug("Deleting cargo with id: {}", id);

        if (!cargoRepository.existsById(id)) {
            throw new CargoNotFoundException("Cargo not found with id: " + id);
        }

        Integer totalQuantity = cargoStorageRepository.sumQuantityByCargoId(id);
        if (totalQuantity != null && totalQuantity > 0) {
            throw new CargoInUseException(
                    "Cannot delete cargo with id: " + id + ". It is currently used in storage (quantity: " + totalQuantity + ")"
            );
        }

        try {
            cargoRepository.deleteById(id);
            log.info("Cargo deleted with id: {}", id);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            throw new CargoInUseException(
                    "Cannot delete cargo with id: " + id + ". It is referenced by other entities."
            );
        }
    }
}
