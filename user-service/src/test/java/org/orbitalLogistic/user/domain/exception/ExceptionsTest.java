package org.orbitalLogistic.user.domain.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Domain Exceptions Tests")
class ExceptionsTest {

    @Test
    @DisplayName("Should create UserNotFoundException")
    void shouldCreateUserNotFoundException() {
        // Given
        String message = "User not found";

        // When
        UserNotFoundException exception = new UserNotFoundException(message);

        // Then
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
    }

    @Test
    @DisplayName("Should create RoleNotFoundException")
    void shouldCreateRoleNotFoundException() {
        // Given
        String message = "Role not found";

        // When
        RoleNotFoundException exception = new RoleNotFoundException(message);

        // Then
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
    }

    @Test
    @DisplayName("Should create UserAlreadyExistsException")
    void shouldCreateUserAlreadyExistsException() {
        // Given
        String message = "User already exists";

        // When
        UserAlreadyExistsException exception = new UserAlreadyExistsException(message);

        // Then
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
    }

    @Test
    @DisplayName("Should create EmailAlreadyExistsException")
    void shouldCreateEmailAlreadyExistsException() {
        // Given
        String message = "Email already exists";

        // When
        EmailAlreadyExistsException exception = new EmailAlreadyExistsException(message);

        // Then
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
    }

    @Test
    @DisplayName("Should create RoleAlreadyExistsException")
    void shouldCreateRoleAlreadyExistsException() {
        // Given
        String message = "Role already exists";

        // When
        RoleAlreadyExistsException exception = new RoleAlreadyExistsException(message);

        // Then
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
    }

    @Test
    @DisplayName("Should create InvalidCredentialsException")
    void shouldCreateInvalidCredentialsException() {
        // Given
        String message = "Invalid credentials";

        // When
        InvalidCredentialsException exception = new InvalidCredentialsException(message);

        // Then
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
    }

    @Test
    @DisplayName("Exceptions should be throwable")
    void exceptionsShouldBeThrowable() {
        // UserNotFoundException
        assertThrows(UserNotFoundException.class, () -> {
            throw new UserNotFoundException("User not found");
        });

        // RoleNotFoundException
        assertThrows(RoleNotFoundException.class, () -> {
            throw new RoleNotFoundException("Role not found");
        });

        // UserAlreadyExistsException
        assertThrows(UserAlreadyExistsException.class, () -> {
            throw new UserAlreadyExistsException("User exists");
        });

        // EmailAlreadyExistsException
        assertThrows(EmailAlreadyExistsException.class, () -> {
            throw new EmailAlreadyExistsException("Email exists");
        });

        // RoleAlreadyExistsException
        assertThrows(RoleAlreadyExistsException.class, () -> {
            throw new RoleAlreadyExistsException("Role exists");
        });

        // InvalidCredentialsException
        assertThrows(InvalidCredentialsException.class, () -> {
            throw new InvalidCredentialsException("Invalid");
        });
    }
}
