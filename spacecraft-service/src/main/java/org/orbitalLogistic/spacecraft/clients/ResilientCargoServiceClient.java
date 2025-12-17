package org.orbitalLogistic.spacecraft.clients;

import org.orbitalLogistic.spacecraft.exceptions.SpacecraftCargoUsageException;
import org.orbitalLogistic.spacecraft.exceptions.SpacecraftNotFoundException;

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

    @CircuitBreaker(name = "cargoService", fallbackMethod = "getSpacecraftCargoUsageFallback")
    public SpacecraftCargoUsageDTO getSpacecraftCargoUsage(Long spacecraftId) {
        try {
            return cargoServiceApi.getSpacecraftCargoUsage(spacecraftId);
        } catch (FeignException.NotFound e) {
            throw new SpacecraftNotFoundException("Spacecraft with ID " + spacecraftId + " not found");
        }
    }

    public SpacecraftCargoUsageDTO getSpacecraftCargoUsageFallback(Long spacecraftId, Throwable t) {
        log.error("FALLBACK getCargoById! status: {}, error: {}", spacecraftId, t.getClass().getSimpleName());
        throw new SpacecraftCargoUsageException("Cargo Service unavailable!");
    }
}
