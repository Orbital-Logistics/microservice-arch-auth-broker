package org.orbitalLogistic.cargo.application.usecases;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.orbitalLogistic.cargo.application.ports.in.DeleteCargoStorageUseCase;
import org.orbitalLogistic.cargo.application.ports.out.CargoRepository;
import org.orbitalLogistic.cargo.application.ports.out.CargoStorageRepository;
import org.orbitalLogistic.cargo.application.ports.out.StorageUnitRepository;
import org.orbitalLogistic.cargo.domain.exception.CargoNotFoundException;
import org.orbitalLogistic.cargo.domain.exception.CargoStorageNotFoundException;
import org.orbitalLogistic.cargo.domain.exception.StorageUnitNotFoundException;
import org.orbitalLogistic.cargo.domain.model.Cargo;
import org.orbitalLogistic.cargo.domain.model.CargoStorage;
import org.orbitalLogistic.cargo.domain.model.StorageUnit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeleteCargoStorageService implements DeleteCargoStorageUseCase {

    private final CargoStorageRepository cargoStorageRepository;
    private final CargoRepository cargoRepository;
    private final StorageUnitRepository storageUnitRepository;

    @Override
    @Transactional
    public void deleteStorage(Long id) {
        log.debug("Deleting cargo storage with id: {}", id);

        CargoStorage storage = cargoStorageRepository.findById(id)
                .orElseThrow(() -> new CargoStorageNotFoundException("Cargo storage not found with id: " + id));

        Cargo cargo = cargoRepository.findById(storage.getCargoId())
                .orElseThrow(() -> new CargoNotFoundException("Cargo not found with id: " + storage.getCargoId()));

        StorageUnit storageUnit = storageUnitRepository.findById(storage.getStorageUnitId())
                .orElseThrow(() -> new StorageUnitNotFoundException("Storage unit not found with id: " + storage.getStorageUnitId()));

        BigDecimal releasedMass = cargo.getMassPerUnit().multiply(BigDecimal.valueOf(storage.getQuantity()));
        BigDecimal releasedVolume = cargo.getVolumePerUnit().multiply(BigDecimal.valueOf(storage.getQuantity()));

        storageUnit.setCurrentMass(storageUnit.getCurrentMass().subtract(releasedMass));
        storageUnit.setCurrentVolume(storageUnit.getCurrentVolume().subtract(releasedVolume));
        storageUnitRepository.save(storageUnit);

        cargoStorageRepository.deleteById(id);
        log.info("Cargo storage deleted with id: {}", id);
    }
}
