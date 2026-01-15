package org.orbitalLogistic.cargo.application.usecases;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.orbitalLogistic.cargo.application.ports.out.StorageUnitRepository;
import org.orbitalLogistic.cargo.domain.model.StorageUnit;
import org.orbitalLogistic.cargo.domain.model.enums.StorageTypeEnum;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetStorageUnitServiceTest {

    @Mock
    private StorageUnitRepository storageUnitRepository;

    @InjectMocks
    private GetStorageUnitService getStorageUnitService;

    private StorageUnit storageUnit;

    @BeforeEach
    void setUp() {
        storageUnit = StorageUnit.builder()
                .id(1L)
                .unitCode("UNIT-001")
                .storageType(StorageTypeEnum.AMBIENT)
                .location("Warehouse A")
                .maxMass(BigDecimal.valueOf(1000))
                .maxVolume(BigDecimal.valueOf(50))
                .currentMass(BigDecimal.ZERO)
                .currentVolume(BigDecimal.ZERO)
                .isActive(true)
                .build();
    }

    @Test
    void getUnitById_Success() {
        // Given
        when(storageUnitRepository.findById(1L)).thenReturn(Optional.of(storageUnit));

        // When
        Optional<StorageUnit> result = getStorageUnitService.getUnitById(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals("UNIT-001", result.get().getUnitCode());
        assertEquals(StorageTypeEnum.AMBIENT, result.get().getStorageType());
        verify(storageUnitRepository).findById(1L);
    }

    @Test
    void getUnitById_ReturnsEmpty_WhenNotFound() {
        // Given
        when(storageUnitRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<StorageUnit> result = getStorageUnitService.getUnitById(999L);

        // Then
        assertFalse(result.isPresent());
        verify(storageUnitRepository).findById(999L);
    }

    @Test
    void getAllUnits_Success() {
        // Given
        StorageUnit unit2 = StorageUnit.builder()
                .id(2L)
                .unitCode("UNIT-002")
                .storageType(StorageTypeEnum.PRESSURIZED)
                .location("Warehouse B")
                .maxMass(BigDecimal.valueOf(2000))
                .maxVolume(BigDecimal.valueOf(100))
                .currentMass(BigDecimal.ZERO)
                .currentVolume(BigDecimal.ZERO)
                .isActive(true)
                .build();

        when(storageUnitRepository.findAll())
                .thenReturn(Arrays.asList(storageUnit, unit2));

        // When
        List<StorageUnit> result = getStorageUnitService.getAllUnits(0, 20);

        // Then
        assertEquals(2, result.size());
        assertEquals("UNIT-001", result.get(0).getUnitCode());
        assertEquals("UNIT-002", result.get(1).getUnitCode());
        verify(storageUnitRepository).findAll();
    }

    @Test
    void getByUnitCode_Success() {
        // Given
        when(storageUnitRepository.findByUnitCode("UNIT-001")).thenReturn(Optional.of(storageUnit));

        // When
        Optional<StorageUnit> result = getStorageUnitService.getByUnitCode("UNIT-001");

        // Then
        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
        verify(storageUnitRepository).findByUnitCode("UNIT-001");
    }

    @Test
    void getByLocation_Success() {
        // Given
        when(storageUnitRepository.findByLocation("Warehouse A", 10, 0))
                .thenReturn(Arrays.asList(storageUnit));

        // When
        List<StorageUnit> result = getStorageUnitService.getByLocation("Warehouse A", 0, 10);

        // Then
        assertEquals(1, result.size());
        assertEquals("Warehouse A", result.get(0).getLocation());
        verify(storageUnitRepository).findByLocation("Warehouse A", 10, 0);
    }
}
