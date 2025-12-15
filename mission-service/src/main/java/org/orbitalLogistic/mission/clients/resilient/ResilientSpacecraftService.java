package org.orbitalLogistic.mission.clients.resilient;

import org.orbitalLogistic.mission.clients.SpacecraftDTO;
import org.orbitalLogistic.mission.clients.SpacecraftServiceClient;
import org.orbitalLogistic.mission.exceptions.SpacecraftServiceException;
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
            throw new SpacecraftServiceException("Spacecraft with ID " + id + " not found");
        }
    }

    public SpacecraftDTO fallbackGetSpacecraftById(Long id, Throwable t) {
        log.error("Fallback triggered for spacecraft ID: {}. Error: {}", id, t.getMessage(), t);
        
        if (!(t instanceof SpacecraftServiceException && t.getMessage().contains("not found"))) {
            throw new SpacecraftServiceException("Spacecraft Service unavailable!");
        }
        throw (SpacecraftServiceException) t;
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
        log.error("Fallback for spacecraftExists ID: {}. Error: {}", id, t.getMessage());
        
        // For network issues, return false instead of throwing exception
        if (t instanceof java.net.NoRouteToHostException || 
            t instanceof java.net.UnknownHostException) {
            return false; // Or true, depending on your business logic
        }
        
        throw new SpacecraftServiceException("Spacecraft Service unavailable!");
    }
}