package org.orbitalLogistic.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.orbitalLogistic.user.controllers.AuthController;
import org.orbitalLogistic.user.dto.request.ChangePasswordRequestDTO;
import org.orbitalLogistic.user.dto.request.RolesProcessRequestDTO;
import org.orbitalLogistic.user.dto.request.SignInRequestDTO;
import org.orbitalLogistic.user.dto.request.SignUpRequestDTO;
import org.orbitalLogistic.user.services.AuthService;
import org.orbitalLogistic.user.services.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "ADMIN")
    void signUp_returnsCreated_andTokenBody() throws Exception {
        SignUpRequestDTO request = new SignUpRequestDTO("a@b.com", "user", "password123", Set.of("logistics_officer"));

        when(authService.signUp(anyString(), anyString(), anyString(), Mockito.anySet())).thenReturn("token123");

        mockMvc.perform(post("/api/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.access_token").value("token123"));
    }

    @Test
    void logIn_returnsToken() throws Exception {
        SignInRequestDTO request = new SignInRequestDTO("user", "password123");
        when(authService.logIn(anyString(), anyString())).thenReturn("tok-login");

        mockMvc.perform(post("/api/auth/log-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").value("tok-login"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void grantRoles_callsService_andReturnsOk() throws Exception {
        RolesProcessRequestDTO request = new RolesProcessRequestDTO("someuser", Set.of("ADMIN"));

        mockMvc.perform(post("/api/auth/grant-roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(authService).grantRoles("someuser", Set.of("ADMIN"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void revokeRoles_callsService_andReturnsOk_whenRolesProvided() throws Exception {
        RolesProcessRequestDTO request = new RolesProcessRequestDTO("someuser", Set.of("USER"));

        mockMvc.perform(post("/api/auth/revoke-roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(authService).revokeRoles("someuser", Set.of("USER"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void revokeRoles_returnsBadRequest_whenRolesMissing() throws Exception {
        String body = "{\"username\": \"someuser\"}";

        mockMvc.perform(post("/api/auth/revoke-roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void changePassword_returnsToken_andOk_whenUserMatchesAuthentication() throws Exception {
        ChangePasswordRequestDTO request = new ChangePasswordRequestDTO("user", "oldpass123", "newpass123");
        when(authService.changePassword(anyString(), anyString(), anyString())).thenReturn("new-token");

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch("/api/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").value("new-token"));
    }
}
