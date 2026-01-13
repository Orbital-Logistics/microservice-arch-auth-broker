package org.orbitalLogistic.user.application.usecases;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.orbitalLogistic.user.application.ports.out.RoleRepository;
import org.orbitalLogistic.user.domain.model.Role;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetRolesService Tests")
class GetRolesServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private GetRolesService getRolesService;

    private Role testRole;

    @BeforeEach
    void setUp() {
        testRole = Role.builder()
                .id(1L)
                .name("ADMIN")
                .build();
    }

    @Test
    @DisplayName("Should get role by id")
    void shouldGetRoleById() {
        // Given
        when(roleRepository.findById(1L)).thenReturn(Optional.of(testRole));

        // When
        Optional<Role> result = getRolesService.getById(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals("ADMIN", result.get().getName());
        verify(roleRepository).findById(1L);
    }

    @Test
    @DisplayName("Should return empty when role not found by id")
    void shouldReturnEmptyWhenRoleNotFoundById() {
        // Given
        when(roleRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<Role> result = getRolesService.getById(999L);

        // Then
        assertFalse(result.isPresent());
        verify(roleRepository).findById(999L);
    }

    @Test
    @DisplayName("Should get role by name")
    void shouldGetRoleByName() {
        // Given
        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.of(testRole));

        // When
        Optional<Role> result = getRolesService.getByName("ADMIN");

        // Then
        assertTrue(result.isPresent());
        assertEquals("ADMIN", result.get().getName());
        verify(roleRepository).findByName("ADMIN");
    }

    @Test
    @DisplayName("Should get all roles")
    void shouldGetAllRoles() {
        // Given
        Role role2 = Role.builder().id(2L).name("USER").build();
        List<Role> roles = Arrays.asList(testRole, role2);
        when(roleRepository.findAll()).thenReturn(roles);

        // When
        List<Role> result = getRolesService.getAll();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(roleRepository).findAll();
    }
}
