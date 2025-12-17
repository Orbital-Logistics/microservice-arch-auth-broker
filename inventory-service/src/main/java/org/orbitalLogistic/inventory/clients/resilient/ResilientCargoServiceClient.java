package org.orbitalLogistic.inventory.clients.resilient;

import org.orbitalLogistic.inventory.clients.CargoDTO;
import org.orbitalLogistic.inventory.clients.CargoServiceClient;
import org.orbitalLogistic.inventory.clients.StorageUnitDTO;
import org.orbitalLogistic.inventory.exceptions.CargoServiceException;
import org.springframework.stereotype.Component;

import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class ResilientCargoServiceClient {
    private final CargoServiceClient cargoServiceApi;

    @CircuitBreaker(name = "cargoService", fallbackMethod = "getCargoByIdFallback")
    public CargoDTO getCargoById(Long id) {
        try {
            return cargoServiceApi.getCargoById(id);
        } catch (FeignException.NotFound e) {
            throw new CargoServiceException("Cargo with ID " + id + " not found");
        }
    }

    public CargoDTO getCargoByIdFallback(Long id, Throwable t) {
        log.error("FALLBACK getCargoById! status: {}, error: {}", id, t.getClass().getSimpleName());
        throw new CargoServiceException("Cargo Service unavailable!");
    }

    @CircuitBreaker(name = "cargoService", fallbackMethod = "cargoExistsFallback")
    public Boolean cargoExists(Long id) {
        try {
            return cargoServiceApi.cargoExists(id);
        } catch (FeignException.NotFound e) {
            throw new CargoServiceException("Cargo with ID " + id + " not found");
        }
    }

    public Boolean cargoExistsFallback(Long id, Throwable t) {
        log.error("FALLBACK cargoExists! status: {}, error: {}", id, t.getClass().getSimpleName());
        throw new CargoServiceException("Cargo Service unavailable!");
    }

    @CircuitBreaker(name = "cargoService", fallbackMethod = "getStorageUnitByIdFallback")
    public StorageUnitDTO getStorageUnitById(Long id) {
        try {
            return cargoServiceApi.getStorageUnitById(id);
        } catch (FeignException.NotFound e) {
            throw new CargoServiceException("Cargo with ID " + id + " not found");
        }
    }

    public StorageUnitDTO getStorageUnitByIdFallback(Long id, Throwable t) {
        log.error("FALLBACK getStorageUnitById! status: {}, error: {}", id, t.getClass().getSimpleName());
        throw new CargoServiceException("Cargo Service unavailable!");
    }

    @CircuitBreaker(name = "cargoService", fallbackMethod = "storageUnitExistsFallback")
    public Boolean storageUnitExists(Long id) {
        try {
            return cargoServiceApi.storageUnitExists(id);
        } catch (FeignException.NotFound e) {
            throw new CargoServiceException("Cargo with ID " + id + " not found");
        }
    }

    public Boolean storageUnitExistsFallback(Long id, Throwable t) {
        log.error("FALLBACK storageUnitExists! status: {}, error: {}", id, t.getClass().getSimpleName());
        throw new CargoServiceException("Cargo Service unavailable!");
    }
}
