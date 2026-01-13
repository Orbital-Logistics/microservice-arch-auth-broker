package org.orbitalLogistic.user.infrastructure.adapters.out.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PasswordEncoderAdapter Tests")
class PasswordEncoderAdapterTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private PasswordEncoderAdapter passwordEncoderAdapter;

    @Test
    @DisplayName("Should encode password")
    void shouldEncodePassword() {
        // Given
        String rawPassword = "password123";
        String encodedPassword = "$2a$10$encoded_password";
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);

        // When
        String result = passwordEncoderAdapter.encode(rawPassword);

        // Then
        assertEquals(encodedPassword, result);
        verify(passwordEncoder).encode(rawPassword);
    }

    @Test
    @DisplayName("Should match passwords when correct")
    void shouldMatchPasswordsWhenCorrect() {
        // Given
        String rawPassword = "password123";
        String encodedPassword = "$2a$10$encoded_password";
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true);

        // When
        boolean result = passwordEncoderAdapter.matches(rawPassword, encodedPassword);

        // Then
        assertTrue(result);
        verify(passwordEncoder).matches(rawPassword, encodedPassword);
    }

    @Test
    @DisplayName("Should not match passwords when incorrect")
    void shouldNotMatchPasswordsWhenIncorrect() {
        // Given
        String rawPassword = "wrongpassword";
        String encodedPassword = "$2a$10$encoded_password";
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(false);

        // When
        boolean result = passwordEncoderAdapter.matches(rawPassword, encodedPassword);

        // Then
        assertFalse(result);
        verify(passwordEncoder).matches(rawPassword, encodedPassword);
    }
}
