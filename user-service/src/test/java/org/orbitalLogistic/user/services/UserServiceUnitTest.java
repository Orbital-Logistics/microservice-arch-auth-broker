package org.orbitalLogistic.user.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.orbitalLogistic.user.entities.User;
import org.orbitalLogistic.user.exceptions.common.BadRequestException;
import org.orbitalLogistic.user.exceptions.auth.UnknownUsernameException;
import org.orbitalLogistic.user.repositories.UserRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class UserServiceUnitTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void updateUser_success_changesUsernameAndEmail() {
        User u = new User();
        u.setId(1L);
        u.setUsername("old");
        u.setEmail("old@example.com");

        when(userRepository.findByUsername("old")).thenReturn(Optional.of(u));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.updateUser("old", "newname", "new@example.com");

        assertThat(result.getUsername()).isEqualTo("newname");
        assertThat(result.getEmail()).isEqualTo("new@example.com");

        verify(userRepository).save(u);
    }

    @Test
    void updateUser_unknownUsername_throws() {
        when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser("missing", "a", null))
                .isInstanceOf(UnknownUsernameException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_emptyNewUsername_throwsBadRequest() {
        User u = new User();
        u.setUsername("joe");
        when(userRepository.findByUsername("joe")).thenReturn(Optional.of(u));

        assertThatThrownBy(() -> userService.updateUser("joe", "", null))
                .isInstanceOf(BadRequestException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_emptyEmail_throwsBadRequest() {
        User u = new User();
        u.setUsername("anna");
        when(userRepository.findByUsername("anna")).thenReturn(Optional.of(u));

        assertThatThrownBy(() -> userService.updateUser("anna", null, ""))
                .isInstanceOf(BadRequestException.class);

        verify(userRepository, never()).save(any());
    }
}

