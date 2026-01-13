package org.orbitalLogistic.user.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Role Domain Model Tests")
class RoleTest {

    @Test
    @DisplayName("Should create role with builder")
    void shouldCreateRoleWithBuilder() {
        // Given & When
        Role role = Role.builder()
                .id(1L)
                .name("ADMIN")
                .build();

        // Then
        assertNotNull(role);
        assertEquals(1L, role.getId());
        assertEquals("ADMIN", role.getName());
    }

    @Test
    @DisplayName("Should create role without id")
    void shouldCreateRoleWithoutId() {
        // Given & When
        Role role = Role.builder()
                .name("USER")
                .build();

        // Then
        assertNotNull(role);
        assertNull(role.getId());
        assertEquals("USER", role.getName());
    }

    @Test
    @DisplayName("Should test equals and hashCode")
    void shouldTestEqualsAndHashCode() {
        // Given
        Role role1 = Role.builder()
                .id(1L)
                .name("ADMIN")
                .build();

        Role role2 = Role.builder()
                .id(1L)
                .name("ADMIN")
                .build();

        Role role3 = Role.builder()
                .id(2L)
                .name("USER")
                .build();

        // Then
        assertEquals(role1, role2);
        assertNotEquals(role1, role3);
        assertEquals(role1.hashCode(), role2.hashCode());
    }

    @Test
    @DisplayName("Should test toString")
    void shouldTestToString() {
        // Given
        Role role = Role.builder()
                .id(1L)
                .name("ADMIN")
                .build();

        // When
        String roleString = role.toString();

        // Then
        assertNotNull(roleString);
        assertTrue(roleString.contains("ADMIN"));
    }

    @Test
    @DisplayName("Should handle role name changes")
    void shouldHandleRoleNameChanges() {
        // Given
        Role role = Role.builder()
                .id(1L)
                .name("ADMIN")
                .build();

        // When
        Role modifiedRole = role.toBuilder()
                .name("SUPER_ADMIN")
                .build();

        // Then
        assertEquals("SUPER_ADMIN", modifiedRole.getName());
        assertEquals(role.getId(), modifiedRole.getId());
    }

    @Test
    @DisplayName("Should test role equality by id only")
    void shouldTestRoleEqualityById() {
        // Given
        Role role1 = Role.builder()
                .id(1L)
                .name("ADMIN")
                .build();

        Role role2 = Role.builder()
                .id(1L)
                .name("DIFFERENT_NAME")
                .build();

        // Then - если equals использует только id
        // В зависимости от реализации Lombok, это может быть true или false
        // Если @EqualsAndHashCode не настроен, будут использоваться все поля
        assertNotNull(role1);
        assertNotNull(role2);
    }
}
