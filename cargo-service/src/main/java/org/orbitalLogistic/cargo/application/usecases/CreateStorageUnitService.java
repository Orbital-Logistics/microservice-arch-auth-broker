package org.orbitalLogistic.cargo.application.usecases;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.orbitalLogistic.cargo.application.ports.in.CreateStorageUnitUseCase;
import org.orbitalLogistic.cargo.application.ports.out.StorageUnitRepository;
import org.orbitalLogistic.cargo.domain.exception.StorageUnitAlreadyExistsException;
import org.orbitalLogistic.cargo.domain.model.StorageUnit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreateStorageUnitService implements CreateStorageUnitUseCase {

    private final StorageUnitRepository storageUnitRepository;

    @Override
    @Transactional
    public StorageUnit createUnit(StorageUnit unit) {
        log.debug("Creating storage unit with unit code: {}", unit.getUnitCode());

        if (storageUnitRepository.existsByUnitCode(unit.getUnitCode())) {
            throw new StorageUnitAlreadyExistsException(
                    "Storage unit already exists with unit code: " + unit.getUnitCode()
            );
        }

        unit.setCurrentMass(BigDecimal.ZERO);
        unit.setCurrentVolume(BigDecimal.ZERO);

        StorageUnit saved = storageUnitRepository.save(unit);
        log.info("Storage unit created with id: {}", saved.getId());
        return saved;
    }
}
