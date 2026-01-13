package org.orbitalLogistic.user.infrastructure.adapters.in.rest.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.orbitalLogistic.user.application.ports.in.GetRolesUseCase;
import org.orbitalLogistic.user.domain.model.Role;
import org.orbitalLogistic.user.infrastructure.adapters.in.rest.mapper.RoleRestMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RoleRestController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("RoleRestController Tests")
class RoleRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetRolesUseCase getRolesUseCase;

    @MockitoBean
    private RoleRestMapper roleRestMapper;

    @MockitoBean
    private org.orbitalLogistic.user.infrastructure.adapters.out.security.JwtTokenAdapter jwtTokenAdapter;

    @MockitoBean
    private org.orbitalLogistic.user.infrastructure.adapters.in.rest.security.JwtAuthFilter jwtAuthFilter;

    private List<Role> roles;

    @BeforeEach
    void setUp() {
        Role role1 = Role.builder().id(1L).name("ADMIN").build();
        Role role2 = Role.builder().id(2L).name("USER").build();
        roles = Arrays.asList(role1, role2);
        
        // Setup mapper mock
        when(roleRestMapper.toResponse(any(Role.class)))
                .thenAnswer(inv -> {
                    Role role = inv.getArgument(0);
                    return new org.orbitalLogistic.user.infrastructure.adapters.in.rest.dto.RoleResponse(
                            role.getId(), 
                            role.getName()
                    );
                });
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("Should get all roles")
    void shouldGetAllRoles() throws Exception {
        // Given
        when(getRolesUseCase.getAll()).thenReturn(roles);

        // When & Then
        mockMvc.perform(get("/api/roles/get"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("ADMIN"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("USER"));

        verify(getRolesUseCase).getAll();
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    @DisplayName("Should allow non-admin to get roles")
    void shouldAllowNonAdminToGetRoles() throws Exception {
        // Given
        when(getRolesUseCase.getAll()).thenReturn(roles);

        // When & Then
        mockMvc.perform(get("/api/roles/get"))
                .andExpect(status().isOk());

        verify(getRolesUseCase).getAll();
    }
}
