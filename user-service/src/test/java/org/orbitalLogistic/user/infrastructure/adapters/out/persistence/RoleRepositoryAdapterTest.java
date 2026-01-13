package org.orbitalLogistic.user.infrastructure.adapters.out.persistence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.orbitalLogistic.user.domain.model.Role;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RoleRepositoryAdapter Tests")
class RoleRepositoryAdapterTest {

    @Mock
    private RoleJdbcRepository roleJdbcRepository;

    @Mock
    private RolePersistenceMapper roleMapper;

    @InjectMocks
    private RoleRepositoryAdapter roleRepositoryAdapter;

    private Role testRole;
    private RoleEntity testRoleEntity;

    @BeforeEach
    void setUp() {
        testRole = Role.builder()
                .id(1L)
                .name("ADMIN")
                .build();

        testRoleEntity = new RoleEntity();
        testRoleEntity.setId(1L);
        testRoleEntity.setName("ADMIN");
    }

    @Test
    @DisplayName("Should save role")
    void shouldSaveRole() {
        // Given
        when(roleMapper.toEntity(testRole)).thenReturn(testRoleEntity);
        when(roleJdbcRepository.save(testRoleEntity)).thenReturn(testRoleEntity);
        when(roleMapper.toDomain(testRoleEntity)).thenReturn(testRole);

        // When
        Role result = roleRepositoryAdapter.save(testRole);

        // Then
        assertNotNull(result);
        assertEquals("ADMIN", result.getName());
        verify(roleJdbcRepository).save(testRoleEntity);
    }

    @Test
    @DisplayName("Should find role by id")
    void shouldFindRoleById() {
        // Given
        when(roleJdbcRepository.findById(1L)).thenReturn(Optional.of(testRoleEntity));
        when(roleMapper.toDomain(testRoleEntity)).thenReturn(testRole);

        // When
        Optional<Role> result = roleRepositoryAdapter.findById(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals("ADMIN", result.get().getName());
        verify(roleJdbcRepository).findById(1L);
    }

    @Test
    @DisplayName("Should find role by name")
    void shouldFindRoleByName() {
        // Given
        when(roleJdbcRepository.findByName("ADMIN")).thenReturn(Optional.of(testRoleEntity));
        when(roleMapper.toDomain(testRoleEntity)).thenReturn(testRole);

        // When
        Optional<Role> result = roleRepositoryAdapter.findByName("ADMIN");

        // Then
        assertTrue(result.isPresent());
        assertEquals("ADMIN", result.get().getName());
        verify(roleJdbcRepository).findByName("ADMIN");
    }

    @Test
    @DisplayName("Should find all roles")
    void shouldFindAllRoles() {
        // Given
        RoleEntity role2 = new RoleEntity();
        role2.setId(2L);
        role2.setName("USER");
        
        List<RoleEntity> entities = Arrays.asList(testRoleEntity, role2);
        when(roleJdbcRepository.findAll()).thenReturn(entities);
        
        Role role2Domain = Role.builder().id(2L).name("USER").build();
        when(roleMapper.toDomain(testRoleEntity)).thenReturn(testRole);
        when(roleMapper.toDomain(role2)).thenReturn(role2Domain);

        // When
        List<Role> result = roleRepositoryAdapter.findAll();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(roleJdbcRepository).findAll();
    }

    @Test
    @DisplayName("Should delete role by id")
    void shouldDeleteRoleById() {
        // Given
        doNothing().when(roleJdbcRepository).deleteById(1L);

        // When
        roleRepositoryAdapter.deleteById(1L);

        // Then
        verify(roleJdbcRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Should check if role exists by name")
    void shouldCheckIfRoleExistsByName() {
        // Given
        when(roleJdbcRepository.existsByName("ADMIN")).thenReturn(true);
        when(roleJdbcRepository.existsByName("UNKNOWN")).thenReturn(false);

        // When
        boolean exists = roleRepositoryAdapter.existsByName("ADMIN");
        boolean notExists = roleRepositoryAdapter.existsByName("UNKNOWN");

        // Then
        assertTrue(exists);
        assertFalse(notExists);
        verify(roleJdbcRepository).existsByName("ADMIN");
        verify(roleJdbcRepository).existsByName("UNKNOWN");
    }
}
