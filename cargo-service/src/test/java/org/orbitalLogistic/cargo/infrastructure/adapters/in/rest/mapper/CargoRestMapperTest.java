package org.orbitalLogistic.cargo.infrastructure.adapters.in.rest.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.orbitalLogistic.cargo.application.ports.out.CargoCategoryRepository;
import org.orbitalLogistic.cargo.application.ports.out.CargoStorageRepository;
import org.orbitalLogistic.cargo.domain.model.Cargo;
import org.orbitalLogistic.cargo.domain.model.CargoCategory;
import org.orbitalLogistic.cargo.domain.model.enums.CargoType;
import org.orbitalLogistic.cargo.domain.model.enums.HazardLevel;
import org.orbitalLogistic.cargo.infrastructure.adapters.in.rest.dto.request.CreateCargoRequest;
import org.orbitalLogistic.cargo.infrastructure.adapters.in.rest.dto.request.UpdateCargoRequest;
import org.orbitalLogistic.cargo.infrastructure.adapters.in.rest.dto.response.CargoResponse;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CargoRestMapperTest {

    @Mock
    private CargoCategoryRepository cargoCategoryRepository;

    @Mock
    private CargoStorageRepository cargoStorageRepository;

    @InjectMocks
    private CargoRestMapper cargoRestMapper;

    private Cargo cargo;
    private CargoCategory category;

    @BeforeEach
    void setUp() {
        category = CargoCategory.builder()
                .id(1L)
                .name("Electronics")
                .build();

        cargo = Cargo.builder()
                .id(1L)
                .name("Laptop")
                .cargoCategoryId(1L)
                .cargoType(CargoType.EQUIPMENT)
                .hazardLevel(HazardLevel.LOW)
                .massPerUnit(BigDecimal.valueOf(2.5))
                .volumePerUnit(BigDecimal.valueOf(0.05))
                .isActive(true)
                .build();
    }

    @Test
    void toResponse_Success() {
        // Given
        when(cargoCategoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(cargoStorageRepository.sumQuantityByCargoId(1L)).thenReturn(100);

        // When
        CargoResponse response = cargoRestMapper.toResponse(cargo);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Laptop", response.getName());
        assertEquals("Electronics", response.getCargoCategoryName());
        assertEquals(100, response.getTotalQuantity());
        assertEquals(CargoType.EQUIPMENT, response.getCargoType());
        
        verify(cargoCategoryRepository).findById(1L);
        verify(cargoStorageRepository).sumQuantityByCargoId(1L);
    }

    @Test
    void toResponse_WithNullCategory() {
        // Given
        when(cargoCategoryRepository.findById(1L)).thenReturn(Optional.empty());
        when(cargoStorageRepository.sumQuantityByCargoId(1L)).thenReturn(null);

        // When
        CargoResponse response = cargoRestMapper.toResponse(cargo);

        // Then
        assertNotNull(response);
        assertNull(response.getCargoCategoryName());
        assertEquals(0, response.getTotalQuantity());
    }

    @Test
    void toResponse_WithNullInput() {
        // When
        CargoResponse response = cargoRestMapper.toResponse(null);

        // Then
        assertNull(response);
    }

    @Test
    void toDomain_FromCreateRequest() {
        // Given
        CreateCargoRequest request = CreateCargoRequest.builder()
                .name("Monitor")
                .cargoCategoryId(2L)
                .cargoType(CargoType.EQUIPMENT)
                .hazardLevel(HazardLevel.NONE)
                .massPerUnit(BigDecimal.valueOf(5.0))
                .volumePerUnit(BigDecimal.valueOf(0.1))
                .build();

        // When
        Cargo result = cargoRestMapper.toDomain(request);

        // Then
        assertNotNull(result);
        assertEquals("Monitor", result.getName());
        assertEquals(2L, result.getCargoCategoryId());
        assertEquals(CargoType.EQUIPMENT, result.getCargoType());
        assertTrue(result.getIsActive());
    }

    @Test
    void toDomain_FromUpdateRequest() {
        // Given
        UpdateCargoRequest request = UpdateCargoRequest.builder()
                .name("Updated Laptop")
                .cargoCategoryId(1L)
                .cargoType(CargoType.EQUIPMENT)
                .hazardLevel(HazardLevel.MEDIUM)
                .massPerUnit(BigDecimal.valueOf(3.0))
                .volumePerUnit(BigDecimal.valueOf(0.06))
                .isActive(false)
                .build();

        // When
        Cargo result = cargoRestMapper.toDomain(request, 1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Updated Laptop", result.getName());
        assertFalse(result.getIsActive());
    }

    @Test
    void toDomain_FromNullRequest() {
        // When
        Cargo result = cargoRestMapper.toDomain((CreateCargoRequest) null);

        // Then
        assertNull(result);
    }
}
