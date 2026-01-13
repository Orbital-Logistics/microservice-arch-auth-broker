package org.orbitalLogistic.user.infrastructure.adapters.in.rest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.orbitalLogistic.user.application.ports.in.*;
import org.orbitalLogistic.user.domain.exception.UserNotFoundException;
import org.orbitalLogistic.user.domain.model.Role;
import org.orbitalLogistic.user.domain.model.User;
import org.orbitalLogistic.user.infrastructure.adapters.in.rest.dto.UpdateUserRequest;
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
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserRestController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("UserRestController Tests")
class UserRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private GetUsersUseCase getUsersUseCase;

    @MockitoBean
    private UpdateUserUseCase updateUserUseCase;

    @MockitoBean
    private DeleteUserUseCase deleteUserUseCase;

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

        // Setup mapper mock
        when(userRestMapper.toResponse(any(User.class)))
                .thenAnswer(inv -> {
                    User user = inv.getArgument(0);
                    return new org.orbitalLogistic.user.infrastructure.adapters.in.rest.dto.UserResponse(
                            user.getId(), 
                            user.getUsername(), 
                            user.getEmail(), 
                            user.getEnabled(), 
                            Set.of()
                    );
                });
        
        when(userRestMapper.toCommand(any(UpdateUserRequest.class)))
                .thenAnswer(inv -> {
                    UpdateUserRequest req = inv.getArgument(0);
                    return new UpdateUserCommand(req.id(), req.username(), req.email());
                });
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("Should get user by id")
    void shouldGetUserById() throws Exception {
        // Given
        when(getUsersUseCase.getById(1L)).thenReturn(Optional.of(testUser));

        // When & Then
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));

        verify(getUsersUseCase).getById(1L);
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("Should return 404 when user not found")
    void shouldReturn404WhenUserNotFound() throws Exception {
        // Given
        when(getUsersUseCase.getById(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/users/999"))
                .andExpect(status().isNotFound());

        verify(getUsersUseCase).getById(999L);
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("Should get username by id")
    void shouldGetUsernameById() throws Exception {
        // Given
        when(getUsersUseCase.getById(1L)).thenReturn(Optional.of(testUser));

        // When & Then
        mockMvc.perform(get("/api/users/1/username"))
                .andExpect(status().isOk())
                .andExpect(content().string("testuser"));

        verify(getUsersUseCase).getById(1L);
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("Should get user by email")
    void shouldGetUserByEmail() throws Exception {
        // Given
        when(getUsersUseCase.getByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // When & Then
        mockMvc.perform(get("/api/users/email/test@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"));

        verify(getUsersUseCase).getByEmail("test@example.com");
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("Should check if user exists")
    void shouldCheckIfUserExists() throws Exception {
        // Given
        when(getUsersUseCase.getById(1L)).thenReturn(Optional.of(testUser));

        // When & Then
        mockMvc.perform(get("/api/users/1/exists"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(getUsersUseCase).getById(1L);
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("Should get user roles")
    void shouldGetUserRoles() throws Exception {
        // Given
        when(getUsersUseCase.getById(1L)).thenReturn(Optional.of(testUser));

        // When & Then
        mockMvc.perform(get("/api/users/1/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("ADMIN"));

        verify(getUsersUseCase).getById(1L);
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    @DisplayName("Should update user (self)")
    void shouldUpdateUserSelf() throws Exception {
        // Given
        UpdateUserRequest request = new UpdateUserRequest(1L, "newusername", "new@example.com");
        User updatedUser = testUser.toBuilder().username("newusername").build();
        
        when(getUsersUseCase.getByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(updateUserUseCase.updateUser(any(UpdateUserCommand.class))).thenReturn(updatedUser);

        // When & Then
        mockMvc.perform(put("/api/users/update")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("newusername"));

        verify(updateUserUseCase).updateUser(any(UpdateUserCommand.class));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("Should update user (admin)")
    void shouldUpdateUserAsAdmin() throws Exception {
        // Given
        UpdateUserRequest request = new UpdateUserRequest(1L, "newusername", "new@example.com");
        User updatedUser = testUser.toBuilder().username("newusername").build();
        User adminUser = testUser.toBuilder().username("admin").build();
        
        when(getUsersUseCase.getByUsername("admin")).thenReturn(Optional.of(adminUser));
        when(updateUserUseCase.updateUser(any(UpdateUserCommand.class))).thenReturn(updatedUser);

        // When & Then
        mockMvc.perform(put("/api/users/update")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(updateUserUseCase).updateUser(any(UpdateUserCommand.class));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("Should delete user")
    void shouldDeleteUser() throws Exception {
        // Given
        doNothing().when(deleteUserUseCase).deleteUser(1L);

        // When & Then
        mockMvc.perform(delete("/api/users/1").with(csrf()))
                .andExpect(status().isNoContent());

        verify(deleteUserUseCase).deleteUser(1L);
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("Should return 404 when deleting non-existent user")
    void shouldReturn404WhenDeletingNonExistentUser() throws Exception {
        // Given
        doThrow(new UserNotFoundException("User not found"))
                .when(deleteUserUseCase).deleteUser(999L);

        // When & Then
        mockMvc.perform(delete("/api/users/999").with(csrf()))
                .andExpect(status().isNotFound());

        verify(deleteUserUseCase).deleteUser(999L);
    }
}
