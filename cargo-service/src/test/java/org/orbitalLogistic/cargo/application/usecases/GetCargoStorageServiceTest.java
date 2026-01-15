package org.orbitalLogistic.cargo.application.usecases;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.orbitalLogistic.cargo.application.ports.out.CargoStorageRepository;
import org.orbitalLogistic.cargo.domain.model.CargoStorage;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetCargoStorageServiceTest {

    @Mock
    private CargoStorageRepository cargoStorageRepository;

    @InjectMocks
    private GetCargoStorageService getCargoStorageService;

    private CargoStorage storage;

    @BeforeEach
    void setUp() {
        storage = CargoStorage.builder()
                .id(1L)
                .storageUnitId(1L)
                .cargoId(1L)
                .quantity(100)
                .storedAt(LocalDateTime.now())
                .lastCheckedByUserId(1L)
                .build();
    }

    @Test
    void getStorageById_Success() {
        // Given
        when(cargoStorageRepository.findById(1L)).thenReturn(Optional.of(storage));

        // When
        Optional<CargoStorage> result = getCargoStorageService.getStorageById(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals(100, result.get().getQuantity());
        verify(cargoStorageRepository).findById(1L);
    }

    @Test
    void getStorageById_ReturnsEmpty_WhenNotFound() {
        // Given
        when(cargoStorageRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<CargoStorage> result = getCargoStorageService.getStorageById(999L);

        // Then
        assertFalse(result.isPresent());
        verify(cargoStorageRepository).findById(999L);
    }

    @Test
    void getAllStorages_Success() {
        // Given
        CargoStorage storage2 = CargoStorage.builder()
                .id(2L)
                .storageUnitId(2L)
                .cargoId(2L)
                .quantity(200)
                .storedAt(LocalDateTime.now())
                .build();

        when(cargoStorageRepository.findWithFilters(null, null, null, 20, 0))
                .thenReturn(Arrays.asList(storage, storage2));

        // When
        List<CargoStorage> result = getCargoStorageService.getAllStorages(0, 20);

        // Then
        assertEquals(2, result.size());
        assertEquals(100, result.get(0).getQuantity());
        assertEquals(200, result.get(1).getQuantity());
        verify(cargoStorageRepository).findWithFilters(null, null, null, 20, 0);
    }

    @Test
    void searchStorages_Success() {
        // Given
        when(cargoStorageRepository.findWithFilters(1L, 1L, null, 10, 0))
                .thenReturn(Arrays.asList(storage));

        // When
        List<CargoStorage> result = getCargoStorageService.searchStorages(1L, 1L, 0, 10);

        // Then
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getStorageUnitId());
        assertEquals(1L, result.get(0).getCargoId());
        verify(cargoStorageRepository).findWithFilters(1L, 1L, null, 10, 0);
    }
}
