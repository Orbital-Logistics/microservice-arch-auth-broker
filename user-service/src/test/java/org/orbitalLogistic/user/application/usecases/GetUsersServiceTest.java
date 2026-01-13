package org.orbitalLogistic.user.application.usecases;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.orbitalLogistic.user.application.ports.out.UserRepository;
import org.orbitalLogistic.user.domain.model.Role;
import org.orbitalLogistic.user.domain.model.User;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetUsersService Tests")
class GetUsersServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private GetUsersService getUsersService;

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
    }

    @Test
    @DisplayName("Should get user by id")
    void shouldGetUserById() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When
        Optional<User> result = getUsersService.getById(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
        verify(userRepository).findById(1L);
    }

    @Test
    @DisplayName("Should return empty when user not found by id")
    void shouldReturnEmptyWhenUserNotFoundById() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<User> result = getUsersService.getById(999L);

        // Then
        assertFalse(result.isPresent());
        verify(userRepository).findById(999L);
    }

    @Test
    @DisplayName("Should get user by username")
    void shouldGetUserByUsername() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // When
        Optional<User> result = getUsersService.getByUsername("testuser");

        // Then
        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    @DisplayName("Should get user by email")
    void shouldGetUserByEmail() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // When
        Optional<User> result = getUsersService.getByEmail("test@example.com");

        // Then
        assertTrue(result.isPresent());
        assertEquals("test@example.com", result.get().getEmail());
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    @DisplayName("Should check if user exists by id")
    void shouldCheckIfUserExistsById() {
        // Given
        when(userRepository.existsById(1L)).thenReturn(true);
        when(userRepository.existsById(999L)).thenReturn(false);

        // When
        boolean exists = getUsersService.existsById(1L);
        boolean notExists = getUsersService.existsById(999L);

        // Then
        assertTrue(exists);
        assertFalse(notExists);
        verify(userRepository).existsById(1L);
        verify(userRepository).existsById(999L);
    }

    @Test
    @DisplayName("Should check if user exists by username")
    void shouldCheckIfUserExistsByUsername() {
        // Given
        when(userRepository.existsByUsername("testuser")).thenReturn(true);
        when(userRepository.existsByUsername("unknown")).thenReturn(false);

        // When
        boolean exists = getUsersService.existsByUsername("testuser");
        boolean notExists = getUsersService.existsByUsername("unknown");

        // Then
        assertTrue(exists);
        assertFalse(notExists);
        verify(userRepository).existsByUsername("testuser");
        verify(userRepository).existsByUsername("unknown");
    }

    @Test
    @DisplayName("Should check if user exists by email")
    void shouldCheckIfUserExistsByEmail() {
        // Given
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);
        when(userRepository.existsByEmail("unknown@example.com")).thenReturn(false);

        // When
        boolean exists = getUsersService.existsByEmail("test@example.com");
        boolean notExists = getUsersService.existsByEmail("unknown@example.com");

        // Then
        assertTrue(exists);
        assertFalse(notExists);
        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository).existsByEmail("unknown@example.com");
    }
}
