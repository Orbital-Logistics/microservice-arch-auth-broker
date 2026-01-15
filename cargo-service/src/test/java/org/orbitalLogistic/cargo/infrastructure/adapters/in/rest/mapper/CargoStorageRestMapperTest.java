package org.orbitalLogistic.cargo.infrastructure.adapters.in.rest.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.orbitalLogistic.cargo.application.ports.out.CargoRepository;
import org.orbitalLogistic.cargo.application.ports.out.StorageUnitRepository;
import org.orbitalLogistic.cargo.application.ports.out.UserServicePort;
import org.orbitalLogistic.cargo.domain.model.Cargo;
import org.orbitalLogistic.cargo.domain.model.CargoStorage;
import org.orbitalLogistic.cargo.domain.model.StorageUnit;
import org.orbitalLogistic.cargo.domain.model.enums.CargoType;
import org.orbitalLogistic.cargo.domain.model.enums.HazardLevel;
import org.orbitalLogistic.cargo.domain.model.enums.StorageTypeEnum;
import org.orbitalLogistic.cargo.infrastructure.adapters.in.rest.dto.request.CreateCargoStorageRequest;
import org.orbitalLogistic.cargo.infrastructure.adapters.in.rest.dto.response.CargoStorageResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CargoStorageRestMapperTest {

    @Mock
    private CargoRepository cargoRepository;

    @Mock
    private StorageUnitRepository storageUnitRepository;

    @Mock
    private UserServicePort userServicePort;

    @InjectMocks
    private CargoStorageRestMapper cargoStorageRestMapper;

    private Cargo cargo;
    private StorageUnit storageUnit;
    private CargoStorage cargoStorage;

    @BeforeEach
    void setUp() {
        cargo = Cargo.builder()
                .id(1L)
                .name("Laptop")
                .cargoCategoryId(1L)
                .cargoType(CargoType.EQUIPMENT)
                .hazardLevel(HazardLevel.LOW)
                .massPerUnit(BigDecimal.valueOf(2.5))
                .volumePerUnit(BigDecimal.valueOf(0.05))
                .build();

        storageUnit = StorageUnit.builder()
                .id(1L)
                .unitCode("UNIT-001")
                .location("Warehouse A")
                .storageType(StorageTypeEnum.AMBIENT)
                .maxMass(BigDecimal.valueOf(1000))
                .maxVolume(BigDecimal.valueOf(50))
                .build();

        cargoStorage = CargoStorage.builder()
                .id(1L)
                .cargoId(1L)
                .storageUnitId(1L)
                .quantity(50)
                .lastCheckedByUserId(100L)
                .lastInventoryCheck(LocalDateTime.now())
                .build();
    }

    @Test
    void toResponse_Success() {
        // Given
        when(cargoRepository.findById(1L)).thenReturn(Optional.of(cargo));
        when(storageUnitRepository.findById(1L)).thenReturn(Optional.of(storageUnit));
        when(userServicePort.getUserById(100L)).thenReturn("John Doe");

        // When
        CargoStorageResponse response = cargoStorageRestMapper.toResponse(cargoStorage);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(50, response.getQuantity());
        assertEquals("Laptop", response.getCargoName());
        assertEquals("UNIT-001", response.getStorageUnitCode());
        
        verify(cargoRepository).findById(1L);
        verify(storageUnitRepository, times(2)).findById(1L);
        verify(userServicePort).getUserById(100L);
    }

    @Test
    void toResponse_WithNullCargoAndUnit() {
        // Given
        when(cargoRepository.findById(1L)).thenReturn(Optional.empty());
        when(storageUnitRepository.findById(1L)).thenReturn(Optional.empty());
        when(userServicePort.getUserById(100L)).thenReturn(null);

        // When
        CargoStorageResponse response = cargoStorageRestMapper.toResponse(cargoStorage);

        // Then
        assertNotNull(response);
        assertNull(response.getCargoName());
        assertNull(response.getStorageUnitCode());
    }

    @Test
    void toResponse_WithNullInput() {
        // When
        CargoStorageResponse response = cargoStorageRestMapper.toResponse(null);

        // Then
        assertNull(response);
    }

    @Test
    void toDomain_FromCreateRequest() {
        // Given
        CreateCargoStorageRequest request = CreateCargoStorageRequest.builder()
                .cargoId(2L)
                .storageUnitId(2L)
                .quantity(100)
                .updatedByUserId(200L)
                .build();

        // When
        CargoStorage result = cargoStorageRestMapper.toDomain(request);

        // Then
        assertNotNull(result);
        assertEquals(2L, result.getCargoId());
        assertEquals(2L, result.getStorageUnitId());
        assertEquals(100, result.getQuantity());
    }

    @Test
    void toDomain_FromNullRequest() {
        // When
        CargoStorage result = cargoStorageRestMapper.toDomain((CreateCargoStorageRequest) null);

        // Then
        assertNull(result);
    }

}

