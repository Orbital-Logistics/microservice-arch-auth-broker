package org.orbitalLogistic.user.infrastructure.adapters.out.persistence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.orbitalLogistic.user.domain.model.Role;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RoleMapper Tests")
class RoleMapperTest {

    private RolePersistenceMapper roleMapper;

    @BeforeEach
    void setUp() {
        roleMapper = new RolePersistenceMapper();
    }

    @Test
    @DisplayName("Should map role entity to domain")
    void shouldMapRoleEntityToDomain() {
        // Given
        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setId(1L);
        roleEntity.setName("ADMIN");

        // When
        Role role = roleMapper.toDomain(roleEntity);

        // Then
        assertNotNull(role);
        assertEquals(1L, role.getId());
        assertEquals("ADMIN", role.getName());
    }

    @Test
    @DisplayName("Should map role domain to entity")
    void shouldMapRoleDomainToEntity() {
        // Given
        Role role = Role.builder()
                .id(1L)
                .name("ADMIN")
                .build();

        // When
        RoleEntity roleEntity = roleMapper.toEntity(role);

        // Then
        assertNotNull(roleEntity);
        assertEquals(1L, roleEntity.getId());
        assertEquals("ADMIN", roleEntity.getName());
    }

    @Test
    @DisplayName("Should handle null id when mapping to domain")
    void shouldHandleNullIdWhenMappingToDomain() {
        // Given
        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setName("ADMIN");

        // When
        Role role = roleMapper.toDomain(roleEntity);

        // Then
        assertNotNull(role);
        assertNull(role.getId());
        assertEquals("ADMIN", role.getName());
    }

    @Test
    @DisplayName("Should handle null id when mapping to entity")
    void shouldHandleNullIdWhenMappingToEntity() {
        // Given
        Role role = Role.builder()
                .name("ADMIN")
                .build();

        // When
        RoleEntity roleEntity = roleMapper.toEntity(role);

        // Then
        assertNotNull(roleEntity);
        assertNull(roleEntity.getId());
        assertEquals("ADMIN", roleEntity.getName());
    }
}
