package org.orbitalLogistic.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.orbitalLogistic.user.controllers.UserController;
import org.orbitalLogistic.user.dto.request.UpdateUserRequestDTO;
import org.orbitalLogistic.user.entities.Role;
import org.orbitalLogistic.user.entities.User;
import org.orbitalLogistic.user.services.UserService;
import org.orbitalLogistic.user.services.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;

    @BeforeEach
    void setUp() {
        Role role = new Role();
        role.setId(1L);
        role.setName("logistics_officer");

        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .username("testuser")
                .password("secret")
                .build();
        testUser.getRoles().add(role);
    }

    @Test
    @DisplayName("GET /api/users/email/{email} - успешное получение пользователя")
    void getUserByEmail_Success() throws Exception {
        when(userService.findUserByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        mockMvc.perform(get("/api/users/email/test@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.username").value("testuser"));

        verify(userService).findUserByEmail("test@example.com");
    }

    @Test
    @DisplayName("GET /api/users/{id} - успешное получение пользователя по ID")
    void getUserById_Success() throws Exception {
        when(userService.findUserById(1L)).thenReturn(Optional.of(testUser));

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.username").value("testuser"));

        verify(userService).findUserById(1L);
    }

    @Test
    @DisplayName("GET /api/users/{id} - пользователь не найден")
    void getUserById_NotFound() throws Exception {
        when(userService.findUserById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/users/{id}/username - успешное получение имени пользователя")
    void getUsernameById_Success() throws Exception {
        when(userService.findUserById(1L)).thenReturn(Optional.of(testUser));

        mockMvc.perform(get("/api/users/1/username"))
                .andExpect(status().isOk())
                .andExpect(content().string("testuser"));

        verify(userService).findUserById(1L);
    }

    @Test
    @DisplayName("PUT /api/users/update - успешное обновление пользователя")
    void updateUser_Success() throws Exception {
        UpdateUserRequestDTO request = new UpdateUserRequestDTO();
        request.setUsername("testuser");
        request.setNewUsername("updatedusername");

        when(userService.updateUser(eq("testuser"), eq("updatedusername"), isNull())).thenReturn(testUser);

        mockMvc.perform(put("/api/users/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testuser"));

        verify(userService).updateUser("testuser", "updatedusername", null);
    }

    @Test
    @DisplayName("DELETE /api/users/{id} - успешное удаление")
    void deleteUser_Success() throws Exception {
        // deleteUser returns void
        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(1L);
    }

    @Test
    @DisplayName("GET /api/users/{id}/exists - пользователь существует")
    void userExists_True() throws Exception {
        when(userService.userExists(1L)).thenReturn(true);

        mockMvc.perform(get("/api/users/1/exists"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(userService).userExists(1L);
    }

    @Test
    @DisplayName("GET /api/users/{id}/roles - успешное получение ролей пользователя")
    void getUserRoles_Success() throws Exception {
        User userWithRoles = new User();
        userWithRoles.setId(20L);
        userWithRoles.setUsername("dan");
        Role role1 = new Role();
        role1.setName("ADMIN");
        userWithRoles.getRoles().add(role1);
        when(userService.findUserById(20L)).thenReturn(Optional.of(userWithRoles));

        mockMvc.perform(get("/api/users/20/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles[0]").value("ADMIN"));

        verify(userService).findUserById(20L);
    }
}
