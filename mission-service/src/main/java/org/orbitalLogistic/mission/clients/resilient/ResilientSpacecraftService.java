package org.orbitalLogistic.mission.clients.resilient;

import org.orbitalLogistic.mission.clients.SpacecraftDTO;
import org.orbitalLogistic.mission.clients.SpacecraftServiceClient;
import org.orbitalLogistic.mission.domain.exception.SpacecraftServiceException;
import org.orbitalLogistic.mission.domain.exception.SpacecraftServiceNotFound;
import org.springframework.stereotype.Service;

import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ResilientSpacecraftService {
    private final SpacecraftServiceClient spacecraftServiceApi;
    
    @CircuitBreaker(name = "spacecraftService", fallbackMethod = "fallbackGetSpacecraftById")
    public SpacecraftDTO getSpacecraftById(Long id) {
        try{
            return spacecraftServiceApi.getSpacecraftById(id);
        } catch (FeignException.NotFound e) {
            throw new SpacecraftServiceNotFound("Spacecraft with ID " + id + " not found");
        } catch (FeignException e) {
            throw new SpacecraftServiceException("Spacecraft Service unavailable!");
        }
    }

    public SpacecraftDTO fallbackGetSpacecraftById(Long id, Throwable t) {
        log.error("Fallback triggered for spacecraft ID: {}. Error: {}", id, t.getMessage(), t);
        throw new SpacecraftServiceException("Spacecraft Service unavailable!");
    }
    
    @CircuitBreaker(name = "spacecraftService", fallbackMethod = "spacecraftExistsFallback")
    public Boolean spacecraftExists(Long id) {
        try {
            return spacecraftServiceApi.spacecraftExists(id);
        } catch (FeignException.NotFound e) {
            throw new SpacecraftServiceNotFound("Spacecraft with ID " + id + " not found");
        } catch (FeignException e) {
            throw new SpacecraftServiceException("Spacecraft Service unavailable!");
        }
    }
    
    public Boolean spacecraftExistsFallback(Long id, Throwable t) {
        log.error("Fallback for spacecraftExists ID: {}. Error: {}", id, t.getMessage());
        throw new SpacecraftServiceException("Spacecraft Service unavailable!");
    }
}