package org.orbitalLogistic.inventory.clients;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CargoServiceClientFallback implements CargoServiceClient {

    @Override
    public CargoDTO getCargoById(Long id) {
        log.warn("Fallback: Unable to fetch cargo with id: {}", id);
        return null;
    }

    @Override
    public Boolean cargoExists(Long id) {
        log.warn("Fallback: Unable to check if cargo exists with id: {}", id);
        return false;
    }

    @Override
    public StorageUnitDTO getStorageUnitById(Long id) {
        log.warn("Fallback: Unable to fetch storage unit with id: {}", id);
        return null;
    }

    @Override
    public Boolean storageUnitExists(Long id) {
        log.warn("Fallback: Unable to check if storage unit exists with id: {}", id);
        return false;
    }
}

