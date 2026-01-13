package org.orbitalLogistic.user.application.usecases;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.orbitalLogistic.user.application.ports.in.CreateUserCommand;
import org.orbitalLogistic.user.application.ports.out.PasswordEncoderPort;
import org.orbitalLogistic.user.application.ports.out.RoleRepository;
import org.orbitalLogistic.user.application.ports.out.UserRepository;
import org.orbitalLogistic.user.domain.exception.EmailAlreadyExistsException;
import org.orbitalLogistic.user.domain.exception.RoleNotFoundException;
import org.orbitalLogistic.user.domain.exception.UserAlreadyExistsException;
import org.orbitalLogistic.user.domain.model.Role;
import org.orbitalLogistic.user.domain.model.User;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateUserService Tests")
class CreateUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoderPort passwordEncoder;

    @InjectMocks
    private CreateUserService createUserService;

    private CreateUserCommand command;
    private Role testRole;

    @BeforeEach
    void setUp() {
        testRole = Role.builder()
                .id(1L)
                .name("ADMIN")
                .build();

        command = new CreateUserCommand(
                "testuser",
                "password123",
                "test@example.com",
                Set.of(1L)
        );
    }

    @Test
    @DisplayName("Should create user successfully")
    void shouldCreateUserSuccessfully() {
        // Given
        when(userRepository.existsByUsername(command.username())).thenReturn(false);
        when(userRepository.existsByEmail(command.email())).thenReturn(false);
        when(roleRepository.findById(1L)).thenReturn(Optional.of(testRole));
        when(passwordEncoder.encode(command.password())).thenReturn("encoded_password");
        
        User savedUser = User.builder()
                .id(1L)
                .username(command.username())
                .password("encoded_password")
                .email(command.email())
                .enabled(true)
                .roles(Set.of(testRole))
                .build();
        
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // When
        User result = createUserService.createUser(command);

        // Then
        assertNotNull(result);
        assertEquals(command.username(), result.getUsername());
        assertEquals(command.email(), result.getEmail());
        assertTrue(result.getEnabled());
        assertEquals(1, result.getRoles().size());
        
        verify(userRepository).existsByUsername(command.username());
        verify(userRepository).existsByEmail(command.email());
        verify(roleRepository).findById(1L);
        verify(passwordEncoder).encode(command.password());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when username already exists")
    void shouldThrowExceptionWhenUsernameExists() {
        // Given
        when(userRepository.existsByUsername(command.username())).thenReturn(true);

        // When & Then
        assertThrows(UserAlreadyExistsException.class, 
                () -> createUserService.createUser(command));
        
        verify(userRepository).existsByUsername(command.username());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when email already exists")
    void shouldThrowExceptionWhenEmailExists() {
        // Given
        when(userRepository.existsByUsername(command.username())).thenReturn(false);
        when(userRepository.existsByEmail(command.email())).thenReturn(true);

        // When & Then
        assertThrows(EmailAlreadyExistsException.class, 
                () -> createUserService.createUser(command));
        
        verify(userRepository).existsByEmail(command.email());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when role not found")
    void shouldThrowExceptionWhenRoleNotFound() {
        // Given
        when(userRepository.existsByUsername(command.username())).thenReturn(false);
        when(userRepository.existsByEmail(command.email())).thenReturn(false);
        when(roleRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RoleNotFoundException.class, 
                () -> createUserService.createUser(command));
        
        verify(roleRepository).findById(1L);
        verify(userRepository, never()).save(any());
    }
}
