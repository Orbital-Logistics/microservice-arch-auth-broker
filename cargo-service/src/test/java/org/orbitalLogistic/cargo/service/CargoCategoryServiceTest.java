package org.orbitalLogistic.cargo.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.orbitalLogistic.cargo.dto.request.CargoCategoryRequestDTO;
import org.orbitalLogistic.cargo.dto.response.CargoCategoryResponseDTO;
import org.orbitalLogistic.cargo.entities.CargoCategory;
import org.orbitalLogistic.cargo.exceptions.CargoCategoryNotFoundException;
import org.orbitalLogistic.cargo.mappers.CargoCategoryMapper;
import org.orbitalLogistic.cargo.repositories.CargoCategoryRepository;
import org.orbitalLogistic.cargo.services.CargoCategoryService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CargoCategoryServiceTest {

    @Mock
    private CargoCategoryRepository cargoCategoryRepository;

    @Mock
    private CargoCategoryMapper cargoCategoryMapper;

    @InjectMocks
    private CargoCategoryService cargoCategoryService;

    private CargoCategory testCategory;
    private CargoCategoryRequestDTO requestDTO;
    private CargoCategoryResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        testCategory = CargoCategory.builder()
                .id(1L)
                .name("Electronics")
                .parentCategoryId(null)
                .description("Electronic components")
                .build();

        requestDTO = new CargoCategoryRequestDTO(
                "Electronics",
                null,
                "Electronic components"
        );

        responseDTO = new CargoCategoryResponseDTO(
                1L,
                "Electronics",
                null,
                null,
                "Electronic components",
                new ArrayList<>(),
                0
        );
    }

    @Test
    void getAllCategories_Success() {
        List<CargoCategory> categories = List.of(testCategory);
        when(cargoCategoryRepository.findAll()).thenReturn(categories);
        when(cargoCategoryMapper.toResponseDTO(any(), any(), any(), anyInt())).thenReturn(responseDTO);

        List<CargoCategoryResponseDTO> result = cargoCategoryService.getAllCategories();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(cargoCategoryRepository).findAll();
    }

    @Test
    void getCategoryById_Success() {
        when(cargoCategoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(cargoCategoryMapper.toResponseDTO(any(), any(), any(), anyInt())).thenReturn(responseDTO);

        CargoCategoryResponseDTO result = cargoCategoryService.getCategoryById(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Electronics", result.name());
        verify(cargoCategoryRepository).findById(1L);
    }

    @Test
    void getCategoryById_NotFound() {
        when(cargoCategoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(CargoCategoryNotFoundException.class,
                () -> cargoCategoryService.getCategoryById(999L));
        verify(cargoCategoryRepository).findById(999L);
    }

    @Test
    void createCategory_Success() {
        when(cargoCategoryMapper.toEntity(requestDTO)).thenReturn(testCategory);
        when(cargoCategoryRepository.save(any(CargoCategory.class))).thenReturn(testCategory);
        when(cargoCategoryMapper.toResponseDTO(any(), any(), any(), anyInt())).thenReturn(responseDTO);

        CargoCategoryResponseDTO result = cargoCategoryService.createCategory(requestDTO);

        assertNotNull(result);
        assertEquals("Electronics", result.name());
        verify(cargoCategoryRepository).save(any(CargoCategory.class));
    }

    @Test
    void getCategoryTree_Success() {
        when(cargoCategoryRepository.findByParentCategoryIdIsNull()).thenReturn(List.of(testCategory));
        when(cargoCategoryRepository.findByParentCategoryId(1L)).thenReturn(new ArrayList<>());
        when(cargoCategoryMapper.toResponseDTO(any(), any(), any(), anyInt())).thenReturn(responseDTO);

        List<CargoCategoryResponseDTO> result = cargoCategoryService.getCategoryTree();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(cargoCategoryRepository).findByParentCategoryIdIsNull();
    }

    @Test
    void getEntityById_Success() {
        when(cargoCategoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));

        CargoCategory result = cargoCategoryService.getEntityById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(cargoCategoryRepository).findById(1L);
    }

    @Test
    void getEntityById_NotFound() {
        when(cargoCategoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(CargoCategoryNotFoundException.class,
                () -> cargoCategoryService.getEntityById(999L));
        verify(cargoCategoryRepository).findById(999L);
    }
}

