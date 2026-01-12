package org.orbitalLogistic.inventory.infrastructure.adapters.out.external;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.orbitalLogistic.inventory.clients.resilient.ResilientUserService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserValidationAdapterTest {

    @Mock
    private ResilientUserService userService;

    @InjectMocks
    private UserValidationAdapter userValidationAdapter;

    @Test
    @DisplayName("Should return true when user exists")
    void userExists_True() {
        when(userService.userExists(1L)).thenReturn(true);
        boolean result = userValidationAdapter.userExists(1L);
        assertTrue(result);
        verify(userService).userExists(1L);
    }

    @Test
    @DisplayName("Should return false when user not found")
    void userExists_False() {
        when(userService.userExists(999L)).thenReturn(false);
        boolean result = userValidationAdapter.userExists(999L);
        assertFalse(result);
    }

    @Test
    @DisplayName("Should return false when exception occurs")
    void userExists_Exception() {
        when(userService.userExists(1L)).thenThrow(new RuntimeException("Service unavailable"));
        boolean result = userValidationAdapter.userExists(1L);
        assertFalse(result);
    }
}
