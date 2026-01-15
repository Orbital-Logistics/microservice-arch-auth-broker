package org.orbitalLogistic.cargo.infrastructure.adapters.out.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.orbitalLogistic.cargo.clients.ResilientUserService;
import org.orbitalLogistic.cargo.domain.exception.UserNotFoundException;
import org.orbitalLogistic.cargo.domain.exception.UserServiceException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceAdapterTest {

    @Mock
    private ResilientUserService resilientUserService;

    @InjectMocks
    private UserServiceAdapter userServiceAdapter;

    @Test
    void userExists_Success_ReturnsTrue() {
        // Given
        Long userId = 1L;
        when(resilientUserService.userExists(userId)).thenReturn(true);

        // When
        boolean result = userServiceAdapter.userExists(userId);

        // Then
        assertTrue(result);
        verify(resilientUserService).userExists(userId);
    }

    @Test
    void userExists_Success_ReturnsFalse() {
        // Given
        Long userId = 999L;
        when(resilientUserService.userExists(userId)).thenReturn(false);

        // When
        boolean result = userServiceAdapter.userExists(userId);

        // Then
        assertFalse(result);
        verify(resilientUserService).userExists(userId);
    }

    @Test
    void userExists_ReturnsFalse_WhenNull() {
        // Given
        Long userId = 1L;
        when(resilientUserService.userExists(userId)).thenReturn(null);

        // When
        boolean result = userServiceAdapter.userExists(userId);

        // Then
        assertFalse(result);
        verify(resilientUserService).userExists(userId);
    }

    @Test
    void userExists_ThrowsException_WhenServiceFails() {
        // Given
        Long userId = 1L;
        when(resilientUserService.userExists(userId))
                .thenThrow(new RuntimeException("Service unavailable"));

        // When & Then
        UserServiceException exception = assertThrows(UserServiceException.class, () -> {
            userServiceAdapter.userExists(userId);
        });

        assertTrue(exception.getMessage().contains("unavailable"));
        verify(resilientUserService).userExists(userId);
    }

    @Test
    void getUserById_Success() {
        // Given
        Long userId = 1L;
        String expectedUsername = "john_doe";
        when(resilientUserService.getUserById(userId)).thenReturn(expectedUsername);

        // When
        String result = userServiceAdapter.getUserById(userId);

        // Then
        assertEquals(expectedUsername, result);
        verify(resilientUserService).getUserById(userId);
    }

    @Test
    void getUserById_ThrowsException_WhenUserNotFound() {
        // Given
        Long userId = 999L;
        when(resilientUserService.getUserById(userId)).thenReturn(null);

        // When & Then
        UserServiceException exception = assertThrows(UserServiceException.class, () -> {
            userServiceAdapter.getUserById(userId);
        });

        assertTrue(exception.getMessage().contains("unavailable"));
        verify(resilientUserService).getUserById(userId);
    }

    @Test
    void getUserById_ThrowsException_WhenServiceFails() {
        // Given
        Long userId = 1L;
        when(resilientUserService.getUserById(userId))
                .thenThrow(new RuntimeException("Service unavailable"));

        // When & Then
        UserServiceException exception = assertThrows(UserServiceException.class, () -> {
            userServiceAdapter.getUserById(userId);
        });

        assertTrue(exception.getMessage().contains("unavailable"));
        verify(resilientUserService).getUserById(userId);
    }

    @Test
    void getUserById_ThrowsException_WhenUserServiceExceptionOccurs() {
        // Given
        Long userId = 1L;
        UserServiceException originalException = new UserServiceException("User service down");
        when(resilientUserService.getUserById(userId)).thenThrow(originalException);

        // When & Then
        UserServiceException exception = assertThrows(UserServiceException.class, () -> {
            userServiceAdapter.getUserById(userId);
        });

        assertTrue(exception.getMessage().contains("unavailable"));
        verify(resilientUserService).getUserById(userId);
    }
}
