package org.orbitalLogistic.user.infrastructure.adapters.in.rest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.orbitalLogistic.user.application.ports.in.*;
import org.orbitalLogistic.user.domain.model.Role;
import org.orbitalLogistic.user.domain.model.User;
import org.orbitalLogistic.user.infrastructure.adapters.in.rest.dto.AuthResponse;
import org.orbitalLogistic.user.infrastructure.adapters.in.rest.dto.ChangePasswordRequest;
import org.orbitalLogistic.user.infrastructure.adapters.in.rest.dto.LoginRequest;
import org.orbitalLogistic.user.infrastructure.adapters.in.rest.dto.ManageRolesRequest;
import org.orbitalLogistic.user.infrastructure.adapters.in.rest.dto.RegisterRequest;
import org.orbitalLogistic.user.infrastructure.adapters.in.rest.dto.UserResponse;
import org.orbitalLogistic.user.infrastructure.adapters.in.rest.mapper.AuthRestMapper;
import org.orbitalLogistic.user.infrastructure.adapters.in.rest.mapper.UserRestMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthRestController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("AuthRestController Tests")
class AuthRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthUseCase authUseCase;

    @MockitoBean
    private UpdateUserUseCase updateUserUseCase;

    @MockitoBean
    private GetUsersUseCase getUsersUseCase;

    @MockitoBean
    private AuthRestMapper authRestMapper;

    @MockitoBean
    private UserRestMapper userRestMapper;

    @MockitoBean
    private org.orbitalLogistic.user.infrastructure.adapters.out.security.JwtTokenAdapter jwtTokenAdapter;

    @MockitoBean
    private org.orbitalLogistic.user.infrastructure.adapters.in.rest.security.JwtAuthFilter jwtAuthFilter;

    private User testUser;
    private Role testRole;

    @BeforeEach
    void setUp() {
        testRole = Role.builder()
                .id(1L)
                .name("ADMIN")
                .build();

        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .password("encoded_password")
                .email("test@example.com")
                .enabled(true)
                .roles(Set.of(testRole))
                .build();

        // Setup mapper mocks
        when(authRestMapper.toCommand(any(LoginRequest.class)))
                .thenAnswer(inv -> {
                    LoginRequest req = inv.getArgument(0);
                    return new LoginCommand(req.username(), req.password());
                });
        
        when(authRestMapper.toCommand(any(RegisterRequest.class)))
                .thenAnswer(inv -> {
                    RegisterRequest req = inv.getArgument(0);
                    return new RegisterCommand(req.username(), req.password(), req.email(), req.roleIds());
                });
        
        when(authRestMapper.toResponse(anyString(), any(User.class)))
                .thenAnswer(inv -> {
                    String token = inv.getArgument(0);
                    User user = inv.getArgument(1);
                    return new AuthResponse(token, user.getUsername(), Set.of("ADMIN"));
                });
        
        when(userRestMapper.toResponse(any(User.class)))
                .thenAnswer(inv -> {
                    User user = inv.getArgument(0);
                    return new UserResponse(user.getId(), user.getUsername(), user.getEmail(), user.getEnabled(), Set.of());
                });
    }

    @Test
    @DisplayName("Should login successfully")
    void shouldLoginSuccessfully() throws Exception {
        // Given
        LoginRequest request = new LoginRequest("testuser", "password");
        when(authUseCase.login(any(LoginCommand.class))).thenReturn("jwt_token");
        when(getUsersUseCase.getByUsername("testuser")).thenReturn(Optional.of(testUser));

        // When & Then
        mockMvc.perform(post("/api/auth/log-in")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt_token"));

        verify(authUseCase).login(any(LoginCommand.class));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("Should register user successfully")
    void shouldRegisterSuccessfully() throws Exception {
        // Given
        RegisterRequest request = new RegisterRequest("newuser", "password", "new@example.com", Set.of(1L));
        when(authUseCase.register(any(RegisterCommand.class))).thenReturn("jwt_token");
        when(getUsersUseCase.getByUsername("newuser")).thenReturn(Optional.of(testUser));

        // When & Then
        mockMvc.perform(post("/api/auth/sign-up")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("jwt_token"))
                .andExpect(jsonPath("$.username").value("testuser"));

        verify(authUseCase).register(any(RegisterCommand.class));
        verify(getUsersUseCase).getByUsername("newuser");
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    @DisplayName("Should change own password")
    void shouldChangeOwnPassword() throws Exception {
        // Given
        ChangePasswordRequest request = new ChangePasswordRequest(1L, "newpassword");
        when(getUsersUseCase.getByUsername("testuser")).thenReturn(Optional.of(testUser));
        doNothing().when(updateUserUseCase).changePassword(1L, "newpassword");

        // When & Then
        mockMvc.perform(patch("/api/auth/change-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(updateUserUseCase).changePassword(1L, "newpassword");
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("Should change any user password as admin")
    void shouldChangeAnyUserPasswordAsAdmin() throws Exception {
        // Given
        ChangePasswordRequest request = new ChangePasswordRequest(1L, "newpassword");
        User adminUser = testUser.toBuilder().username("admin").build();
        when(getUsersUseCase.getByUsername("admin")).thenReturn(Optional.of(adminUser));
        doNothing().when(updateUserUseCase).changePassword(1L, "newpassword");

        // When & Then
        mockMvc.perform(patch("/api/auth/change-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(updateUserUseCase).changePassword(1L, "newpassword");
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("Should grant roles")
    void shouldGrantRoles() throws Exception {
        // Given
        ManageRolesRequest request = new ManageRolesRequest(1L, Set.of(2L));
        when(updateUserUseCase.grantRoles(1L, Set.of(2L))).thenReturn(testUser);

        // When & Then
        mockMvc.perform(post("/api/auth/grant-roles")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"));

        verify(updateUserUseCase).grantRoles(1L, Set.of(2L));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("Should revoke roles")
    void shouldRevokeRoles() throws Exception {
        // Given
        ManageRolesRequest request = new ManageRolesRequest(1L, Set.of(2L));
        when(updateUserUseCase.revokeRoles(1L, Set.of(2L))).thenReturn(testUser);

        // When & Then
        mockMvc.perform(post("/api/auth/revoke-roles")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"));

        verify(updateUserUseCase).revokeRoles(1L, Set.of(2L));
    }
}
