package org.orbitalLogistic.cargo.clients;

import org.orbitalLogistic.cargo.exceptions.UserNotFoundException;
import org.orbitalLogistic.cargo.exceptions.UserServiceException;
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

    @CircuitBreaker(name = "userService", fallbackMethod = "getUsernameByIdFallback")
    public String getUserById(Long id) {
        try {
            return userServiceApi.getUserById(id);
        } catch (FeignException.NotFound e) {
            throw new UserNotFoundException("User with ID " + id + " not found");
        }
    }

    public String getUsernameByIdFallback(Long id, Throwable t) {
        log.error("FALLBACK getUsernameById! status: {}, error: {}", id, t.getClass().getSimpleName());
        throw new UserServiceException("User service unavailable!");
    }
}
