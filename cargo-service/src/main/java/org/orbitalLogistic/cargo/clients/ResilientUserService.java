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

    @CircuitBreaker(name = "userService", fallbackMethod = "getUserByIdFallback")
    public String getUserById(Long id) {
        try {
            return userServiceApi.getUserById(id);
        } catch (FeignException.NotFound e) {
            throw new UserNotFoundException("User with ID " + id + " not found");
        }
    }

    public String getUserByIdFallback(Long id, Throwable t) {
        log.error("FALLBACK getUsernameById! status: {}, error: {}", id, t.getClass().getSimpleName());
        throw new UserServiceException("User service unavailable!");
    }

    @CircuitBreaker(name = "userService", fallbackMethod = "userExistsFallback")
    public Boolean userExists(Long id) {
        return userServiceApi.userExists(id);
    }

    public Boolean userExistsFallback(Long id, Throwable t) {
        log.error("FALLBACK userExists! userId: {}, error: {}", id, t.getClass().getSimpleName());
        if (!(t instanceof UserServiceException && t.getMessage().contains("not found"))) {
            throw new UserServiceException("User service unavailable", t);
        }
        throw (UserServiceException) t;
    }
}
