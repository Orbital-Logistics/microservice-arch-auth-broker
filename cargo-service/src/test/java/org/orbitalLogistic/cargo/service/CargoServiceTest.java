package org.orbitalLogistic.cargo.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.orbitalLogistic.cargo.dto.common.PageResponseDTO;
import org.orbitalLogistic.cargo.dto.request.CargoRequestDTO;
import org.orbitalLogistic.cargo.dto.response.CargoResponseDTO;
import org.orbitalLogistic.cargo.entities.Cargo;
import org.orbitalLogistic.cargo.entities.CargoCategory;
import org.orbitalLogistic.cargo.entities.enums.CargoType;
import org.orbitalLogistic.cargo.entities.enums.HazardLevel;
import org.orbitalLogistic.cargo.exceptions.CargoAlreadyExistsException;
import org.orbitalLogistic.cargo.exceptions.CargoNotFoundException;
import org.orbitalLogistic.cargo.mappers.CargoMapper;
import org.orbitalLogistic.cargo.repositories.CargoRepository;
import org.orbitalLogistic.cargo.services.CargoCategoryService;
import org.orbitalLogistic.cargo.services.CargoService;
import org.orbitalLogistic.cargo.services.CargoStorageService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CargoServiceTest {

    @Mock
    private CargoRepository cargoRepository;

    @Mock
    private CargoMapper cargoMapper;

    @Mock
    private CargoCategoryService cargoCategoryService;

    @Mock
    private CargoStorageService cargoStorageService;

    @InjectMocks
    private CargoService cargoService;

    private Cargo testCargo;
    private CargoCategory testCategory;
    private CargoRequestDTO requestDTO;
    private CargoResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        testCategory = CargoCategory.builder()
                .id(1L)
                .name("Electronics")
                .description("Electronic components")
                .build();

        testCargo = Cargo.builder()
                .id(1L)
                .name("Microchips")
                .cargoCategoryId(1L)
                .massPerUnit(new BigDecimal("0.50"))
                .volumePerUnit(new BigDecimal("0.01"))
                .cargoType(CargoType.EQUIPMENT)
                .hazardLevel(HazardLevel.NONE)
                .build();

        requestDTO = new CargoRequestDTO(
                "Microchips",
                1L,
                new BigDecimal("0.50"),
                new BigDecimal("0.01"),
                CargoType.EQUIPMENT,
                HazardLevel.NONE
        );

        responseDTO = new CargoResponseDTO(
                1L,
                "Microchips",
                "Electronics",
                new BigDecimal("0.50"),
                new BigDecimal("0.01"),
                CargoType.EQUIPMENT,
                HazardLevel.NONE,
                100
        );
    }

    @Test
    void getCargoById_Success() {
        when(cargoRepository.findById(1L)).thenReturn(Optional.of(testCargo));
        when(cargoCategoryService.getEntityById(1L)).thenReturn(testCategory);
        when(cargoStorageService.calculateTotalQuantityForCargo(1L)).thenReturn(100);
        when(cargoMapper.toResponseDTO(any(Cargo.class), anyString(), anyInt())).thenReturn(responseDTO);

        CargoResponseDTO result = cargoService.getCargoById(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Microchips", result.name());
        verify(cargoRepository).findById(1L);
        verify(cargoCategoryService).getEntityById(1L);
    }

    @Test
    void getCargoById_NotFound() {
        when(cargoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(CargoNotFoundException.class, () -> cargoService.getCargoById(999L));
        verify(cargoRepository).findById(999L);
    }

    @Test
    void createCargo_Success() {
        when(cargoRepository.existsByName("Microchips")).thenReturn(false);
        when(cargoCategoryService.getEntityById(1L)).thenReturn(testCategory);
        when(cargoMapper.toEntity(requestDTO)).thenReturn(testCargo);
        when(cargoRepository.save(any(Cargo.class))).thenReturn(testCargo);
        when(cargoStorageService.calculateTotalQuantityForCargo(1L)).thenReturn(0);
        when(cargoMapper.toResponseDTO(any(Cargo.class), anyString(), anyInt())).thenReturn(responseDTO);

        CargoResponseDTO result = cargoService.createCargo(requestDTO);

        assertNotNull(result);
        assertEquals("Microchips", result.name());
        verify(cargoRepository).existsByName("Microchips");
        verify(cargoRepository).save(any(Cargo.class));
    }

    @Test
    void createCargo_AlreadyExists() {
        when(cargoRepository.existsByName("Microchips")).thenReturn(true);

        assertThrows(CargoAlreadyExistsException.class, () -> cargoService.createCargo(requestDTO));
        verify(cargoRepository).existsByName("Microchips");
        verify(cargoRepository, never()).save(any(Cargo.class));
    }

    @Test
    void updateCargo_Success() {
        CargoRequestDTO updateRequest = new CargoRequestDTO(
                "Updated Microchips",
                1L,
                new BigDecimal("0.60"),
                new BigDecimal("0.02"),
                CargoType.EQUIPMENT,
                HazardLevel.NONE
        );

        when(cargoRepository.findById(1L)).thenReturn(Optional.of(testCargo));
        when(cargoRepository.existsByName("Updated Microchips")).thenReturn(false);
        when(cargoCategoryService.getEntityById(1L)).thenReturn(testCategory);
        when(cargoRepository.save(any(Cargo.class))).thenReturn(testCargo);
        when(cargoStorageService.calculateTotalQuantityForCargo(1L)).thenReturn(100);
        when(cargoMapper.toResponseDTO(any(Cargo.class), anyString(), anyInt())).thenReturn(responseDTO);

        CargoResponseDTO result = cargoService.updateCargo(1L, updateRequest);

        assertNotNull(result);
        verify(cargoRepository).findById(1L);
        verify(cargoRepository).save(any(Cargo.class));
    }

    @Test
    void updateCargo_NotFound() {
        when(cargoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(CargoNotFoundException.class, () -> cargoService.updateCargo(999L, requestDTO));
        verify(cargoRepository).findById(999L);
        verify(cargoRepository, never()).save(any(Cargo.class));
    }

    @Test
    void deleteCargo_Success() {
        when(cargoRepository.existsById(1L)).thenReturn(true);
        doNothing().when(cargoRepository).deleteById(1L);

        cargoService.deleteCargo(1L);

        verify(cargoRepository).existsById(1L);
        verify(cargoRepository).deleteById(1L);
    }

    @Test
    void deleteCargo_NotFound() {
        when(cargoRepository.existsById(999L)).thenReturn(false);

        assertThrows(CargoNotFoundException.class, () -> cargoService.deleteCargo(999L));
        verify(cargoRepository).existsById(999L);
        verify(cargoRepository, never()).deleteById(anyLong());
    }

    @Test
    void getCargosPaged_Success() {
        when(cargoRepository.findWithFilters(null, null, null, 20, 0))
                .thenReturn(List.of(testCargo));
        when(cargoRepository.countWithFilters(null, null, null)).thenReturn(1L);
        when(cargoCategoryService.getEntityById(1L)).thenReturn(testCategory);
        when(cargoStorageService.calculateTotalQuantityForCargo(1L)).thenReturn(100);
        when(cargoMapper.toResponseDTO(any(Cargo.class), anyString(), anyInt())).thenReturn(responseDTO);

        PageResponseDTO<CargoResponseDTO> result = cargoService.getCargosPaged(null, null, null, 0, 20);

        assertNotNull(result);
        assertEquals(1L, result.totalElements());
        assertEquals(1, result.content().size());
        assertTrue(result.first());
        assertTrue(result.last());
        verify(cargoRepository).findWithFilters(null, null, null, 20, 0);
    }

    @Test
    void searchCargos_WithFilters() {
        when(cargoRepository.findWithFilters("Micro", "EQUIPMENT", "NONE", 20, 0))
                .thenReturn(List.of(testCargo));
        when(cargoRepository.countWithFilters("Micro", "EQUIPMENT", "NONE")).thenReturn(1L);
        when(cargoCategoryService.getEntityById(1L)).thenReturn(testCategory);
        when(cargoStorageService.calculateTotalQuantityForCargo(1L)).thenReturn(100);
        when(cargoMapper.toResponseDTO(any(Cargo.class), anyString(), anyInt())).thenReturn(responseDTO);

        PageResponseDTO<CargoResponseDTO> result = cargoService.searchCargos("Micro", "EQUIPMENT", "NONE", 0, 20);

        assertNotNull(result);
        assertEquals(1L, result.totalElements());
        verify(cargoRepository).findWithFilters("Micro", "EQUIPMENT", "NONE", 20, 0);
    }

    @Test
    void cargoExists_True() {
        when(cargoRepository.existsById(1L)).thenReturn(true);

        boolean result = cargoService.cargoExists(1L);

        assertTrue(result);
        verify(cargoRepository).existsById(1L);
    }

    @Test
    void cargoExists_False() {
        when(cargoRepository.existsById(999L)).thenReturn(false);

        boolean result = cargoService.cargoExists(999L);

        assertFalse(result);
        verify(cargoRepository).existsById(999L);
    }
}

