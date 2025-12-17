package org.orbitalLogistic.user.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.orbitalLogistic.user.dto.request.UpdateUserRequestDTO;
import org.orbitalLogistic.user.dto.request.UserRegistrationRequestDTO;
import org.orbitalLogistic.user.entities.User;
import org.orbitalLogistic.user.entities.UserRole;
import org.orbitalLogistic.user.repositories.UserRepository;
import org.orbitalLogistic.user.repositories.UserRoleRepository;
import org.orbitalLogistic.user.TestcontainersConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
@Tag("integration-tests")
@TestPropertySource(properties = {
        "spring.cloud.config.enabled=false"
})
class UserIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        if (userRoleRepository.findByName("logistics_officer").isEmpty()) {
            UserRole role = new UserRole();
            role.setName("logistics_officer");
            role.setDescription("Logistics Officer");
            userRoleRepository.save(role);
        }
    }

    @Test
    @DisplayName("Интеграционный тест: полный жизненный цикл пользователя")
    void fullUserLifecycle() {
        UserRegistrationRequestDTO registrationRequest = new UserRegistrationRequestDTO(
                "integration@test.com",
                "integrationuser",
                "password123"
        );

        webTestClient.post()
                .uri("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(registrationRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.email").isEqualTo("integration@test.com")
                .jsonPath("$.username").isEqualTo("integrationuser")
                .jsonPath("$.id").value(id -> assertThat(id).isNotNull());

        User savedUser = userRepository.findByEmail("integration@test.com").orElseThrow();
        Long id = savedUser.getId();

        webTestClient.get()
                .uri("/api/users/" + id)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(id)
                .jsonPath("$.email").isEqualTo("integration@test.com")
                .jsonPath("$.username").isEqualTo("integrationuser");

        webTestClient.get()
                .uri("/api/users/email/integration@test.com")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(id)
                .jsonPath("$.email").isEqualTo("integration@test.com");

        webTestClient.get()
                .uri("/api/users/" + id + "/username")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .isEqualTo("integrationuser");

        webTestClient.get()
                .uri("/api/users/" + id + "/exists")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Boolean.class)
                .isEqualTo(true);

        UpdateUserRequestDTO updateRequest = new UpdateUserRequestDTO("updateduser");

        webTestClient.put()
                .uri("/api/users/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(id)
                .jsonPath("$.username").isEqualTo("updateduser")
                .jsonPath("$.email").isEqualTo("integration@test.com");

        webTestClient.delete()
                .uri("/api/users/" + id)
                .exchange()
                .expectStatus().isNoContent();

        webTestClient.get()
                .uri("/api/users/" + id + "/exists")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Boolean.class)
                .isEqualTo(false);
    }

    @Test
    @DisplayName("Регистрация с дублирующимся email возвращает ошибку")
    void registerUser_DuplicateEmail_ReturnsError() {
        UserRole role = userRoleRepository.findByName("logistics_officer").orElseThrow();
        User existingUser = User.builder()
                .email("duplicate@test.com")
                .username("existinguser")
                .passwordHash("password123")
                .roleId(role.getId())
                .build();
        userRepository.save(existingUser);

        UserRegistrationRequestDTO duplicateRequest = new UserRegistrationRequestDTO(
                "duplicate@test.com",
                "newuser",
                "password123"
        );

        webTestClient.post()
                .uri("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(duplicateRequest)
                .exchange()
                .expectStatus().is4xxClientError();
    }

    @Test
    @DisplayName("Регистрация с дублирующимся username возвращает ошибку")
    void registerUser_DuplicateUsername_ReturnsError() {
        UserRole role = userRoleRepository.findByName("logistics_officer").orElseThrow();
        User existingUser = User.builder()
                .email("user1@test.com")
                .username("duplicateusername")
                .passwordHash("password123")
                .roleId(role.getId())
                .build();
        userRepository.save(existingUser);

        UserRegistrationRequestDTO duplicateRequest = new UserRegistrationRequestDTO(
                "user2@test.com",
                "duplicateusername",
                "password123"
        );

        webTestClient.post()
                .uri("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(duplicateRequest)
                .exchange()
                .expectStatus().is4xxClientError();
    }

    @Test
    @DisplayName("Получение несуществующего пользователя возвращает ошибку")
    void getUserById_NotFound_ReturnsError() {
        webTestClient.get()
                .uri("/api/users/999999")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("Обновление несуществующего пользователя возвращает ошибку")
    void updateUser_NotFound_ReturnsError() {
        UpdateUserRequestDTO updateRequest = new UpdateUserRequestDTO("newusername");

        webTestClient.put()
                .uri("/api/users/999999")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateRequest)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("Удаление несуществующего пользователя возвращает ошибку")
    void deleteUser_NotFound_ReturnsError() {
        webTestClient.delete()
                .uri("/api/users/999999")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("Валидация: невалидный email при регистрации")
    void registerUser_InvalidEmail_ReturnsValidationError() {
        UserRegistrationRequestDTO invalidRequest = new UserRegistrationRequestDTO(
                "invalid-email",
                "username",
                "password123"
        );

        webTestClient.post()
                .uri("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidRequest)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("Валидация: слишком короткий username при регистрации")
    void registerUser_UsernameTooShort_ReturnsValidationError() {
        UserRegistrationRequestDTO invalidRequest = new UserRegistrationRequestDTO(
                "test@example.com",
                "a",
                "password123"
        );

        webTestClient.post()
                .uri("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidRequest)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("Валидация: слишком короткий пароль при регистрации")
    void registerUser_PasswordTooShort_ReturnsValidationError() {
        UserRegistrationRequestDTO invalidRequest = new UserRegistrationRequestDTO(
                "test@example.com",
                "username",
                "short"
        );

        webTestClient.post()
                .uri("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidRequest)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("Множественная регистрация пользователей")
    void registerMultipleUsers() {
        UserRegistrationRequestDTO request1 = new UserRegistrationRequestDTO(
                "user1@test.com",
                "user1",
                "password123"
        );

        webTestClient.post()
                .uri("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request1)
                .exchange()
                .expectStatus().isCreated();

        UserRegistrationRequestDTO request2 = new UserRegistrationRequestDTO(
                "user2@test.com",
                "user2",
                "password123"
        );

        webTestClient.post()
                .uri("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request2)
                .exchange()
                .expectStatus().isCreated();

        assertThat(userRepository.count()).isEqualTo(2);
    }
}

