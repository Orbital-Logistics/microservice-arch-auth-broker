package org.orbitalLogistic.user.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.orbitalLogistic.user.dto.request.UpdateUserRequestDTO;
import org.orbitalLogistic.user.dto.request.UserRegistrationRequestDTO;
import org.orbitalLogistic.user.dto.response.UserResponseDTO;
import org.orbitalLogistic.user.entities.User;
import org.orbitalLogistic.user.entities.UserRole;
import org.orbitalLogistic.user.exceptions.user.UserAlreadyExistsException;
import org.orbitalLogistic.user.exceptions.user.UserNotFoundException;
import org.orbitalLogistic.user.mappers.UserMapper;
import org.orbitalLogistic.user.repositories.UserRepository;
import org.orbitalLogistic.user.repositories.UserRoleRepository;
import org.orbitalLogistic.user.services.UserService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserRoleRepository roleRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserRole testRole;
    private UserRegistrationRequestDTO registrationRequest;
    private UserResponseDTO userResponseDTO;
    private UpdateUserRequestDTO updateRequest;

    @BeforeEach
    void setUp() {
        testRole = new UserRole();
        testRole.setId(1L);
        testRole.setName("logistics_officer");

        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .username("testuser")
                .passwordHash("hashedpassword123")
                .roleId(1L)
                .build();

        registrationRequest = new UserRegistrationRequestDTO(
                "newuser@example.com",
                "newuser",
                "password123"
        );

        userResponseDTO = new UserResponseDTO(
                testUser.getId(),
                testUser.getEmail(),
                testUser.getUsername()
        );

        updateRequest = new UpdateUserRequestDTO("updatedusername");
    }

    @Test
    @DisplayName("Регистрация пользователя - успешная")
    void registerUser_Success() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(roleRepository.findByName("logistics_officer")).thenReturn(Optional.of(testRole));
        when(userMapper.toEntity(any(UserRegistrationRequestDTO.class))).thenReturn(testUser);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.toResponseDTO(any(User.class))).thenReturn(userResponseDTO);

        StepVerifier.create(userService.registerUser(registrationRequest))
                .assertNext(response -> {
                    assertNotNull(response);
                    assertEquals(userResponseDTO.email(), response.email());
                    assertEquals(userResponseDTO.username(), response.username());
                })
                .verifyComplete();

        verify(userRepository).existsByEmail(registrationRequest.email());
        verify(userRepository).existsByUsername(registrationRequest.username());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Регистрация пользователя - email уже существует")
    void registerUser_EmailAlreadyExists() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        StepVerifier.create(userService.registerUser(registrationRequest))
                .expectError(UserAlreadyExistsException.class)
                .verify();

        verify(userRepository).existsByEmail(registrationRequest.email());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Регистрация пользователя - username уже существует")
    void registerUser_UsernameAlreadyExists() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByUsername(anyString())).thenReturn(true);

        StepVerifier.create(userService.registerUser(registrationRequest))
                .expectError(UserAlreadyExistsException.class)
                .verify();

        verify(userRepository).existsByEmail(registrationRequest.email());
        verify(userRepository).existsByUsername(registrationRequest.username());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Поиск пользователя по ID - успешный")
    void findUserById_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userMapper.toResponseDTO(any(User.class))).thenReturn(userResponseDTO);

        StepVerifier.create(userService.findUserById(1L))
                .assertNext(response -> {
                    assertNotNull(response);
                    assertEquals(testUser.getId(), response.id());
                    assertEquals(testUser.getEmail(), response.email());
                })
                .verifyComplete();

        verify(userRepository).findById(1L);
    }

    @Test
    @DisplayName("Поиск пользователя по ID - не найден")
    void findUserById_NotFound() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        StepVerifier.create(userService.findUserById(999L))
                .expectError(UserNotFoundException.class)
                .verify();

        verify(userRepository).findById(999L);
    }

    @Test
    @DisplayName("Поиск пользователя по email - успешный")
    void findUserByEmail_Success() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(userMapper.toResponseDTO(any(User.class))).thenReturn(userResponseDTO);

        StepVerifier.create(userService.findUserByEmail("test@example.com"))
                .assertNext(response -> {
                    assertNotNull(response);
                    assertEquals(testUser.getEmail(), response.email());
                })
                .verifyComplete();

        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    @DisplayName("Поиск пользователя по email - не найден")
    void findUserByEmail_NotFound() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        StepVerifier.create(userService.findUserByEmail("nonexistent@example.com"))
                .expectError(UserNotFoundException.class)
                .verify();

        verify(userRepository).findByEmail("nonexistent@example.com");
    }

    @Test
    @DisplayName("Получение списка пользователей с фильтрами")
    void getUsers_WithFilters() {
        List<User> users = List.of(testUser);
        when(userRepository.findUsersWithFilters(anyString(), anyString(), anyInt(), anyInt()))
                .thenReturn(users);
        when(userRepository.countUsersWithFilters(anyString(), anyString())).thenReturn(1L);
        when(userMapper.toResponseDTO(any(User.class))).thenReturn(userResponseDTO);

        StepVerifier.create(userService.getUsers("test@example.com", "testuser", 0, 10))
                .assertNext(response -> {
                    assertNotNull(response);
                    assertEquals(1, response.totalElements());
                    assertEquals(1, response.content().size());
                    assertEquals(0, response.currentPage());
                    assertTrue(response.first());
                    assertTrue(response.last());
                })
                .verifyComplete();

        verify(userRepository).findUsersWithFilters("test@example.com", "testuser", 10, 0);
        verify(userRepository).countUsersWithFilters("test@example.com", "testuser");
    }

    @Test
    @DisplayName("Получение пустого списка пользователей")
    void getUsers_EmptyList() {
        when(userRepository.findUsersWithFilters(any(), any(), anyInt(), anyInt()))
                .thenReturn(List.of());
        when(userRepository.countUsersWithFilters(any(), any())).thenReturn(0L);

        StepVerifier.create(userService.getUsers(null, null, 0, 10))
                .assertNext(response -> {
                    assertNotNull(response);
                    assertEquals(0, response.totalElements());
                    assertEquals(0, response.content().size());
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Обновление пользователя - успешное")
    void updateUser_Success() {
        User updatedUser = User.builder()
                .id(1L)
                .email(testUser.getEmail())
                .username("updatedusername")
                .passwordHash(testUser.getPasswordHash())
                .roleId(testUser.getRoleId())
                .build();

        UserResponseDTO updatedResponse = new UserResponseDTO(
                updatedUser.getId(),
                updatedUser.getEmail(),
                updatedUser.getUsername()
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);
        when(userMapper.toResponseDTO(any(User.class))).thenReturn(updatedResponse);

        StepVerifier.create(userService.updateUser(1L, updateRequest))
                .assertNext(response -> {
                    assertNotNull(response);
                    assertEquals("updatedusername", response.username());
                })
                .verifyComplete();

        verify(userRepository).findById(1L);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Обновление пользователя - не найден")
    void updateUser_NotFound() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        StepVerifier.create(userService.updateUser(999L, updateRequest))
                .expectError(UserNotFoundException.class)
                .verify();

        verify(userRepository).findById(999L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Удаление пользователя - успешное")
    void deleteUser_Success() {
        when(userRepository.existsById(1L)).thenReturn(true);
        doNothing().when(userRepository).deleteById(1L);

        StepVerifier.create(userService.deleteUser(1L))
                .verifyComplete();

        verify(userRepository).existsById(1L);
        verify(userRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Удаление пользователя - не найден")
    void deleteUser_NotFound() {
        when(userRepository.existsById(anyLong())).thenReturn(false);

        StepVerifier.create(userService.deleteUser(999L))
                .expectError(UserNotFoundException.class)
                .verify();

        verify(userRepository).existsById(999L);
        verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Проверка существования пользователя - существует")
    void userExists_True() {
        when(userRepository.existsById(1L)).thenReturn(true);

        StepVerifier.create(userService.userExists(1L))
                .assertNext(Assertions::assertTrue)
                .verifyComplete();

        verify(userRepository).existsById(1L);
    }

    @Test
    @DisplayName("Проверка существования пользователя - не существует")
    void userExists_False() {
        when(userRepository.existsById(999L)).thenReturn(false);

        StepVerifier.create(userService.userExists(999L))
                .assertNext(Assertions::assertFalse)
                .verifyComplete();

        verify(userRepository).existsById(999L);
    }

    @Test
    @DisplayName("Получение сущности пользователя по ID")
    void getEntityById_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        StepVerifier.create(userService.getEntityById(1L))
                .assertNext(user -> {
                    assertNotNull(user);
                    assertEquals(testUser.getId(), user.getId());
                    assertEquals(testUser.getEmail(), user.getEmail());
                })
                .verifyComplete();

        verify(userRepository).findById(1L);
    }

    @Test
    @DisplayName("Получение сущности пользователя по ID или null")
    void getEntityByIdOrNull_Found() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        StepVerifier.create(userService.getEntityByIdOrNull(1L))
                .assertNext(user -> {
                    assertNotNull(user);
                    assertEquals(testUser.getId(), user.getId());
                })
                .verifyComplete();

        verify(userRepository).findById(1L);
    }

    @Test
    @DisplayName("Получение сущности пользователя по ID или null - не найден")
    void getEntityByIdOrNull_NotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        StepVerifier.create(userService.getEntityByIdOrNull(999L))
                .verifyComplete(); // Ожидаем пустое завершение без элементов

        verify(userRepository).findById(999L);
    }
}

