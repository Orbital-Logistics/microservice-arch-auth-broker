package org.orbitalLogistic.user.controller;

import org.junit.jupiter.api.Test;
import org.orbitalLogistic.user.controllers.RoleController;
import org.orbitalLogistic.user.services.JwtService;
import org.orbitalLogistic.user.services.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = RoleController.class)
@AutoConfigureMockMvc(addFilters = false)
public class RoleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RoleService roleService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void getAllRoles_returnsList() throws Exception {
        when(roleService.getAllRolesStrings()).thenReturn(Set.of("ADMIN", "USER"));

        mockMvc.perform(get("/api/roles/get"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles").isArray())
                .andExpect(jsonPath("$.roles", containsInAnyOrder("ADMIN", "USER")));
    }
}
