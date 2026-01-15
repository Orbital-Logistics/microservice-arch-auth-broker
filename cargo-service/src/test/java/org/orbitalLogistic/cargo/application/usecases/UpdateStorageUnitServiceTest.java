package org.orbitalLogistic.cargo.application.usecases;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.orbitalLogistic.cargo.application.ports.out.StorageUnitRepository;
import org.orbitalLogistic.cargo.domain.exception.StorageUnitNotFoundException;
import org.orbitalLogistic.cargo.domain.model.StorageUnit;
import org.orbitalLogistic.cargo.domain.model.enums.StorageTypeEnum;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateStorageUnitServiceTest {

    @Mock
    private StorageUnitRepository storageUnitRepository;

    @InjectMocks
    private UpdateStorageUnitService updateStorageUnitService;

    private StorageUnit existingUnit;
    private StorageUnit updateData;

    @BeforeEach
    void setUp() {
        existingUnit = StorageUnit.builder()
                .id(1L)
                .unitCode("UNIT-001")
                .storageType(StorageTypeEnum.AMBIENT)
                .location("Warehouse A")
                .maxMass(BigDecimal.valueOf(1000))
                .maxVolume(BigDecimal.valueOf(50))
                .currentMass(BigDecimal.valueOf(100))
                .currentVolume(BigDecimal.valueOf(10))
                .isActive(true)
                .build();

        updateData = StorageUnit.builder()
                .id(1L)
                .unitCode("UNIT-001-UPD")
                .storageType(StorageTypeEnum.PRESSURIZED)
                .location("Warehouse B")
                .maxMass(BigDecimal.valueOf(1500))
                .maxVolume(BigDecimal.valueOf(75))
                .build();
    }

    @Test
    void updateUnit_Success() {
        // Given
        when(storageUnitRepository.findById(1L)).thenReturn(java.util.Optional.of(existingUnit));
        
        StorageUnit updated = StorageUnit.builder()
                .id(1L)
                .unitCode("UNIT-001-UPD")
                .storageType(StorageTypeEnum.PRESSURIZED)
                .location("Warehouse B")
                .maxMass(BigDecimal.valueOf(1500))
                .maxVolume(BigDecimal.valueOf(75))
                .currentMass(BigDecimal.valueOf(100))
                .currentVolume(BigDecimal.valueOf(10))
                .isActive(true)
                .build();
        
        when(storageUnitRepository.save(any(StorageUnit.class))).thenReturn(updated);

        // When
        StorageUnit result = updateStorageUnitService.updateUnit(1L, updateData);

        // Then
        assertNotNull(result);
        assertEquals("UNIT-001-UPD", result.getUnitCode());
        assertEquals("Warehouse B", result.getLocation());
        assertEquals(StorageTypeEnum.PRESSURIZED, result.getStorageType());
        
        verify(storageUnitRepository).findById(1L);
        verify(storageUnitRepository).save(any(StorageUnit.class));
    }

    @Test
    void updateUnit_ThrowsException_WhenNotFound() {
        // Given
        when(storageUnitRepository.findById(999L)).thenReturn(java.util.Optional.empty());

        // When & Then
        assertThrows(StorageUnitNotFoundException.class, () -> {
            updateStorageUnitService.updateUnit(999L, updateData);
        });

        verify(storageUnitRepository).findById(999L);
        verify(storageUnitRepository, never()).save(any(StorageUnit.class));
    }
}
