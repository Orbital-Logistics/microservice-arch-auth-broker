package org.orbitalLogistic.mission.clients.resilient;

import org.orbitalLogistic.mission.clients.UserDTO;
import org.orbitalLogistic.mission.clients.UserServiceClient;
import org.orbitalLogistic.mission.exceptions.UserServiceException;
import org.orbitalLogistic.mission.exceptions.UserServiceNotFound;
import org.springframework.stereotype.Component;

import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class ResilientUserService {
    private final UserServiceClient userServiceApi;

    @CircuitBreaker(name = "userService", fallbackMethod = "getUserbyIdFallback")
    public UserDTO getUserById(Long id) {
        
        log.debug("Getting user by ID: {}", id);
        
        try {
            log.debug("Calling user-service for user ID: {}", id);
            UserDTO user = userServiceApi.getUserById(id);
            log.debug("Successfully retrieved user: {}", user.username());
            return user;
        } catch (FeignException.NotFound e) {
            log.warn("User with ID {} not found", id);
            throw new UserServiceNotFound("Commanding officer not found with id: " + id);
        }
    }

    public UserDTO getUserbyIdFallback(Long id, Throwable t) {
        log.error("FALLBACK getUserById! userId: {}, error: {}", id, t.getClass().getSimpleName());
        if (!(t instanceof UserServiceException && t.getMessage().contains("not found"))) {
            throw new UserServiceException("User service unavailable", t);
        }
        throw (UserServiceException) t;
    }

    @CircuitBreaker(name = "userService", fallbackMethod = "userExistsFallback")
    public Boolean userExists(Long id) {
        log.debug("Checking if user exists: {} ", id);
        
        try {
            log.debug("Checking if user exists: {}", id);
            Boolean exists = userServiceApi.userExists(id);
            log.debug("User exists check result for ID {}: {}", id, exists);
            return exists;
        } catch (FeignException.NotFound e) {
            log.debug("User with ID {} doesn't exist", id);
            throw new UserServiceNotFound("Commanding officer not found with id: " + id);
        } 
    }

    public Boolean userExistsFallback(Long id, Throwable t) {
        log.error("FALLBACK userExists! userId: {}, error: {}", id, t.getClass().getSimpleName());
        if (!(t instanceof UserServiceException && t.getMessage().contains("not found"))) {
            throw new UserServiceException("User service unavailable", t);
        }
        throw (UserServiceException) t;
    }
}