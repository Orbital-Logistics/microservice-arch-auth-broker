package org.orbitalLogistic.user.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("User Domain Model Tests")
class UserTest {

    @Test
    @DisplayName("Should create user with builder")
    void shouldCreateUserWithBuilder() {
        // Given & When
        Role role = Role.builder().id(1L).name("ADMIN").build();
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .password("password")
                .email("test@example.com")
                .enabled(true)
                .roles(Set.of(role))
                .build();

        // Then
        assertNotNull(user);
        assertEquals(1L, user.getId());
        assertEquals("testuser", user.getUsername());
        assertEquals("password", user.getPassword());
        assertEquals("test@example.com", user.getEmail());
        assertTrue(user.getEnabled());
        assertEquals(1, user.getRoles().size());
    }

    @Test
    @DisplayName("Should create user with toBuilder")
    void shouldCreateUserWithToBuilder() {
        // Given
        Role role = Role.builder().id(1L).name("ADMIN").build();
        User originalUser = User.builder()
                .id(1L)
                .username("testuser")
                .password("password")
                .email("test@example.com")
                .enabled(true)
                .roles(Set.of(role))
                .build();

        // When
        User modifiedUser = originalUser.toBuilder()
                .username("newusername")
                .email("new@example.com")
                .build();

        // Then
        assertEquals("newusername", modifiedUser.getUsername());
        assertEquals("new@example.com", modifiedUser.getEmail());
        assertEquals(originalUser.getId(), modifiedUser.getId());
        assertEquals(originalUser.getPassword(), modifiedUser.getPassword());
    }

    @Test
    @DisplayName("Should handle empty roles")
    void shouldHandleEmptyRoles() {
        // Given & When
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .password("password")
                .email("test@example.com")
                .enabled(true)
                .roles(new HashSet<>())
                .build();

        // Then
        assertNotNull(user.getRoles());
        assertTrue(user.getRoles().isEmpty());
    }

    @Test
    @DisplayName("Should test equals and hashCode")
    void shouldTestEqualsAndHashCode() {
        // Given
        Role role = Role.builder().id(1L).name("ADMIN").build();
        User user1 = User.builder()
                .id(1L)
                .username("testuser")
                .password("password")
                .email("test@example.com")
                .enabled(true)
                .roles(Set.of(role))
                .build();

        User user2 = User.builder()
                .id(1L)
                .username("testuser")
                .password("password")
                .email("test@example.com")
                .enabled(true)
                .roles(Set.of(role))
                .build();

        User user3 = User.builder()
                .id(2L)
                .username("otheruser")
                .password("password")
                .email("other@example.com")
                .enabled(true)
                .roles(Set.of(role))
                .build();

        // Then
        assertEquals(user1, user2);
        assertNotEquals(user1, user3);
        assertEquals(user1.hashCode(), user2.hashCode());
    }

    @Test
    @DisplayName("Should test toString")
    void shouldTestToString() {
        // Given
        Role role = Role.builder().id(1L).name("ADMIN").build();
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .password("password")
                .email("test@example.com")
                .enabled(true)
                .roles(Set.of(role))
                .build();

        // When
        String userString = user.toString();

        // Then
        assertNotNull(userString);
        assertTrue(userString.contains("testuser"));
        assertTrue(userString.contains("test@example.com"));
    }

    @Test
    @DisplayName("Should handle disabled user")
    void shouldHandleDisabledUser() {
        // Given & When
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .password("password")
                .email("test@example.com")
                .enabled(false)
                .roles(new HashSet<>())
                .build();

        // Then
        assertFalse(user.getEnabled());
    }

    @Test
    @DisplayName("Should add and remove roles")
    void shouldAddAndRemoveRoles() {
        // Given
        Role role1 = Role.builder().id(1L).name("ADMIN").build();
        Role role2 = Role.builder().id(2L).name("USER").build();
        
        Set<Role> roles = new HashSet<>();
        roles.add(role1);
        
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .password("password")
                .email("test@example.com")
                .enabled(true)
                .roles(roles)
                .build();

        // When - add role
        User userWithNewRole = user.toBuilder()
                .roles(new HashSet<>(user.getRoles()) {{ add(role2); }})
                .build();

        // Then
        assertEquals(2, userWithNewRole.getRoles().size());

        // When - remove role
        Set<Role> remainingRoles = new HashSet<>(userWithNewRole.getRoles());
        remainingRoles.remove(role2);
        User userWithRemovedRole = userWithNewRole.toBuilder()
                .roles(remainingRoles)
                .build();

        // Then
        assertEquals(1, userWithRemovedRole.getRoles().size());
    }
}
