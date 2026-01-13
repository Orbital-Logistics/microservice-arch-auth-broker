package org.orbitalLogistic.user.application.usecases;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.orbitalLogistic.user.application.ports.in.UpdateUserCommand;
import org.orbitalLogistic.user.application.ports.out.PasswordEncoderPort;
import org.orbitalLogistic.user.application.ports.out.RoleRepository;
import org.orbitalLogistic.user.application.ports.out.UserRepository;
import org.orbitalLogistic.user.domain.exception.EmailAlreadyExistsException;
import org.orbitalLogistic.user.domain.exception.RoleNotFoundException;
import org.orbitalLogistic.user.domain.exception.UserAlreadyExistsException;
import org.orbitalLogistic.user.domain.exception.UserNotFoundException;
import org.orbitalLogistic.user.domain.model.Role;
import org.orbitalLogistic.user.domain.model.User;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateUserService Tests")
class UpdateUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoderPort passwordEncoderPort;

    @InjectMocks
    private UpdateUserService updateUserService;

    private User existingUser;
    private Role testRole;

    @BeforeEach
    void setUp() {
        testRole = Role.builder()
                .id(1L)
                .name("ADMIN")
                .build();

        existingUser = User.builder()
                .id(1L)
                .username("oldusername")
                .password("encoded_password")
                .email("old@example.com")
                .enabled(true)
                .roles(Set.of(testRole))
                .build();
    }

    @Test
    @DisplayName("Should update user successfully")
    void shouldUpdateUserSuccessfully() {
        // Given
        UpdateUserCommand command = new UpdateUserCommand(
                1L,
                "newusername",
                "new@example.com"
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByUsername("newusername")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        
        User updatedUser = existingUser.toBuilder()
                .username("newusername")
                .email("new@example.com")
                .build();
        
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        // When
        User result = updateUserService.updateUser(command);

        // Then
        assertNotNull(result);
        assertEquals("newusername", result.getUsername());
        assertEquals("new@example.com", result.getEmail());
        
        verify(userRepository).findById(1L);
        verify(userRepository).existsByUsername("newusername");
        verify(userRepository).existsByEmail("new@example.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when user not found")
    void shouldThrowExceptionWhenUserNotFound() {
        // Given
        UpdateUserCommand command = new UpdateUserCommand(999L, "newusername", "new@example.com");
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UserNotFoundException.class, 
                () -> updateUserService.updateUser(command));
        
        verify(userRepository).findById(999L);
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when new username already exists")
    void shouldThrowExceptionWhenUsernameExists() {
        // Given
        UpdateUserCommand command = new UpdateUserCommand(1L, "existinguser", "new@example.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        // When & Then
        assertThrows(UserAlreadyExistsException.class, 
                () -> updateUserService.updateUser(command));
        
        verify(userRepository).existsByUsername("existinguser");
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when new email already exists")
    void shouldThrowExceptionWhenEmailExists() {
        // Given
        UpdateUserCommand command = new UpdateUserCommand(1L, "newusername", "existing@example.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByUsername("newusername")).thenReturn(false);
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        // When & Then
        assertThrows(EmailAlreadyExistsException.class, 
                () -> updateUserService.updateUser(command));
        
        verify(userRepository).existsByEmail("existing@example.com");
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should change password successfully")
    void shouldChangePasswordSuccessfully() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(passwordEncoderPort.encode("newpassword")).thenReturn("new_encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        // When
        updateUserService.changePassword(1L, "newpassword");

        // Then
        verify(userRepository).findById(1L);
        verify(passwordEncoderPort).encode("newpassword");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should grant roles successfully")
    void shouldGrantRolesSuccessfully() {
        // Given
        Role newRole = Role.builder().id(2L).name("USER").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(roleRepository.findById(2L)).thenReturn(Optional.of(newRole));
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        // When
        User result = updateUserService.grantRoles(1L, Set.of(2L));

        // Then
        assertNotNull(result);
        verify(userRepository).findById(1L);
        verify(roleRepository).findById(2L);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when granting non-existent role")
    void shouldThrowExceptionWhenGrantingNonExistentRole() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(roleRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RoleNotFoundException.class, 
                () -> updateUserService.grantRoles(1L, Set.of(999L)));
        
        verify(roleRepository).findById(999L);
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should revoke roles successfully")
    void shouldRevokeRolesSuccessfully() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        // When
        User result = updateUserService.revokeRoles(1L, Set.of(1L));

        // Then
        assertNotNull(result);
        verify(userRepository).findById(1L);
        verify(userRepository).save(any(User.class));
    }
}
