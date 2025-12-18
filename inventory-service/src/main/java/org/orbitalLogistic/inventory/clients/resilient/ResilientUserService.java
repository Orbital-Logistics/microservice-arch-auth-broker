package org.orbitalLogistic.inventory.clients.resilient;


import java.util.function.Supplier;

import org.orbitalLogistic.inventory.clients.SpacecraftDTO;
import org.orbitalLogistic.inventory.clients.UserDTO;
import org.orbitalLogistic.inventory.clients.UserServiceClient;
import org.orbitalLogistic.inventory.exceptions.UserServiceException;
import org.orbitalLogistic.inventory.exceptions.UserServiceNotFound;
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
public class ResilientUserService {
    private final UserServiceClient userServiceApi;
    private final CircuitBreakerRegistry registry;

    // @CircuitBreaker(name = "userService", fallbackMethod = "getUserByIdFallback")
    public UserDTO getUserById(Long id) {
        CircuitBreaker cb = registry.circuitBreaker("userService");
    
        Supplier<UserDTO> supplier = CircuitBreaker.decorateSupplier(
            cb,
            () -> userServiceApi.getUserById(id)
        );

        try {
            return supplier.get();
        } catch (CallNotPermittedException e) {
            return getUserByIdFallback(id, e);
        } catch (FeignException.NotFound e) {
            throw new UserServiceNotFound("User with ID " + id + " not found", e);
        } catch (FeignException e) {
            throw new UserServiceException("Unable to send request User Service");
        }
        // try {
        //     return userServiceApi.getUserById(id);
        // } catch (FeignException.NotFound e) {
        //     throw new UserServiceException("User with ID " + id + " not found");
        // }
    }

    public UserDTO getUserByIdFallback(Long id, Throwable t) {
        log.error("FALLBACK getUsernameById! status: {}, error: {}", id, t.getClass().getSimpleName());
        throw new UserServiceException("User service unavailable!");
    }

    // @CircuitBreaker(name = "userService", fallbackMethod = "userExistsFallback")
    public Boolean userExists(Long id) {
        CircuitBreaker cb = registry.circuitBreaker("userService");
    
        Supplier<Boolean> supplier = CircuitBreaker.decorateSupplier(
            cb,
            () -> userServiceApi.userExists(id)
        );

        try {
            return supplier.get();
        } catch (CallNotPermittedException e) {
            return userExistsFallback(id, e);
        } catch (FeignException.NotFound e) {
            throw new UserServiceNotFound("User with ID " + id + " not found", e);
        } catch (FeignException e) {
            throw new UserServiceException("Unable to send request User Service");
        }
        // try {
        //     return userServiceApi.userExists(id);
        // } catch (FeignException.NotFound e) {
        //     throw new UserServiceException("User with ID " + id + " exists");
        // }
    }

    public Boolean userExistsFallback(Long id, Throwable t) {
        log.error("FALLBACK getUsernameById! status: {}, error: {}", id, t.getClass().getSimpleName());
        throw new UserServiceException("User service unavailable!");
    }
}
