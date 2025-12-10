package org.orbitalLogistic.user.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.orbitalLogistic.user.controllers.UserController;
import org.orbitalLogistic.user.dto.request.UpdateUserRequestDTO;
import org.orbitalLogistic.user.dto.request.UserRegistrationRequestDTO;
import org.orbitalLogistic.user.dto.response.UserResponseDTO;
import org.orbitalLogistic.user.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebFluxTest(UserController.class)
class UserControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private UserService userService;

    private UserResponseDTO userResponseDTO;
    private UserRegistrationRequestDTO registrationRequest;
    private UpdateUserRequestDTO updateRequest;

    @BeforeEach
    void setUp() {
        userResponseDTO = new UserResponseDTO(
                1L,
                "test@example.com",
                "testuser"
        );

        registrationRequest = new UserRegistrationRequestDTO(
                "newuser@example.com",
                "newuser",
                "password123"
        );

        updateRequest = new UpdateUserRequestDTO("updatedusername");
    }

    @Test
    @DisplayName("POST /api/users/register - успешная регистрация")
    void registerUser_Success() {
        when(userService.registerUser(any(UserRegistrationRequestDTO.class)))
                .thenReturn(Mono.just(userResponseDTO));

        webTestClient.post()
                .uri("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(registrationRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isEqualTo(1)
                .jsonPath("$.email").isEqualTo("test@example.com")
                .jsonPath("$.username").isEqualTo("testuser");

        verify(userService).registerUser(any(UserRegistrationRequestDTO.class));
    }

    @Test
    @DisplayName("POST /api/users/register - невалидные данные (пустой email)")
    void registerUser_InvalidEmail() {
        UserRegistrationRequestDTO invalidRequest = new UserRegistrationRequestDTO(
                "",
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
    @DisplayName("POST /api/users/register - невалидный пароль (слишком короткий)")
    void registerUser_InvalidPassword() {
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
    @DisplayName("GET /api/users/email/{email} - успешное получение пользователя")
    void getUserByEmail_Success() {
        when(userService.findUserByEmail(anyString()))
                .thenReturn(Mono.just(userResponseDTO));

        webTestClient.get()
                .uri("/api/users/email/test@example.com")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(1)
                .jsonPath("$.email").isEqualTo("test@example.com")
                .jsonPath("$.username").isEqualTo("testuser");

        verify(userService).findUserByEmail("test@example.com");
    }

    @Test
    @DisplayName("GET /api/users/{id} - успешное получение пользователя по ID")
    void getUserById_Success() {
        when(userService.findUserById(anyLong()))
                .thenReturn(Mono.just(userResponseDTO));

        webTestClient.get()
                .uri("/api/users/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(1)
                .jsonPath("$.email").isEqualTo("test@example.com")
                .jsonPath("$.username").isEqualTo("testuser");

        verify(userService).findUserById(1L);
    }

    @Test
    @DisplayName("GET /api/users/{id}/username - успешное получение имени пользователя")
    void getUsernameById_Success() {
        when(userService.findUserById(anyLong()))
                .thenReturn(Mono.just(userResponseDTO));

        webTestClient.get()
                .uri("/api/users/1/username")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .isEqualTo("testuser");

        verify(userService).findUserById(1L);
    }

    @Test
    @DisplayName("PUT /api/users/{id} - успешное обновление пользователя")
    void updateUser_Success() {
        UserResponseDTO updatedResponse = new UserResponseDTO(
                1L,
                "test@example.com",
                "updatedusername"
        );

        when(userService.updateUser(anyLong(), any(UpdateUserRequestDTO.class)))
                .thenReturn(Mono.just(updatedResponse));

        webTestClient.put()
                .uri("/api/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(1)
                .jsonPath("$.username").isEqualTo("updatedusername");

        verify(userService).updateUser(eq(1L), any(UpdateUserRequestDTO.class));
    }

    @Test
    @DisplayName("PUT /api/users/{id} - невалидное имя пользователя")
    void updateUser_InvalidUsername() {
        UpdateUserRequestDTO invalidRequest = new UpdateUserRequestDTO("a");

        webTestClient.put()
                .uri("/api/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidRequest)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("DELETE /api/users/{id} - успешное удаление")
    void deleteUser_Success() {
        when(userService.deleteUser(anyLong()))
                .thenReturn(Mono.empty());

        webTestClient.delete()
                .uri("/api/users/1")
                .exchange()
                .expectStatus().isNoContent();

        verify(userService).deleteUser(1L);
    }

    @Test
    @DisplayName("GET /api/users/{id}/exists - пользователь существует")
    void userExists_True() {
        when(userService.userExists(anyLong()))
                .thenReturn(Mono.just(true));

        webTestClient.get()
                .uri("/api/users/1/exists")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Boolean.class)
                .isEqualTo(true);

        verify(userService).userExists(1L);
    }

    @Test
    @DisplayName("GET /api/users/{id}/exists - пользователь не существует")
    void userExists_False() {
        when(userService.userExists(anyLong()))
                .thenReturn(Mono.just(false));

        webTestClient.get()
                .uri("/api/users/999/exists")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Boolean.class)
                .isEqualTo(false);

        verify(userService).userExists(999L);
    }
}

