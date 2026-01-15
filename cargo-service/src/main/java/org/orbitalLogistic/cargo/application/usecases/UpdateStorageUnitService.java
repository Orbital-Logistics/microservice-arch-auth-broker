package org.orbitalLogistic.cargo.application.usecases;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.orbitalLogistic.cargo.application.ports.in.UpdateStorageUnitUseCase;
import org.orbitalLogistic.cargo.application.ports.out.CargoRepository;
import org.orbitalLogistic.cargo.application.ports.out.CargoStorageRepository;
import org.orbitalLogistic.cargo.application.ports.out.StorageUnitRepository;
import org.orbitalLogistic.cargo.domain.exception.CargoNotFoundException;
import org.orbitalLogistic.cargo.domain.exception.StorageUnitAlreadyExistsException;
import org.orbitalLogistic.cargo.domain.exception.StorageUnitNotFoundException;
import org.orbitalLogistic.cargo.domain.model.Cargo;
import org.orbitalLogistic.cargo.domain.model.CargoStorage;
import org.orbitalLogistic.cargo.domain.model.StorageUnit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UpdateStorageUnitService implements UpdateStorageUnitUseCase {

    private final StorageUnitRepository storageUnitRepository;
    private final CargoStorageRepository cargoStorageRepository;
    private final CargoRepository cargoRepository;

    @Override
    @Transactional
    public StorageUnit updateUnit(Long id, StorageUnit unit) {
        log.debug("Updating storage unit with id: {}", id);

        StorageUnit existing = storageUnitRepository.findById(id)
                .orElseThrow(() -> new StorageUnitNotFoundException("Storage unit not found with id: " + id));

        if (!existing.getUnitCode().equals(unit.getUnitCode()) && 
            storageUnitRepository.existsByUnitCode(unit.getUnitCode())) {
            throw new StorageUnitAlreadyExistsException(
                    "Storage unit already exists with unit code: " + unit.getUnitCode()
            );
        }

        existing.setUnitCode(unit.getUnitCode());
        existing.setLocation(unit.getLocation());
        existing.setStorageType(unit.getStorageType());
        existing.setMaxMass(unit.getMaxMass());
        existing.setMaxVolume(unit.getMaxVolume());
        existing.setIsActive(unit.getIsActive());

        StorageUnit updated = storageUnitRepository.save(existing);
        log.info("Storage unit updated with id: {}", updated.getId());
        return updated;
    }

    @Override
    @Transactional
    public StorageUnit recalculateCapacity(Long id) {
        log.debug("Recalculating capacity for storage unit id: {}", id);

        StorageUnit unit = storageUnitRepository.findById(id)
                .orElseThrow(() -> new StorageUnitNotFoundException("Storage unit not found with id: " + id));

        List<CargoStorage> storages = cargoStorageRepository.findByStorageUnitId(id);

        BigDecimal totalMass = BigDecimal.ZERO;
        BigDecimal totalVolume = BigDecimal.ZERO;

        for (CargoStorage storage : storages) {
            Cargo cargo = cargoRepository.findById(storage.getCargoId())
                    .orElseThrow(() -> new CargoNotFoundException("Cargo not found with id: " + storage.getCargoId()));

            totalMass = totalMass.add(cargo.getMassPerUnit().multiply(BigDecimal.valueOf(storage.getQuantity())));
            totalVolume = totalVolume.add(cargo.getVolumePerUnit().multiply(BigDecimal.valueOf(storage.getQuantity())));
        }

        unit.setCurrentMass(totalMass);
        unit.setCurrentVolume(totalVolume);
        StorageUnit updated = storageUnitRepository.save(unit);
        log.info("Capacity recalculated for storage unit id: {}", updated.getId());
        return updated;
    }
}
