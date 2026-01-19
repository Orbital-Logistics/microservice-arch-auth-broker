package org.orbitalLogistic.file.clients.resilient;

import java.util.function.Supplier;

import org.orbitalLogistic.file.clients.SpacecraftDTO;
import org.orbitalLogistic.file.clients.SpacecraftServiceClient;
import org.orbitalLogistic.file.exceptions.SpacecraftServiceException;
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
public class ResilientSpacecraftService {
    private final SpacecraftServiceClient spacecraftServiceApi;
    private final CircuitBreakerRegistry registry;

    // @CircuitBreaker(name = "spacecraftService", fallbackMethod = "getSpacecraftByIdFallback")
    public SpacecraftDTO getSpacecraftById(Long id) {
        CircuitBreaker cb = registry.circuitBreaker("spacecraftService");
    
        Supplier<SpacecraftDTO> supplier = CircuitBreaker.decorateSupplier(
            cb,
            () -> spacecraftServiceApi.getSpacecraftById(id)
        );

        try {
            return supplier.get();
        } catch (CallNotPermittedException e) {
            return getSpacecraftByIdFallback(id, e);
        } catch (FeignException.NotFound e) {
            throw new SpacecraftServiceException("Spacecraft with ID " + id + " not found", e);
        } catch (FeignException.ServiceUnavailable e) {
            throw new SpacecraftServiceException("Spacecraft Service unavailable!");
        }
        // try {
        //     return spacecraftServiceApi.getSpacecraftById(id);
        // } catch (FeignException.NotFound e) {
        //     throw new SpacecraftServiceException("Spacecraft with ID " + id + " not found");
        // }
    }

    public SpacecraftDTO getSpacecraftByIdFallback(Long id, Throwable t) {
        log.error("FALLBACK getSpacecraftById! status: {}, error: {}", id, t.getClass().getSimpleName());
        throw new SpacecraftServiceException("Cargo Service service unavailable!");
    }

    // @CircuitBreaker(name = "spacecraftService", fallbackMethod = "spacecraftExistsFallback")
    public Boolean spacecraftExists(Long id) {
        CircuitBreaker cb = registry.circuitBreaker("spacecraftService");
    
        Supplier<Boolean> supplier = CircuitBreaker.decorateSupplier(
            cb,
            () -> spacecraftServiceApi.spacecraftExists(id)
        );

        try {
            return supplier.get();
        } catch (CallNotPermittedException e) {
            return spacecraftExistsFallback(id, e);
        } catch (FeignException.NotFound e) {
            throw new SpacecraftServiceException("Spacecraft with ID " + id + " not found", e);
        } catch (FeignException e) {
            throw new SpacecraftServiceException("Spacecraft Service unavailable!");
        }
        // try {
        //     return spacecraftServiceApi.spacecraftExists(id);
        // } catch (FeignException.NotFound e) {
        //     throw new SpacecraftServiceException("Spacecraft with ID " + id + " not found");
        // }
    }

    public Boolean spacecraftExistsFallback(Long id, Throwable t) {
        log.error("FALLBACK spacecraftExists! status: {}, error: {}", id, t.getClass().getSimpleName());
        throw new SpacecraftServiceException("Spacecraft Service unavailable!");
    }
}
