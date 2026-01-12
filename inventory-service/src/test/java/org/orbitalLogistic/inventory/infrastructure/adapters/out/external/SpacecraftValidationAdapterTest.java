package org.orbitalLogistic.inventory.infrastructure.adapters.out.external;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.orbitalLogistic.inventory.clients.resilient.ResilientSpacecraftService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpacecraftValidationAdapterTest {

    @Mock
    private ResilientSpacecraftService spacecraftService;

    @InjectMocks
    private SpacecraftValidationAdapter spacecraftValidationAdapter;

    @Test
    @DisplayName("Should return true when spacecraft exists")
    void spacecraftExists_True() {
        when(spacecraftService.spacecraftExists(1L)).thenReturn(true);
        boolean result = spacecraftValidationAdapter.spacecraftExists(1L);
        assertTrue(result);
        verify(spacecraftService).spacecraftExists(1L);
    }

    @Test
    @DisplayName("Should return false when spacecraft not found")
    void spacecraftExists_False() {
        when(spacecraftService.spacecraftExists(999L)).thenReturn(false);
        boolean result = spacecraftValidationAdapter.spacecraftExists(999L);
        assertFalse(result);
    }

    @Test
    @DisplayName("Should return false when exception occurs")
    void spacecraftExists_Exception() {
        when(spacecraftService.spacecraftExists(1L)).thenThrow(new RuntimeException("Service unavailable"));
        boolean result = spacecraftValidationAdapter.spacecraftExists(1L);
        assertFalse(result);
    }
}
