package org.orbitalLogistic.file.clients.resilient;

import java.util.function.Supplier;

import org.orbitalLogistic.file.clients.CargoDTO;
import org.orbitalLogistic.file.clients.CargoServiceClient;
import org.orbitalLogistic.file.clients.StorageUnitDTO;
import org.orbitalLogistic.file.exceptions.CargoServiceException;
import org.orbitalLogistic.file.exceptions.UserServiceException;
import org.springframework.stereotype.Component;

import feign.FeignException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class ResilientCargoServiceClient {
    private final CargoServiceClient cargoServiceApi;
    private final CircuitBreakerRegistry registry;

    // @CircuitBreaker(name = "cargoService", fallbackMethod = "getCargoByIdFallback")
    public CargoDTO getCargoById(Long id) {
        CircuitBreaker cb = registry.circuitBreaker("cargoService");
    
        Supplier<CargoDTO> supplier = CircuitBreaker.decorateSupplier(
            cb,
            () -> cargoServiceApi.getCargoById(id)
        );

        try {
            return supplier.get();
        } catch (CallNotPermittedException e) {
            return getCargoByIdFallback(id, e);
        } catch (FeignException.NotFound e) {
            throw new CargoServiceException("User with ID " + id + " not found", e);
        } catch (FeignException.ServiceUnavailable e) {
            throw new UserServiceException("Cargo Service unavailable!");
        }
        // try {
        //     return cargoServiceApi.getCargoById(id);
        // } catch (FeignException.NotFound e) {
        //     throw new CargoServiceException("Cargo with ID " + id + " not found");
        // }
    }

    public CargoDTO getCargoByIdFallback(Long id, Throwable t) {
        log.error("FALLBACK getCargoById! status: {}, error: {}", id, t.getClass().getSimpleName());
        throw new CargoServiceException("Cargo Service unavailable!");
    }

    // @CircuitBreaker(name = "cargoService", fallbackMethod = "cargoExistsFallback")
    public Boolean cargoExists(Long id) {
        CircuitBreaker cb = registry.circuitBreaker("cargoService");
    
        Supplier<Boolean> supplier = CircuitBreaker.decorateSupplier(
            cb,
            () -> cargoServiceApi.cargoExists(id)
        );

        try {
            return supplier.get();
        } catch (CallNotPermittedException e) {
            return cargoExistsFallback(id, e);
        } catch (FeignException.NotFound e) {
            throw new CargoServiceException("User with ID " + id + " not found", e);
        } catch (FeignException.ServiceUnavailable e) {
            throw new UserServiceException("Cargo Service unavailable!");
        }
        // try {
        //     return cargoServiceApi.cargoExists(id);
        // } catch (FeignException.NotFound e) {
        //     throw new CargoServiceException("Cargo with ID " + id + " not found");
        // }
    }

    public Boolean cargoExistsFallback(Long id, Throwable t) {
        log.error("FALLBACK cargoExists! status: {}, error: {}", id, t.getClass().getSimpleName());
        throw new CargoServiceException("Cargo Service unavailable!");
    }

    // @CircuitBreaker(name = "cargoService", fallbackMethod = "getStorageUnitByIdFallback")
    public StorageUnitDTO getStorageUnitById(Long id) {
        CircuitBreaker cb = registry.circuitBreaker("cargoService");
    
        Supplier<StorageUnitDTO> supplier = CircuitBreaker.decorateSupplier(
            cb,
            () -> cargoServiceApi.getStorageUnitById(id)
        );

        try {
            return supplier.get();
        } catch (CallNotPermittedException e) {
            return getStorageUnitByIdFallback(id, e);
        } catch (FeignException.NotFound e) {
            throw new CargoServiceException("User with ID " + id + " not found", e);
        } catch (FeignException.ServiceUnavailable e) {
            throw new UserServiceException("Spacecraft Service unavailable!");
        }
        // try {
        //     return cargoServiceApi.getStorageUnitById(id);
        // } catch (FeignException.NotFound e) {
        //     throw new CargoServiceException("Cargo with ID " + id + " not found");
        // }
    }

    public StorageUnitDTO getStorageUnitByIdFallback(Long id, Throwable t) {
        log.error("FALLBACK getStorageUnitById! status: {}, error: {}", id, t.getClass().getSimpleName());
        throw new CargoServiceException("Cargo Service unavailable!");
    }

    // @CircuitBreaker(name = "cargoService", fallbackMethod = "storageUnitExistsFallback")
    public Boolean storageUnitExists(Long id) {
        CircuitBreaker cb = registry.circuitBreaker("cargoService");
    
        Supplier<Boolean> supplier = CircuitBreaker.decorateSupplier(
            cb,
            () -> cargoServiceApi.storageUnitExists(id)
        );

        try {
            return supplier.get();
        } catch (CallNotPermittedException e) {
            return storageUnitExistsFallback(id, e);
        } catch (FeignException.NotFound e) {
            throw new CargoServiceException("User with ID " + id + " not found", e);
        }catch (FeignException e) {
            throw new UserServiceException("Spacecraft Service unavailable!");
        }
        // try {
        //     return cargoServiceApi.storageUnitExists(id);
        // } catch (FeignException.NotFound e) {
        //     throw new CargoServiceException("Cargo with ID " + id + " not found");
        // }
    }

    public Boolean storageUnitExistsFallback(Long id, Throwable t) {
        log.error("FALLBACK storageUnitExists! status: {}, error: {}", id, t.getClass().getSimpleName());
        throw new CargoServiceException("Cargo Service unavailable!");
    }
}
