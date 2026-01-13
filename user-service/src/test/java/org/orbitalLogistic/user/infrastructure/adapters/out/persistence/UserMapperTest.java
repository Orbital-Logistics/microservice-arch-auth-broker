package org.orbitalLogistic.user.infrastructure.adapters.out.persistence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.orbitalLogistic.user.domain.model.Role;
import org.orbitalLogistic.user.domain.model.User;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UserMapper Tests")
class UserMapperTest {

    private UserPersistenceMapper userMapper;
    private RolePersistenceMapper roleMapper;

    @BeforeEach
    void setUp() {
        roleMapper = new RolePersistenceMapper();
        userMapper = new UserPersistenceMapper(roleMapper);
    }

    @Test
    @DisplayName("Should map user entity to domain")
    void shouldMapUserEntityToDomain() {
        // Given
        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setId(1L);
        roleEntity.setName("ADMIN");

        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setUsername("testuser");
        userEntity.setPasswordHash("encoded_password");
        userEntity.setEmail("test@example.com");
        userEntity.setEnabled(true);
        userEntity.setRoles(Set.of(roleEntity));

        // When
        User user = userMapper.toDomain(userEntity);

        // Then
        assertNotNull(user);
        assertEquals(1L, user.getId());
        assertEquals("testuser", user.getUsername());
        assertEquals("encoded_password", user.getPassword());
        assertEquals("test@example.com", user.getEmail());
        assertTrue(user.getEnabled());
        assertEquals(1, user.getRoles().size());
        assertTrue(user.getRoles().stream().anyMatch(r -> r.getName().equals("ADMIN")));
    }

    @Test
    @DisplayName("Should map user domain to entity")
    void shouldMapUserDomainToEntity() {
        // Given
        Role role = Role.builder()
                .id(1L)
                .name("ADMIN")
                .build();

        User user = User.builder()
                .id(1L)
                .username("testuser")
                .password("encoded_password")
                .email("test@example.com")
                .enabled(true)
                .roles(Set.of(role))
                .build();

        // When
        UserEntity userEntity = userMapper.toEntity(user);

        // Then
        assertNotNull(userEntity);
        assertEquals(1L, userEntity.getId());
        assertEquals("testuser", userEntity.getUsername());
        assertEquals("encoded_password", userEntity.getPasswordHash());
        assertEquals("test@example.com", userEntity.getEmail());
        assertTrue(userEntity.getEnabled());
        assertEquals(1, userEntity.getRoles().size());
    }

    @Test
    @DisplayName("Should handle empty roles when mapping to domain")
    void shouldHandleEmptyRolesWhenMappingToDomain() {
        // Given
        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setUsername("testuser");
        userEntity.setPasswordHash("encoded_password");
        userEntity.setEmail("test@example.com");
        userEntity.setEnabled(true);
        userEntity.setRoles(new HashSet<>());

        // When
        User user = userMapper.toDomain(userEntity);

        // Then
        assertNotNull(user);
        assertNotNull(user.getRoles());
        assertTrue(user.getRoles().isEmpty());
    }

    @Test
    @DisplayName("Should handle empty roles when mapping to entity")
    void shouldHandleEmptyRolesWhenMappingToEntity() {
        // Given
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .password("encoded_password")
                .email("test@example.com")
                .enabled(true)
                .roles(new HashSet<>())
                .build();

        // When
        UserEntity userEntity = userMapper.toEntity(user);

        // Then
        assertNotNull(userEntity);
        assertNotNull(userEntity.getRoles());
        assertTrue(userEntity.getRoles().isEmpty());
    }

    @Test
    @DisplayName("Should map multiple roles correctly")
    void shouldMapMultipleRolesCorrectly() {
        // Given
        Role role1 = Role.builder().id(1L).name("ADMIN").build();
        Role role2 = Role.builder().id(2L).name("USER").build();

        User user = User.builder()
                .id(1L)
                .username("testuser")
                .password("encoded_password")
                .email("test@example.com")
                .enabled(true)
                .roles(Set.of(role1, role2))
                .build();

        // When
        UserEntity userEntity = userMapper.toEntity(user);

        // Then
        assertNotNull(userEntity);
        assertEquals(2, userEntity.getRoles().size());
    }
}
