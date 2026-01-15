package org.orbitalLogistic.cargo.application.usecases;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.orbitalLogistic.cargo.application.ports.out.CargoCategoryRepository;
import org.orbitalLogistic.cargo.domain.model.CargoCategory;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateCargoCategoryServiceTest {

    @Mock
    private CargoCategoryRepository cargoCategoryRepository;

    @InjectMocks
    private CreateCargoCategoryService createCargoCategoryService;

    private CargoCategory category;

    @BeforeEach
    void setUp() {
        category = CargoCategory.builder()
                .name("Electronics")
                .description("Electronic devices")
                .parentCategoryId(null)
                .build();
    }

    @Test
    void createCategory_Success() {
        // Given
        CargoCategory savedCategory = CargoCategory.builder()
                .id(1L)
                .name("Electronics")
                .description("Electronic devices")
                .parentCategoryId(null)
                .build();
        
        when(cargoCategoryRepository.save(any(CargoCategory.class))).thenReturn(savedCategory);

        // When
        CargoCategory result = createCargoCategoryService.createCategory(category);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Electronics", result.getName());
        
        verify(cargoCategoryRepository).save(any(CargoCategory.class));
    }

    @Test
    void createCategory_Success_WithParentCategory() {
        // Given
        category.setParentCategoryId(1L);
        
        CargoCategory parentCategory = CargoCategory.builder()
                .id(1L)
                .name("Parent Category")
                .build();
        
        when(cargoCategoryRepository.existsById(1L)).thenReturn(true);
        
        CargoCategory savedCategory = CargoCategory.builder()
                .id(2L)
                .name("Electronics")
                .parentCategoryId(1L)
                .build();
        
        when(cargoCategoryRepository.save(any(CargoCategory.class))).thenReturn(savedCategory);

        // When
        CargoCategory result = createCargoCategoryService.createCategory(category);

        // Then
        assertNotNull(result);
        assertEquals(2L, result.getId());
        assertEquals(1L, result.getParentCategoryId());
        
        verify(cargoCategoryRepository).existsById(1L);
        verify(cargoCategoryRepository).save(any(CargoCategory.class));
    }
}
