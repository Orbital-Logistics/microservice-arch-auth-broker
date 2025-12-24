package org.orbitalLogistic.user.services;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.orbitalLogistic.user.entities.User;
import org.orbitalLogistic.user.repositories.UserRepository;
import org.orbitalLogistic.user.exceptions.auth.UnknownUsernameException;
import org.orbitalLogistic.user.exceptions.common.BadRequestException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceExceptionTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void updateUser_NotFound_ThrowsUnknownUsernameException() {
        when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());

        assertThrows(UnknownUsernameException.class, () -> userService.updateUser("missing", "newName", null));

        verify(userRepository).findByUsername("missing");
    }

    @Test
    void updateUser_EmptyNewUsername_ThrowsBadRequestException() {
        User existing = new User();
        existing.setUsername("old");
        existing.setEmail("old@x.com");

        when(userRepository.findByUsername("old")).thenReturn(Optional.of(existing));

        assertThrows(BadRequestException.class, () -> userService.updateUser("old", "", null));

        verify(userRepository).findByUsername("old");
    }

    @Test
    void getByUsername_NotFound_ThrowsEntityNotFoundException() {
        when(userRepository.findByUsername("noone")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> userService.getByUsername("noone"));

        verify(userRepository).findByUsername("noone");
    }

    @Test
    void deleteUser_CallsRepositoryDeleteById() {
        doNothing().when(userRepository).deleteById(99L);

        userService.deleteUser(99L);

        verify(userRepository).deleteById(99L);
    }
}
