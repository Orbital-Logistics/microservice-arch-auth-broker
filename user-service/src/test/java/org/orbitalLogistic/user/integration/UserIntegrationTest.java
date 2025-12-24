package org.orbitalLogistic.user.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.orbitalLogistic.user.dto.request.UpdateUserRequestDTO;
import org.orbitalLogistic.user.dto.request.SignUpRequestDTO;
import org.orbitalLogistic.user.entities.User;
import org.orbitalLogistic.user.entities.Role;
import org.orbitalLogistic.user.TestcontainersConfiguration;
import org.orbitalLogistic.user.repositories.RoleRepository;
import org.orbitalLogistic.user.repositories.UserRepository;
import org.orbitalLogistic.user.services.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
@Tag("integration-tests")
@TestPropertySource(properties = {
        "spring.cloud.config.enabled=false"
})
@WithMockUser(roles = "ADMIN")
class UserIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        if (roleRepository.findByName("logistics_officer").isEmpty()) {
            Role role = new Role();
            role.setName("logistics_officer");
            roleRepository.save(role);
        }
    }

    @Test
    @DisplayName("Интеграционный тест: полный жизненный цикл пользователя")
    void fullUserLifecycle() throws Exception {
        SignUpRequestDTO registrationRequest = new SignUpRequestDTO(
                "integration@test.com",
                "integrationuser",
                "password123",
                Set.of("logistics_officer")
        );

        mockMvc.perform(post("/api/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(registrationRequest)))
                .andExpect(status().isCreated());

        User savedUser = userRepository.findByEmail("integration@test.com").orElseThrow();
        Long id = savedUser.getId();

        mockMvc.perform(get("/api/users/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.email").value("integration@test.com"))
                .andExpect(jsonPath("$.username").value("integrationuser"));

        mockMvc.perform(get("/api/users/email/integration@test.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.email").value("integration@test.com"));

        mockMvc.perform(get("/api/users/" + id + "/username"))
                .andExpect(status().isOk())
                .andExpect(content().string("integrationuser"));

        mockMvc.perform(get("/api/users/" + id + "/exists"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        UpdateUserRequestDTO updateRequest = new UpdateUserRequestDTO();
        updateRequest.setUsername("integrationuser");
        updateRequest.setNewUsername("updateduser");

        mockMvc.perform(put("/api/users/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                // application returns the updated username
                .andExpect(jsonPath("$.username").value("updateduser"));

        mockMvc.perform(delete("/api/users/" + id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/users/" + id + "/exists"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    @DisplayName("Регистрация с дублирующимся email возвращает ошибку")
    void registerUser_DuplicateEmail_ReturnsError() throws Exception {
        Role role = roleRepository.findByName("logistics_officer").orElseThrow();
        User existingUser = User.builder()
                .email("duplicate@test.com")
                .username("existinguser")
                .password("password123")
                .enabled(true)
                .build();
        existingUser.getRoles().add(role);
        userRepository.save(existingUser);

        SignUpRequestDTO duplicateRequest = new SignUpRequestDTO(
                "duplicate@test.com",
                "newuser",
                "password123",
                Set.of("logistics_officer")
        );

        mockMvc.perform(post("/api/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(duplicateRequest)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("Регистрация с дублирующимся username возвращает ошибку")
    void registerUser_DuplicateUsername_ReturnsError() throws Exception {
        Role role = roleRepository.findByName("logistics_officer").orElseThrow();
        User existingUser = User.builder()
                .email("user1@test.com")
                .username("duplicateusername")
                .password("password123")
                .enabled(true)
                .build();
        existingUser.getRoles().add(role);
        userRepository.save(existingUser);

        SignUpRequestDTO duplicateRequest = new SignUpRequestDTO(
                "user2@test.com",
                "duplicateusername",
                "password123",
                Set.of("logistics_officer")
        );

        mockMvc.perform(post("/api/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(duplicateRequest)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("Получение несуществующего пользователя возвращает ошибку")
    void getUserById_NotFound_ReturnsError() throws Exception {
        mockMvc.perform(get("/api/users/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Обновление несуществующего пользователя возвращает ошибку")
    void updateUser_NotFound_ReturnsError() throws Exception {
        UpdateUserRequestDTO updateRequest = new UpdateUserRequestDTO();
        updateRequest.setUsername("newusername");

        mockMvc.perform(put("/api/users/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Удаление несуществующего пользователя возвращает ошибку")
    void deleteUser_NotFound_ReturnsError() throws Exception {
        mockMvc.perform(delete("/api/users/999999"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Валидация: невалидный email при регистрации")
    void registerUser_InvalidEmail_ReturnsValidationError() throws Exception {
        SignUpRequestDTO invalidRequest = new SignUpRequestDTO(
                "invalid-email",
                "username",
                "password123",
                Set.of("logistics_officer")
        );

        mockMvc.perform(post("/api/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Валидация: слишком короткий username при регистрации")
    void registerUser_UsernameTooShort_ReturnsValidationError() throws Exception {
        SignUpRequestDTO invalidRequest = new SignUpRequestDTO(
                "test@example.com",
                "a",
                "password123",
                Set.of("logistics_officer")
        );

        mockMvc.perform(post("/api/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Валидация: слишком короткий пароль при регистрации")
    void registerUser_PasswordTooShort_ReturnsValidationError() throws Exception {
        SignUpRequestDTO invalidRequest = new SignUpRequestDTO(
                "test@example.com",
                "username",
                "short",
                Set.of("logistics_officer")
        );

        mockMvc.perform(post("/api/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Множественная регистрация пользователей")
    void registerMultipleUsers() throws Exception {
        SignUpRequestDTO request1 = new SignUpRequestDTO(
                "user1@test.com",
                "user1",
                "password123",
                Set.of("logistics_officer")
        );

        mockMvc.perform(post("/api/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(request1)))
                .andExpect(status().isCreated());

        SignUpRequestDTO request2 = new SignUpRequestDTO(
                "user2@test.com",
                "user2",
                "password123",
                Set.of("logistics_officer")
        );

        mockMvc.perform(post("/api/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(request2)))
                .andExpect(status().isCreated());

        assertThat(userRepository.count()).isEqualTo(2);
    }
}
