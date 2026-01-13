package org.orbitalLogistic.user.application.usecases;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.orbitalLogistic.user.application.ports.in.CreateRoleCommand;
import org.orbitalLogistic.user.application.ports.out.RoleRepository;
import org.orbitalLogistic.user.domain.exception.RoleAlreadyExistsException;
import org.orbitalLogistic.user.domain.model.Role;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateRoleService Tests")
class CreateRoleServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private CreateRoleService createRoleService;

    private CreateRoleCommand command;

    @BeforeEach
    void setUp() {
        command = new CreateRoleCommand("ADMIN");
    }

    @Test
    @DisplayName("Should create role successfully")
    void shouldCreateRoleSuccessfully() {
        // Given
        when(roleRepository.existsByName("ADMIN")).thenReturn(false);
        
        Role savedRole = Role.builder()
                .id(1L)
                .name("ADMIN")
                .build();
        
        when(roleRepository.save(any(Role.class))).thenReturn(savedRole);

        // When
        Role result = createRoleService.createRole(command);

        // Then
        assertNotNull(result);
        assertEquals("ADMIN", result.getName());
        assertEquals(1L, result.getId());
        
        verify(roleRepository).existsByName("ADMIN");
        verify(roleRepository).save(any(Role.class));
    }

    @Test
    @DisplayName("Should throw exception when role already exists")
    void shouldThrowExceptionWhenRoleExists() {
        // Given
        when(roleRepository.existsByName("ADMIN")).thenReturn(true);

        // When & Then
        assertThrows(RoleAlreadyExistsException.class, 
                () -> createRoleService.createRole(command));
        
        verify(roleRepository).existsByName("ADMIN");
        verify(roleRepository, never()).save(any());
    }
}
