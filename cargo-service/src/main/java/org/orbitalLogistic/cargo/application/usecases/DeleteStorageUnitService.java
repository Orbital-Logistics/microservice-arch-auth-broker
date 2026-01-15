package org.orbitalLogistic.cargo.application.usecases;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.orbitalLogistic.cargo.application.ports.in.DeleteStorageUnitUseCase;
import org.orbitalLogistic.cargo.application.ports.out.CargoStorageRepository;
import org.orbitalLogistic.cargo.application.ports.out.StorageUnitRepository;
import org.orbitalLogistic.cargo.domain.exception.StorageUnitNotFoundException;
import org.orbitalLogistic.cargo.domain.model.CargoStorage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeleteStorageUnitService implements DeleteStorageUnitUseCase {

    private final StorageUnitRepository storageUnitRepository;
    private final CargoStorageRepository cargoStorageRepository;

    @Override
    @Transactional
    public void deleteUnit(Long id) {
        log.debug("Deleting storage unit with id: {}", id);

        if (!storageUnitRepository.existsById(id)) {
            throw new StorageUnitNotFoundException("Storage unit not found with id: " + id);
        }

        List<CargoStorage> storages = cargoStorageRepository.findByStorageUnitId(id);
        for (CargoStorage storage : storages) {
            cargoStorageRepository.deleteById(storage.getId());
        }

        storageUnitRepository.deleteById(id);
        log.info("Storage unit deleted with id: {} along with {} cargo storages", id, storages.size());
    }
}
