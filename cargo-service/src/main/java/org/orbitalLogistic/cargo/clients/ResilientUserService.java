package org.orbitalLogistic.cargo.clients;

import java.util.function.Supplier;

import org.orbitalLogistic.cargo.exceptions.UserServiceException;
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
    public String getUserById(Long id) {
        CircuitBreaker cb = registry.circuitBreaker("userService");
    
        Supplier<String> supplier = CircuitBreaker.decorateSupplier(
            cb,
            () -> userServiceApi.getUserById(id)
        );
        
        try {
            return supplier.get();
        } catch (CallNotPermittedException e) {
            return getUserByIdFallback(id, e);
        } catch (FeignException.NotFound e) {
            throw new UserServiceException("User with ID " + id + " not found", e);
        } catch (FeignException e) {
            throw new UserServiceException("User Service unavailable!");
        }
    }

    // public SpacecraftCargoUsageDTO getSpacecraftCargoUsageFallback(Long spacecraftId, Throwable e) {
    //     log.error("FALLBACK getCargoById! status: {}, error: {}", spacecraftId, e.getClass().getSimpleName());
    //     throw new SpacecraftCargoUsageException("Cargo Service unavailable!");
    // }

    // // @CircuitBreaker(name = "userService", fallbackMethod = "getUserByIdFallback")
    // public String getUserById(Long id) {
    //     try {
    //         return userServiceApi.getUserById(id);
    //     } catch (FeignException.NotFound e) {
    //         throw new UserNotFoundException("User with ID " + id + " not found");
    //     }
    // }

    public String getUserByIdFallback(Long id, Throwable t) {
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
            throw new UserServiceException("User with ID " + id + " not found", e);
        } catch (FeignException e) {
            throw new UserServiceException("User Service unavailable!");
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
