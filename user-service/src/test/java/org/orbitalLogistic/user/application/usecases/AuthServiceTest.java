package org.orbitalLogistic.user.application.usecases;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.orbitalLogistic.user.application.ports.in.LoginCommand;
import org.orbitalLogistic.user.application.ports.in.RegisterCommand;
import org.orbitalLogistic.user.application.ports.out.*;
import org.orbitalLogistic.user.domain.exception.InvalidCredentialsException;
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
@DisplayName("AuthService Tests")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoderPort passwordEncoder;

    @Mock
    private JwtTokenPort jwtTokenPort;

    @Mock
    private ReportSender reportSender;

    @InjectMocks
    private AuthService authService;

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
    @DisplayName("Should login successfully with valid credentials")
    void shouldLoginSuccessfully() {
        // Given
        LoginCommand command = new LoginCommand("testuser", "password123");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "encoded_password")).thenReturn(true);
        when(jwtTokenPort.generateToken(testUser)).thenReturn("jwt_token");

        // When
        String token = authService.login(command);

        // Then
        assertNotNull(token);
        assertEquals("jwt_token", token);
        
        verify(userRepository).findByUsername("testuser");
        verify(passwordEncoder).matches("password123", "encoded_password");
        verify(jwtTokenPort).generateToken(testUser);
    }

    @Test
    @DisplayName("Should throw exception when user not found")
    void shouldThrowExceptionWhenUserNotFound() {
        // Given
        LoginCommand command = new LoginCommand("unknown", "password123");
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UserNotFoundException.class, () -> authService.login(command));
        
        verify(userRepository).findByUsername("unknown");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtTokenPort, never()).generateToken(any());
    }

    @Test
    @DisplayName("Should throw exception when password is invalid")
    void shouldThrowExceptionWhenPasswordInvalid() {
        // Given
        LoginCommand command = new LoginCommand("testuser", "wrong_password");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrong_password", "encoded_password")).thenReturn(false);

        // When & Then
        assertThrows(InvalidCredentialsException.class, () -> authService.login(command));
        
        verify(userRepository).findByUsername("testuser");
        verify(passwordEncoder).matches("wrong_password", "encoded_password");
        verify(jwtTokenPort, never()).generateToken(any());
    }

    @Test
    @DisplayName("Should register user successfully")
    void shouldRegisterSuccessfully() {
        // Given
        RegisterCommand command = new RegisterCommand(
                "newuser",
                "password123",
                "new@example.com",
                Set.of(1L)
        );
        
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(roleRepository.findById(1L)).thenReturn(Optional.of(testRole));
        when(passwordEncoder.encode("password123")).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtTokenPort.generateToken(any(User.class))).thenReturn("jwt_token");

        // When
        String token = authService.register(command);

        // Then
        assertNotNull(token);
        assertEquals("jwt_token", token);
        
        verify(userRepository).existsByUsername("newuser");
        verify(userRepository).existsByEmail("new@example.com");
        verify(roleRepository).findById(1L);
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
        verify(jwtTokenPort).generateToken(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when username already exists during registration")
    void shouldThrowExceptionWhenUsernameExistsOnRegister() {
        // Given
        RegisterCommand command = new RegisterCommand(
                "testuser",
                "password123",
                "new@example.com",
                Set.of(1L)
        );
        
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        // When & Then
        assertThrows(UserAlreadyExistsException.class, () -> authService.register(command));
        
        verify(userRepository).existsByUsername("testuser");
        verify(userRepository, never()).save(any());
    }
}
