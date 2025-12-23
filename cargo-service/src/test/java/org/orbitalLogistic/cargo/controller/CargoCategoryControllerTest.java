package org.orbitalLogistic.cargo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.orbitalLogistic.cargo.controllers.CargoCategoryController;
import org.orbitalLogistic.cargo.dto.request.CargoCategoryRequestDTO;
import org.orbitalLogistic.cargo.dto.response.CargoCategoryResponseDTO;
import org.orbitalLogistic.cargo.exceptions.CargoCategoryNotFoundException;
import org.orbitalLogistic.cargo.services.CargoCategoryService;
import org.orbitalLogistic.cargo.services.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CargoCategoryController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = {
        "spring.cloud.config.enabled=false"
})
class CargoCategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CargoCategoryService cargoCategoryService;

    @MockitoBean
    private JwtService jwtService;

    private CargoCategoryResponseDTO responseDTO;
    private CargoCategoryRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        responseDTO = new CargoCategoryResponseDTO(
                1L,
                "Electronics",
                null,
                null,
                "Electronic components",
                new ArrayList<>(),
                0
        );

        requestDTO = new CargoCategoryRequestDTO(
                "Electronics",
                null,
                "Electronic components"
        );
    }

    @Test
    void getAllCategories_Success() throws Exception {
        when(cargoCategoryService.getAllCategories()).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/cargo-categories")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Electronics"));

        verify(cargoCategoryService).getAllCategories();
    }

    @Test
    void getCategoryById_Success() throws Exception {
        when(cargoCategoryService.getCategoryById(1L)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/cargo-categories/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Electronics"));

        verify(cargoCategoryService).getCategoryById(1L);
    }

    @Test
    void getCategoryById_NotFound() throws Exception {
        when(cargoCategoryService.getCategoryById(999L))
                .thenThrow(new CargoCategoryNotFoundException("Category not found"));

        mockMvc.perform(get("/api/cargo-categories/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(cargoCategoryService).getCategoryById(999L);
    }

    @Test
    void createCategory_Success() throws Exception {
        when(cargoCategoryService.createCategory(any(CargoCategoryRequestDTO.class)))
                .thenReturn(responseDTO);

        mockMvc.perform(post("/api/cargo-categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Electronics"));

        verify(cargoCategoryService).createCategory(any(CargoCategoryRequestDTO.class));
    }

    @Test
    void createCategory_InvalidData() throws Exception {
        CargoCategoryRequestDTO invalidRequest = new CargoCategoryRequestDTO(
                "",
                null,
                "Description"
        );

        mockMvc.perform(post("/api/cargo-categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(cargoCategoryService, never()).createCategory(any(CargoCategoryRequestDTO.class));
    }

    @Test
    void getCategoryTree_Success() throws Exception {
        when(cargoCategoryService.getCategoryTree()).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/cargo-categories/tree")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Electronics"));

        verify(cargoCategoryService).getCategoryTree();
    }
}

