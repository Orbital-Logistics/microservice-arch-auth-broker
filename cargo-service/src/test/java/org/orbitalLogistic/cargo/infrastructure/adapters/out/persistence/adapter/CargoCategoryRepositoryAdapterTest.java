package org.orbitalLogistic.cargo.infrastructure.adapters.out.persistence.adapter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.orbitalLogistic.cargo.domain.model.CargoCategory;
import org.orbitalLogistic.cargo.infrastructure.adapters.out.persistence.CargoCategoryRepositoryAdapter;
import org.orbitalLogistic.cargo.infrastructure.adapters.out.persistence.entity.CargoCategoryEntity;
import org.orbitalLogistic.cargo.infrastructure.adapters.out.persistence.mapper.CargoCategoryPersistenceMapper;
import org.orbitalLogistic.cargo.infrastructure.adapters.out.persistence.repository.CargoCategoryJdbcRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CargoCategoryRepositoryAdapterTest {

    @Mock
    private CargoCategoryJdbcRepository jdbcRepository;

    @Mock
    private CargoCategoryPersistenceMapper mapper;

    @InjectMocks
    private CargoCategoryRepositoryAdapter adapter;

    private CargoCategory category;
    private CargoCategoryEntity categoryEntity;

    @BeforeEach
    void setUp() {
        category = CargoCategory.builder()
                .id(1L)
                .name("Electronics")
                .description("Electronic equipment")
                .parentCategoryId(null)
                .build();

        categoryEntity = new CargoCategoryEntity();
        categoryEntity.setId(1L);
        categoryEntity.setName("Electronics");
        categoryEntity.setDescription("Electronic equipment");
        categoryEntity.setParentCategoryId(null);
    }

    @Test
    void save_Success() {
        // Given
        when(mapper.toEntity(category)).thenReturn(categoryEntity);
        when(jdbcRepository.save(categoryEntity)).thenReturn(categoryEntity);
        when(mapper.toDomain(categoryEntity)).thenReturn(category);

        // When
        CargoCategory result = adapter.save(category);

        // Then
        assertNotNull(result);
        assertEquals("Electronics", result.getName());
        verify(jdbcRepository).save(categoryEntity);
    }

    @Test
    void findById_Success() {
        // Given
        when(jdbcRepository.findById(1L)).thenReturn(Optional.of(categoryEntity));
        when(mapper.toDomain(categoryEntity)).thenReturn(category);

        // When
        Optional<CargoCategory> result = adapter.findById(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals("Electronics", result.get().getName());
        verify(jdbcRepository).findById(1L);
    }

    @Test
    void findById_NotFound() {
        // Given
        when(jdbcRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<CargoCategory> result = adapter.findById(999L);

        // Then
        assertFalse(result.isPresent());
        verify(jdbcRepository).findById(999L);
        verify(mapper, never()).toDomain(any());
    }

    @Test
    void findAll_Success() {
        // Given
        when(jdbcRepository.findAll()).thenReturn(Arrays.asList(categoryEntity));
        when(mapper.toDomain(categoryEntity)).thenReturn(category);

        // When
        List<CargoCategory> result = adapter.findAll();

        // Then
        assertEquals(1, result.size());
        assertEquals("Electronics", result.get(0).getName());
        verify(jdbcRepository).findAll();
    }

    @Test
    void findByParentCategoryIdIsNull_Success() {
        // Given
        when(jdbcRepository.findByParentCategoryIdIsNull()).thenReturn(Arrays.asList(categoryEntity));
        when(mapper.toDomain(categoryEntity)).thenReturn(category);

        // When
        List<CargoCategory> result = adapter.findByParentCategoryIdIsNull();

        // Then
        assertEquals(1, result.size());
        assertNull(result.get(0).getParentCategoryId());
        verify(jdbcRepository).findByParentCategoryIdIsNull();
    }

    @Test
    void findByParentCategoryId_Success() {
        // Given
        Long parentId = 1L;
        CargoCategory subCategory = CargoCategory.builder()
                .id(2L)
                .name("Laptops")
                .parentCategoryId(parentId)
                .build();
        
        CargoCategoryEntity subCategoryEntity = new CargoCategoryEntity();
        subCategoryEntity.setId(2L);
        subCategoryEntity.setName("Laptops");
        subCategoryEntity.setParentCategoryId(parentId);

        when(jdbcRepository.findByParentCategoryId(parentId)).thenReturn(Arrays.asList(subCategoryEntity));
        when(mapper.toDomain(subCategoryEntity)).thenReturn(subCategory);

        // When
        List<CargoCategory> result = adapter.findByParentCategoryId(parentId);

        // Then
        assertEquals(1, result.size());
        assertEquals(parentId, result.get(0).getParentCategoryId());
        verify(jdbcRepository).findByParentCategoryId(parentId);
    }

    @Test
    void existsById_ReturnsTrue() {
        // Given
        when(jdbcRepository.existsById(1L)).thenReturn(true);

        // When
        boolean result = adapter.existsById(1L);

        // Then
        assertTrue(result);
        verify(jdbcRepository).existsById(1L);
    }

    @Test
    void existsById_ReturnsFalse() {
        // Given
        when(jdbcRepository.existsById(999L)).thenReturn(false);

        // When
        boolean result = adapter.existsById(999L);

        // Then
        assertFalse(result);
        verify(jdbcRepository).existsById(999L);
    }

    @Test
    void deleteById_Success() {
        // Given
        doNothing().when(jdbcRepository).deleteById(1L);

        // When
        adapter.deleteById(1L);

        // Then
        verify(jdbcRepository).deleteById(1L);
    }
}
