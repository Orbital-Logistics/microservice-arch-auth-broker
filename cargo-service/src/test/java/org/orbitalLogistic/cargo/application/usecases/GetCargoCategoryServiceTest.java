package org.orbitalLogistic.cargo.application.usecases;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.orbitalLogistic.cargo.application.ports.out.CargoCategoryRepository;
import org.orbitalLogistic.cargo.domain.model.CargoCategory;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetCargoCategoryServiceTest {

    @Mock
    private CargoCategoryRepository cargoCategoryRepository;

    @InjectMocks
    private GetCargoCategoryService getCargoCategoryService;

    private CargoCategory category;

    @BeforeEach
    void setUp() {
        category = CargoCategory.builder()
                .id(1L)
                .name("Electronics")
                .description("Electronic devices")
                .build();
    }

    @Test
    void getCategoryById_Success() {
        // Given
        when(cargoCategoryRepository.findById(1L)).thenReturn(Optional.of(category));

        // When
        Optional<CargoCategory> result = getCargoCategoryService.getCategoryById(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals("Electronics", result.get().getName());
        
        verify(cargoCategoryRepository).findById(1L);
    }

    @Test
    void getCategoryById_ReturnsEmpty_WhenNotFound() {
        // Given
        when(cargoCategoryRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<CargoCategory> result = getCargoCategoryService.getCategoryById(999L);

        // Then
        assertFalse(result.isPresent());
        verify(cargoCategoryRepository).findById(999L);
    }

    @Test
    void getAllCategories_Success() {
        // Given
        CargoCategory category2 = CargoCategory.builder()
                .id(2L)
                .name("Food")
                .build();
        
        when(cargoCategoryRepository.findAll()).thenReturn(Arrays.asList(category, category2));

        // When
        List<CargoCategory> result = getCargoCategoryService.getAllCategories();

        // Then
        assertEquals(2, result.size());
        assertEquals("Electronics", result.get(0).getName());
        assertEquals("Food", result.get(1).getName());
        
        verify(cargoCategoryRepository).findAll();
    }

    @Test
    void getCategoryTree_WithChildren_Success() {
        // Given
        CargoCategory childCategory = CargoCategory.builder()
                .id(2L)
                .name("Laptops")
                .parentCategoryId(1L)
                .build();
        
        when(cargoCategoryRepository.findByParentCategoryIdIsNull()).thenReturn(Arrays.asList(category));
        when(cargoCategoryRepository.findByParentCategoryId(1L)).thenReturn(Arrays.asList(childCategory));

        // When
        List<CargoCategory> result = getCargoCategoryService.getCategoryTree();

        // Then
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getChildren().size());
        assertEquals("Laptops", result.get(0).getChildren().get(0).getName());
        
        verify(cargoCategoryRepository).findByParentCategoryIdIsNull();
        verify(cargoCategoryRepository).findByParentCategoryId(1L);
    }
}
