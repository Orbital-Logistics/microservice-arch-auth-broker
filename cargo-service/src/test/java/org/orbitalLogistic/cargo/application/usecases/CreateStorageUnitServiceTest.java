package org.orbitalLogistic.cargo.application.usecases;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.orbitalLogistic.cargo.application.ports.out.StorageUnitRepository;
import org.orbitalLogistic.cargo.domain.exception.StorageUnitAlreadyExistsException;
import org.orbitalLogistic.cargo.domain.model.StorageUnit;
import org.orbitalLogistic.cargo.domain.model.enums.StorageTypeEnum;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateStorageUnitServiceTest {

    @Mock
    private StorageUnitRepository storageUnitRepository;

    @InjectMocks
    private CreateStorageUnitService createStorageUnitService;

    private StorageUnit storageUnit;

    @BeforeEach
    void setUp() {
        storageUnit = StorageUnit.builder()
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
    void createUnit_Success() {
        // Given
        when(storageUnitRepository.existsByUnitCode("UNIT-001")).thenReturn(false);
        
        StorageUnit savedUnit = StorageUnit.builder()
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
        
        when(storageUnitRepository.save(any(StorageUnit.class))).thenReturn(savedUnit);

        // When
        StorageUnit result = createStorageUnitService.createUnit(storageUnit);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("UNIT-001", result.getUnitCode());
        assertEquals(StorageTypeEnum.AMBIENT, result.getStorageType());
        assertTrue(result.getIsActive());
        
        verify(storageUnitRepository).existsByUnitCode("UNIT-001");
        verify(storageUnitRepository).save(any(StorageUnit.class));
    }

    @Test
    void createUnit_ThrowsException_WhenUnitCodeExists() {
        // Given
        when(storageUnitRepository.existsByUnitCode("UNIT-001")).thenReturn(true);

        // When & Then
        assertThrows(StorageUnitAlreadyExistsException.class, () -> {
            createStorageUnitService.createUnit(storageUnit);
        });

        verify(storageUnitRepository).existsByUnitCode("UNIT-001");
        verify(storageUnitRepository, never()).save(any(StorageUnit.class));
    }

}
