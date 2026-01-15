package org.orbitalLogistic.cargo.clients;

import feign.FeignException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.orbitalLogistic.cargo.domain.exception.UserServiceException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResilientUserServiceTest {

    @Mock
    private UserServiceClient userServiceApi;

    @Mock
    private CircuitBreakerRegistry registry;

    @Mock
    private CircuitBreaker circuitBreaker;

    @InjectMocks
    private ResilientUserService resilientUserService;

    @BeforeEach
    void setUp() {
        lenient().when(registry.circuitBreaker(anyString())).thenReturn(circuitBreaker);
    }

    @Test
    void getUserById_Success() {
        // Given
        Long userId = 1L;
        String expectedUsername = "john_doe";
        when(userServiceApi.getUserById(userId)).thenReturn(expectedUsername);

        // When
        String result = resilientUserService.getUserById(userId);

        // Then
        assertEquals(expectedUsername, result);
        verify(userServiceApi).getUserById(userId);
    }

    @Test
    void getUserById_ThrowsException_WhenUserNotFound() {
        // Given
        Long userId = 999L;
        FeignException.NotFound notFoundException = mock(FeignException.NotFound.class);
        when(userServiceApi.getUserById(userId)).thenThrow(notFoundException);

        // When & Then
        UserServiceException exception = assertThrows(UserServiceException.class, () -> {
            resilientUserService.getUserById(userId);
        });
        
        assertTrue(exception.getMessage().contains("not found"));
        verify(userServiceApi).getUserById(userId);
    }

    @Test
    void getUserById_ThrowsException_WhenServiceUnavailable() {
        // Given
        Long userId = 1L;
        FeignException feignException = mock(FeignException.class);
        when(userServiceApi.getUserById(userId)).thenThrow(feignException);

        // When & Then
        UserServiceException exception = assertThrows(UserServiceException.class, () -> {
            resilientUserService.getUserById(userId);
        });
        
        assertTrue(exception.getMessage().contains("unavailable"));
        verify(userServiceApi).getUserById(userId);
    }

    @Test
    void userExists_Success_ReturnsTrue() {
        // Given
        Long userId = 1L;
        when(userServiceApi.userExists(userId)).thenReturn(true);

        // When
        Boolean result = resilientUserService.userExists(userId);

        // Then
        assertTrue(result);
        verify(userServiceApi).userExists(userId);
    }

    @Test
    void userExists_Success_ReturnsFalse() {
        // Given
        Long userId = 999L;
        when(userServiceApi.userExists(userId)).thenReturn(false);

        // When
        Boolean result = resilientUserService.userExists(userId);

        // Then
        assertFalse(result);
        verify(userServiceApi).userExists(userId);
    }

    @Test
    void userExists_ThrowsException_WhenUserNotFound() {
        // Given
        Long userId = 999L;
        FeignException.NotFound notFoundException = mock(FeignException.NotFound.class);
        when(userServiceApi.userExists(userId)).thenThrow(notFoundException);

        // When & Then
        UserServiceException exception = assertThrows(UserServiceException.class, () -> {
            resilientUserService.userExists(userId);
        });
        
        assertTrue(exception.getMessage().contains("not found"));
        verify(userServiceApi).userExists(userId);
    }

    @Test
    void userExists_ThrowsException_WhenServiceUnavailable() {
        // Given
        Long userId = 1L;
        FeignException feignException = mock(FeignException.class);
        when(userServiceApi.userExists(userId)).thenThrow(feignException);

        // When & Then
        UserServiceException exception = assertThrows(UserServiceException.class, () -> {
            resilientUserService.userExists(userId);
        });
        
        assertTrue(exception.getMessage().contains("unavailable"));
        verify(userServiceApi).userExists(userId);
    }

    @Test
    void getUserByIdFallback_ThrowsUserServiceException() {
        // Given
        Long userId = 1L;
        Throwable throwable = new RuntimeException("Circuit breaker open");

        // When & Then
        UserServiceException exception = assertThrows(UserServiceException.class, () -> {
            resilientUserService.getUserByIdFallback(userId, throwable);
        });
        
        assertTrue(exception.getMessage().contains("unavailable"));
    }

    @Test
    void userExistsFallback_ThrowsUserServiceException() {
        // Given
        Long userId = 1L;
        Throwable throwable = new RuntimeException("Circuit breaker open");

        // When & Then
        UserServiceException exception = assertThrows(UserServiceException.class, () -> {
            resilientUserService.userExistsFallback(userId, throwable);
        });
        
        assertTrue(exception.getMessage().contains("unavailable"));
    }

    @Test
    void userExistsFallback_RethrowsUserNotFoundException() {
        // Given
        Long userId = 1L;
        UserServiceException notFoundException = new UserServiceException("User with ID 1 not found");

        // When & Then
        UserServiceException exception = assertThrows(UserServiceException.class, () -> {
            resilientUserService.userExistsFallback(userId, notFoundException);
        });
        
        assertSame(notFoundException, exception);
    }
}
