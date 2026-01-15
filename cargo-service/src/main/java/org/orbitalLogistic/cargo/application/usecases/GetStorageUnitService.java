package org.orbitalLogistic.cargo.application.usecases;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.orbitalLogistic.cargo.application.ports.in.GetStorageUnitUseCase;
import org.orbitalLogistic.cargo.application.ports.out.StorageUnitRepository;
import org.orbitalLogistic.cargo.domain.model.StorageUnit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class GetStorageUnitService implements GetStorageUnitUseCase {

    private final StorageUnitRepository storageUnitRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<StorageUnit> getUnitById(Long id) {
        log.debug("Finding storage unit by id: {}", id);
        return storageUnitRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StorageUnit> getAllUnits(int page, int size) {
        log.debug("Getting all storage units, page: {}, size: {}", page, size);
        return storageUnitRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<StorageUnit> getByUnitCode(String unitCode) {
        log.debug("Finding storage unit by unit code: {}", unitCode);
        return storageUnitRepository.findByUnitCode(unitCode);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StorageUnit> getByLocation(String location, int page, int size) {
        log.debug("Finding storage units by location: {}, page: {}, size: {}", location, page, size);
        int offset = page * size;
        return storageUnitRepository.findByLocation(location, size, offset);
    }
}
