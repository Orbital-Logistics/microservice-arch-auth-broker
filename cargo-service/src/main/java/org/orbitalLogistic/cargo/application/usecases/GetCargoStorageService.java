package org.orbitalLogistic.cargo.application.usecases;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.orbitalLogistic.cargo.application.ports.in.GetCargoStorageUseCase;
import org.orbitalLogistic.cargo.application.ports.out.CargoStorageRepository;
import org.orbitalLogistic.cargo.domain.model.CargoStorage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class GetCargoStorageService implements GetCargoStorageUseCase {

    private final CargoStorageRepository cargoStorageRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<CargoStorage> getStorageById(Long id) {
        log.debug("Finding cargo storage by id: {}", id);
        return cargoStorageRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CargoStorage> getAllStorages(int page, int size) {
        log.debug("Getting all cargo storages, page: {}, size: {}", page, size);
        int offset = page * size;
        return cargoStorageRepository.findWithFilters(null, null, null, size, offset);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CargoStorage> searchStorages(Long storageUnitId, Long cargoId, int page, int size) {
        log.debug("Searching cargo storages - storageUnitId: {}, cargoId: {}", 
                storageUnitId, cargoId);
        int offset = page * size;
        return cargoStorageRepository.findWithFilters(storageUnitId, cargoId, null, size, offset);
    }
}
