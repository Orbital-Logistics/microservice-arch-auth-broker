package org.orbitalLogistic.inventory.infrastructure.adapters.out.external;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.orbitalLogistic.inventory.clients.resilient.ResilientCargoServiceClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CargoValidationAdapterTest {

    @Mock
    private ResilientCargoServiceClient cargoServiceClient;

    @InjectMocks
    private CargoValidationAdapter cargoValidationAdapter;

    @Test
    @DisplayName("Should return true when cargo exists")
    void cargoExists_True() {
        when(cargoServiceClient.cargoExists(1L)).thenReturn(true);
        boolean result = cargoValidationAdapter.cargoExists(1L);
        assertTrue(result);
        verify(cargoServiceClient).cargoExists(1L);
    }

    @Test
    @DisplayName("Should return false when cargo not found")
    void cargoExists_False() {
        when(cargoServiceClient.cargoExists(999L)).thenReturn(false);
        boolean result = cargoValidationAdapter.cargoExists(999L);
        assertFalse(result);
    }

    @Test
    @DisplayName("Should return false when exception occurs")
    void cargoExists_Exception() {
        when(cargoServiceClient.cargoExists(1L)).thenThrow(new RuntimeException("Service unavailable"));
        boolean result = cargoValidationAdapter.cargoExists(1L);
        assertFalse(result);
    }

    @Test
    @DisplayName("Should return true when storage unit exists")
    void storageUnitExists_True() {
        when(cargoServiceClient.storageUnitExists(1L)).thenReturn(true);
        boolean result = cargoValidationAdapter.storageUnitExists(1L);
        assertTrue(result);
        verify(cargoServiceClient).storageUnitExists(1L);
    }

    @Test
    @DisplayName("Should return false when storage unit not found")
    void storageUnitExists_False() {
        when(cargoServiceClient.storageUnitExists(999L)).thenReturn(false);
        boolean result = cargoValidationAdapter.storageUnitExists(999L);
        assertFalse(result);
    }

    @Test
    @DisplayName("Should return false when storage unit check throws exception")
    void storageUnitExists_Exception() {
        when(cargoServiceClient.storageUnitExists(1L)).thenThrow(new RuntimeException("Service unavailable"));
        boolean result = cargoValidationAdapter.storageUnitExists(1L);
        assertFalse(result);
    }
}
