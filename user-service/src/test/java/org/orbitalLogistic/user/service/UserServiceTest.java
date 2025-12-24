package org.orbitalLogistic.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.orbitalLogistic.user.entities.User;
import org.orbitalLogistic.user.entities.Role;
import org.orbitalLogistic.user.exceptions.auth.UnknownUsernameException;
import org.orbitalLogistic.user.exceptions.common.BadRequestException;
import org.orbitalLogistic.user.repositories.RoleRepository;
import org.orbitalLogistic.user.repositories.UserRepository;
import org.orbitalLogistic.user.services.UserService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private Role testRole;

    @BeforeEach
    void setUp() {
        testRole = new Role();
        testRole.setId(1L);
        testRole.setName("logistics_officer");

        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .username("testuser")
                .password("hashedpassword123")
                .enabled(true)
                .build();
        testUser.getRoles().add(testRole);
    }

    @Test
    @DisplayName("Поиск пользователя по ID - успешный")
    void findUserById_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        Optional<User> result = userService.findUserById(1L);

        assertTrue(result.isPresent());
        assertEquals(testUser.getEmail(), result.get().getEmail());

        verify(userRepository).findById(1L);
    }

    @Test
    @DisplayName("Поиск пользователя по email - успешный")
    void findUserByEmail_Success() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        Optional<User> result = userService.findUserByEmail("test@example.com");

        assertTrue(result.isPresent());
        assertEquals(testUser.getUsername(), result.get().getUsername());

        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    @DisplayName("Обновление пользователя - успешное")
    void updateUser_Success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User updated = userService.updateUser("testuser", "updatedUser", null);

        assertNotNull(updated);
        assertEquals("updatedUser", updated.getUsername());
        verify(userRepository).findByUsername("testuser");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Обновление пользователя - не найден")
    void updateUser_NotFound() {
        when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());

        assertThrows(UnknownUsernameException.class, () -> userService.updateUser("missing", "new", null));

        verify(userRepository).findByUsername("missing");
    }

    @Test
    @DisplayName("Обновление пользователя - пустой новый username")
    void updateUser_EmptyNewUsername() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        assertThrows(BadRequestException.class, () -> userService.updateUser("testuser", "", null));

        verify(userRepository).findByUsername("testuser");
    }

    @Test
    @DisplayName("Удаление пользователя")
    void deleteUser_CallsRepository() {
        doNothing().when(userRepository).deleteById(1L);

        userService.deleteUser(1L);

        verify(userRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Проверка существования пользователя")
    void userExists() {
        when(userRepository.existsById(1L)).thenReturn(true);
        assertTrue(userService.userExists(1L));
        verify(userRepository).existsById(1L);

        when(userRepository.existsById(2L)).thenReturn(false);
        assertFalse(userService.userExists(2L));
        verify(userRepository).existsById(2L);
    }
}
