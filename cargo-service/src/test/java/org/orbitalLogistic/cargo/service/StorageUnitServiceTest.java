package org.orbitalLogistic.cargo.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.orbitalLogistic.cargo.dto.common.PageResponseDTO;
import org.orbitalLogistic.cargo.dto.request.StorageUnitRequestDTO;
import org.orbitalLogistic.cargo.dto.response.CargoStorageResponseDTO;
import org.orbitalLogistic.cargo.dto.response.StorageUnitResponseDTO;
import org.orbitalLogistic.cargo.entities.StorageUnit;
import org.orbitalLogistic.cargo.entities.enums.StorageTypeEnum;
import org.orbitalLogistic.cargo.exceptions.StorageUnitAlreadyExistsException;
import org.orbitalLogistic.cargo.exceptions.StorageUnitNotFoundException;
import org.orbitalLogistic.cargo.mappers.StorageUnitMapper;
import org.orbitalLogistic.cargo.repositories.CargoStorageRepository;
import org.orbitalLogistic.cargo.repositories.StorageUnitRepository;
import org.orbitalLogistic.cargo.services.CargoStorageService;
import org.orbitalLogistic.cargo.services.StorageUnitService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StorageUnitServiceTest {

    @Mock
    private StorageUnitRepository storageUnitRepository;

    @Mock
    private StorageUnitMapper storageUnitMapper;

    @Mock
    private CargoStorageRepository cargoStorageRepository;

    @Mock
    private CargoStorageService cargoStorageService;

    @InjectMocks
    private StorageUnitService storageUnitService;

    private StorageUnit testUnit;
    private StorageUnitRequestDTO requestDTO;
    private StorageUnitResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        testUnit = StorageUnit.builder()
                .id(1L)
                .unitCode("SU-001")
                .location("Warehouse A")
                .storageType(StorageTypeEnum.AMBIENT)
                .totalMassCapacity(new BigDecimal("10000.00"))
                .totalVolumeCapacity(new BigDecimal("500.00"))
                .currentMass(new BigDecimal("5000.00"))
                .currentVolume(new BigDecimal("250.00"))
                .build();

        requestDTO = new StorageUnitRequestDTO(
                "SU-001",
                "Warehouse A",
                StorageTypeEnum.AMBIENT,
                new BigDecimal("10000.00"),
                new BigDecimal("500.00")
        );

        responseDTO = new StorageUnitResponseDTO(
                1L,
                "SU-001",
                "Warehouse A",
                StorageTypeEnum.AMBIENT,
                new BigDecimal("10000.00"),
                new BigDecimal("500.00"),
                new BigDecimal("5000.00"),
                new BigDecimal("250.00"),
                new BigDecimal("5000.00"),
                new BigDecimal("250.00"),
                50.0,
                50.0
        );
    }

    @Test
    void getStorageUnitById_Success() {
        when(storageUnitRepository.findById(1L)).thenReturn(Optional.of(testUnit));
        when(storageUnitMapper.toResponseDTO(any(), any(), any(), anyDouble(), anyDouble())).thenReturn(responseDTO);

        StorageUnitResponseDTO result = storageUnitService.getStorageUnitById(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("SU-001", result.unitCode());
        verify(storageUnitRepository).findById(1L);
    }

    @Test
    void getStorageUnitById_NotFound() {
        when(storageUnitRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(StorageUnitNotFoundException.class,
                () -> storageUnitService.getStorageUnitById(999L));
        verify(storageUnitRepository).findById(999L);
    }

    @Test
    void createStorageUnit_Success() {
        when(storageUnitRepository.existsByUnitCode("SU-001")).thenReturn(false);
        when(storageUnitMapper.toEntity(requestDTO)).thenReturn(testUnit);
        when(storageUnitRepository.save(any(StorageUnit.class))).thenReturn(testUnit);
        when(storageUnitMapper.toResponseDTO(any(), any(), any(), anyDouble(), anyDouble())).thenReturn(responseDTO);

        StorageUnitResponseDTO result = storageUnitService.createStorageUnit(requestDTO);

        assertNotNull(result);
        assertEquals("SU-001", result.unitCode());
        verify(storageUnitRepository).existsByUnitCode("SU-001");
        verify(storageUnitRepository).save(any(StorageUnit.class));
    }

    @Test
    void createStorageUnit_AlreadyExists() {
        when(storageUnitRepository.existsByUnitCode("SU-001")).thenReturn(true);

        assertThrows(StorageUnitAlreadyExistsException.class,
                () -> storageUnitService.createStorageUnit(requestDTO));
        verify(storageUnitRepository).existsByUnitCode("SU-001");
        verify(storageUnitRepository, never()).save(any(StorageUnit.class));
    }

    @Test
    void updateStorageUnit_Success() {
        StorageUnitRequestDTO updateRequest = new StorageUnitRequestDTO(
                "SU-001-UPDATED",
                "Warehouse B",
                StorageTypeEnum.PRESSURIZED,
                new BigDecimal("15000.00"),
                new BigDecimal("600.00")
        );

        when(storageUnitRepository.findById(1L)).thenReturn(Optional.of(testUnit));
        when(storageUnitRepository.existsByUnitCode("SU-001-UPDATED")).thenReturn(false);
        when(storageUnitRepository.save(any(StorageUnit.class))).thenReturn(testUnit);
        when(storageUnitMapper.toResponseDTO(any(), any(), any(), anyDouble(), anyDouble())).thenReturn(responseDTO);

        StorageUnitResponseDTO result = storageUnitService.updateStorageUnit(1L, updateRequest);

        assertNotNull(result);
        verify(storageUnitRepository).findById(1L);
        verify(storageUnitRepository).save(any(StorageUnit.class));
    }

    @Test
    void updateStorageUnit_NotFound() {
        when(storageUnitRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(StorageUnitNotFoundException.class,
                () -> storageUnitService.updateStorageUnit(999L, requestDTO));
        verify(storageUnitRepository).findById(999L);
        verify(storageUnitRepository, never()).save(any(StorageUnit.class));
    }

    @Test
    void getStorageUnits_Success() {
        when(storageUnitRepository.findAllPaged(20, 0)).thenReturn(List.of(testUnit));
        when(storageUnitRepository.countAll()).thenReturn(1L);
        when(storageUnitMapper.toResponseDTO(any(), any(), any(), anyDouble(), anyDouble())).thenReturn(responseDTO);

        PageResponseDTO<StorageUnitResponseDTO> result = storageUnitService.getStorageUnits(0, 20);

        assertNotNull(result);
        assertEquals(1L, result.totalElements());
        assertEquals(1, result.content().size());
        verify(storageUnitRepository).findAllPaged(20, 0);
    }

    @Test
    void getStorageUnitInventory_Success() {
        when(storageUnitRepository.existsById(1L)).thenReturn(true);
        when(cargoStorageService.getStorageUnitCargo(1L, 0, 20)).thenReturn(new PageResponseDTO<>(List.of(), 0, 20, 0L, 0, true, true));

        PageResponseDTO<CargoStorageResponseDTO> result = storageUnitService.getStorageUnitInventory(1L, 0, 20);

        assertNotNull(result);
        verify(storageUnitRepository).existsById(1L);
        verify(cargoStorageService).getStorageUnitCargo(1L, 0, 20);
    }

    @Test
    void getStorageUnitInventory_NotFound() {
        when(storageUnitRepository.existsById(999L)).thenReturn(false);

        assertThrows(StorageUnitNotFoundException.class,
                () -> storageUnitService.getStorageUnitInventory(999L, 0, 20));
        verify(storageUnitRepository).existsById(999L);
        verify(cargoStorageService, never()).getStorageUnitCargo(anyLong(), anyInt(), anyInt());
    }
}

