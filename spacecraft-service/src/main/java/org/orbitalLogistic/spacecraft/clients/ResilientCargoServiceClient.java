package org.orbitalLogistic.spacecraft.clients;

import java.util.function.Supplier;

import org.orbitalLogistic.spacecraft.exceptions.SpacecraftCargoUsageException;
import org.orbitalLogistic.spacecraft.exceptions.SpacecraftNotFoundException;

import org.springframework.stereotype.Component;

import feign.FeignException;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;


@Component
@Slf4j
@RequiredArgsConstructor
public class ResilientCargoServiceClient {
    private final CargoServiceClient cargoServiceApi;
    private final CircuitBreakerRegistry registry;

    public SpacecraftCargoUsageDTO getSpacecraftCargoUsage(Long spacecraftId) {
        CircuitBreaker cb = registry.circuitBreaker("cargoService");
    
        Supplier<SpacecraftCargoUsageDTO> supplier = CircuitBreaker.decorateSupplier(
            cb,
            () -> cargoServiceApi.getSpacecraftCargoUsage(spacecraftId)
        );
        
        try {
            return supplier.get();
        } catch (CallNotPermittedException e) {
            return getSpacecraftCargoUsageFallback(spacecraftId, e);
        } catch (FeignException.NotFound e) {
            throw new SpacecraftCargoUsageException("User with ID " + spacecraftId + " not found");
        } catch (FeignException e) {
            throw new SpacecraftCargoUsageException("Cargo Service unavailable!");
        }
    }

    public SpacecraftCargoUsageDTO getSpacecraftCargoUsageFallback(Long spacecraftId, Throwable e) {
        log.error("FALLBACK getCargoById! status: {}, error: {}", spacecraftId, e.getClass().getSimpleName());
        throw new SpacecraftCargoUsageException("Cargo Service unavailable!");
    }

}
