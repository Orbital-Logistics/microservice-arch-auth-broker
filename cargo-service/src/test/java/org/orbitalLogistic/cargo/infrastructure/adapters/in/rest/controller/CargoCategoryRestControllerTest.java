package org.orbitalLogistic.cargo.infrastructure.adapters.in.rest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.orbitalLogistic.cargo.application.ports.in.*;
import org.orbitalLogistic.cargo.domain.model.CargoCategory;
import org.orbitalLogistic.cargo.filter.JwtService;
import org.orbitalLogistic.cargo.infrastructure.adapters.in.rest.dto.request.CreateCargoCategoryRequest;
import org.orbitalLogistic.cargo.infrastructure.adapters.in.rest.dto.response.CargoCategoryResponse;
import org.orbitalLogistic.cargo.infrastructure.adapters.in.rest.mapper.CargoCategoryRestMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CargoCategoryRestController.class)
@TestPropertySource(properties = {"spring.cloud.config.enabled=false"})
class CargoCategoryRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreateCargoCategoryUseCase createCargoCategoryUseCase;

    @MockitoBean
    private GetCargoCategoryUseCase getCargoCategoryUseCase;

    @MockitoBean
    private CargoCategoryRestMapper categoryMapper;

    @MockitoBean
    private JwtService jwtService;

    private CargoCategory category;
    private CargoCategoryResponse categoryResponse;
    private CreateCargoCategoryRequest createRequest;

    @BeforeEach
    void setUp() {
        category = CargoCategory.builder()
                .id(1L)
                .name("Electronics")
                .description("Electronic devices")
                .parentCategoryId(null)
                .build();

        categoryResponse = CargoCategoryResponse.builder()
                .id(1L)
                .name("Electronics")
                .description("Electronic devices")
                .parentCategoryId(null)
                .build();

        createRequest = CreateCargoCategoryRequest.builder()
                .name("Electronics")
                .description("Electronic devices")
                .build();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getCategoryById_Success() throws Exception {
        // Given
        when(getCargoCategoryUseCase.getCategoryById(1L)).thenReturn(Optional.of(category));
        when(categoryMapper.toResponse(category)).thenReturn(categoryResponse);

        // When & Then
        mockMvc.perform(get("/api/cargo-categories/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Electronics"));

        verify(getCargoCategoryUseCase).getCategoryById(1L);
        verify(categoryMapper).toResponse(category);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getCategoryById_NotFound() throws Exception {
        // Given
        when(getCargoCategoryUseCase.getCategoryById(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/cargo-categories/{id}", 999L))
                .andExpect(status().isNotFound());

        verify(getCargoCategoryUseCase).getCategoryById(999L);
        verify(categoryMapper, never()).toResponse(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllCategories_Success() throws Exception {
        // Given
        when(getCargoCategoryUseCase.getAllCategories()).thenReturn(Arrays.asList(category));
        when(categoryMapper.toResponse(category)).thenReturn(categoryResponse);

        // When & Then
        mockMvc.perform(get("/api/cargo-categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Electronics"));

        verify(getCargoCategoryUseCase).getAllCategories();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getCategoryTree_Success() throws Exception {
        // Given
        when(getCargoCategoryUseCase.getCategoryTree()).thenReturn(Arrays.asList(category));
        when(categoryMapper.toResponse(category)).thenReturn(categoryResponse);

        // When & Then
        mockMvc.perform(get("/api/cargo-categories/tree"))
                .andExpect(status().isOk());

        verify(getCargoCategoryUseCase).getCategoryTree();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCategory_Success() throws Exception {
        // Given
        when(categoryMapper.toDomain(any(CreateCargoCategoryRequest.class))).thenReturn(category);
        when(createCargoCategoryUseCase.createCategory(any(CargoCategory.class))).thenReturn(category);
        when(categoryMapper.toResponse(category)).thenReturn(categoryResponse);

        // When & Then
        mockMvc.perform(post("/api/cargo-categories")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Electronics"));

        verify(createCargoCategoryUseCase).createCategory(any(CargoCategory.class));
    }
}
