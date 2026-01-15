package org.orbitalLogistic.cargo.infrastructure.adapters.in.rest.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.orbitalLogistic.cargo.domain.model.StorageUnit;
import org.orbitalLogistic.cargo.domain.model.enums.StorageTypeEnum;
import org.orbitalLogistic.cargo.infrastructure.adapters.in.rest.dto.request.CreateStorageUnitRequest;
import org.orbitalLogistic.cargo.infrastructure.adapters.in.rest.dto.request.UpdateStorageUnitRequest;
import org.orbitalLogistic.cargo.infrastructure.adapters.in.rest.dto.response.StorageUnitResponse;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class StorageUnitRestMapperTest {

    private StorageUnitRestMapper mapper;
    private StorageUnit storageUnit;

    @BeforeEach
    void setUp() {
        mapper = new StorageUnitRestMapper();
        
        storageUnit = StorageUnit.builder()
                .id(1L)
                .unitCode("UNIT-001")
                .location("Warehouse A")
                .storageType(StorageTypeEnum.AMBIENT)
                .maxMass(BigDecimal.valueOf(1000))
                .maxVolume(BigDecimal.valueOf(50))
                .currentMass(BigDecimal.valueOf(100))
                .currentVolume(BigDecimal.valueOf(10))
                .isActive(true)
                .build();
    }

    @Test
    void toResponse_Success() {
        // When
        StorageUnitResponse response = mapper.toResponse(storageUnit);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("UNIT-001", response.getUnitCode());
        assertEquals("Warehouse A", response.getLocation());
        assertEquals(StorageTypeEnum.AMBIENT, response.getStorageType());
        assertEquals(BigDecimal.valueOf(900), response.getAvailableMassCapacity());
        assertEquals(BigDecimal.valueOf(40), response.getAvailableVolumeCapacity());
        assertEquals(10.0, response.getMassUsagePercentage(), 0.01);
        assertEquals(20.0, response.getVolumeUsagePercentage(), 0.01);
    }

    @Test
    void toResponse_WithNullInput() {
        // When
        StorageUnitResponse response = mapper.toResponse(null);

        // Then
        assertNull(response);
    }

    @Test
    void toDomain_FromCreateRequest() {
        // Given
        CreateStorageUnitRequest request = CreateStorageUnitRequest.builder()
                .unitCode("UNIT-002")
                .location("Warehouse B")
                .storageType(StorageTypeEnum.PRESSURIZED)
                .totalMassCapacity(BigDecimal.valueOf(2000))
                .totalVolumeCapacity(BigDecimal.valueOf(100))
                .build();

        // When
        StorageUnit result = mapper.toDomain(request);

        // Then
        assertNotNull(result);
        assertEquals("UNIT-002", result.getUnitCode());
        assertEquals("Warehouse B", result.getLocation());
        assertEquals(StorageTypeEnum.PRESSURIZED, result.getStorageType());
        assertEquals(BigDecimal.ZERO, result.getCurrentMass());
        assertEquals(BigDecimal.ZERO, result.getCurrentVolume());
        assertTrue(result.getIsActive());
    }

    @Test
    void toDomain_FromUpdateRequest() {
        // Given
        UpdateStorageUnitRequest request = UpdateStorageUnitRequest.builder()
                .unitCode("UNIT-001-UPD")
                .location("Warehouse C")
                .storageType(StorageTypeEnum.REFRIGERATED)
                .totalMassCapacity(BigDecimal.valueOf(1500))
                .totalVolumeCapacity(BigDecimal.valueOf(75))
                .build();

        // When
        StorageUnit result = mapper.toDomain(request, 1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("UNIT-001-UPD", result.getUnitCode());
        assertEquals("Warehouse C", result.getLocation());
        assertEquals(StorageTypeEnum.REFRIGERATED, result.getStorageType());
    }

    @Test
    void toDomain_FromNullRequest() {
        // When
        StorageUnit result = mapper.toDomain((CreateStorageUnitRequest) null);

        // Then
        assertNull(result);
    }
}
