package org.orbitalLogistic.inventory.clients.resilient;

import org.orbitalLogistic.inventory.clients.SpacecraftDTO;
import org.orbitalLogistic.inventory.clients.SpacecraftServiceClient;
import org.orbitalLogistic.inventory.exceptions.SpacecraftServiceException;
import org.springframework.stereotype.Component;

import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class ResilientSpacecraftService {
    private final SpacecraftServiceClient spacecraftServiceApi;

    @CircuitBreaker(name = "spacecraftService", fallbackMethod = "getSpacecraftByIdFallback")
    public SpacecraftDTO getSpacecraftById(Long id) {
        try {
            return spacecraftServiceApi.getSpacecraftById(id);
        } catch (FeignException.NotFound e) {
            throw new SpacecraftServiceException("Spacecraft with ID " + id + " not found");
        }
    }

    public SpacecraftDTO getSpacecraftByIdFallback(Long id, Throwable t) {
        log.error("FALLBACK getSpacecraftById! status: {}, error: {}", id, t.getClass().getSimpleName());
        throw new SpacecraftServiceException("Cargo Service service unavailable!");
    }

    @CircuitBreaker(name = "spacecraftService", fallbackMethod = "spacecraftExistsFallback")
    public Boolean spacecraftExists(Long id) {
        try {
            return spacecraftServiceApi.spacecraftExists(id);
        } catch (FeignException.NotFound e) {
            throw new SpacecraftServiceException("Spacecraft with ID " + id + " not found");
        }
    }

    public Boolean spacecraftExistsFallback(Long id, Throwable t) {
        log.error("FALLBACK cargoExists! status: {}, error: {}", id, t.getClass().getSimpleName());
        throw new SpacecraftServiceException("Spacecraft Service unavailable!");
    }
}
