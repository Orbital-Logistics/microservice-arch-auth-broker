package org.orbitalLogistic.inventory.clients.resilient;


import org.orbitalLogistic.inventory.clients.UserDTO;
import org.orbitalLogistic.inventory.clients.UserServiceClient;
import org.orbitalLogistic.inventory.exceptions.UserServiceException;
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

    @CircuitBreaker(name = "userService", fallbackMethod = "getUserByIdFallback")
    public UserDTO getUserById(Long id) {
        try {
            return userServiceApi.getUserById(id);
        } catch (FeignException.NotFound e) {
            throw new UserServiceException("User with ID " + id + " not found");
        }
    }

    public UserDTO getUserByIdFallback(Long id, Throwable t) {
        log.error("FALLBACK getUsernameById! status: {}, error: {}", id, t.getClass().getSimpleName());
        throw new UserServiceException("User service unavailable!");
    }

    @CircuitBreaker(name = "userService", fallbackMethod = "userExistsFallback")
    public Boolean userExists(Long id) {
        try {
            return userServiceApi.userExists(id);
        } catch (FeignException.NotFound e) {
            throw new UserServiceException("User with ID " + id + " exists");
        }
    }

    public Boolean userExistsFallback(Long id, Throwable t) {
        log.error("FALLBACK getUsernameById! status: {}, error: {}", id, t.getClass().getSimpleName());
        throw new UserServiceException("User service unavailable!");
    }
}
