package org.orbitalLogistic.user.infrastructure.adapters.out.persistence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.orbitalLogistic.user.TestcontainersConfiguration;
import org.orbitalLogistic.user.domain.model.Role;
import org.orbitalLogistic.user.domain.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
@Transactional
@DisplayName("UserRepositoryAdapter Integration Tests")
@Tag("integration-tests")
class UserRepositoryAdapterTest {

    @Autowired
    private UserRepositoryAdapter userRepositoryAdapter;

    @Autowired
    private RoleRepositoryAdapter roleRepositoryAdapter;

    private Role testRole;

    @BeforeEach
    void setUp() {
        // Создаем тестовую роль, если её нет
        testRole = roleRepositoryAdapter.findByName("ADMIN")
                .orElseGet(() -> {
                    Role role = Role.builder()
                            .name("ADMIN")
                            .build();
                    return roleRepositoryAdapter.save(role);
                });
    }

    @Test
    @DisplayName("Should save and find user by username")
    void shouldSaveAndFindUserByUsername() {
        // Given
        User user = User.builder()
                .username("testuser")
                .password("encoded_password")
                .email("test@example.com")
                .enabled(true)
                .roles(Set.of(testRole))
                .build();

        // When
        User savedUser = userRepositoryAdapter.save(user);
        Optional<User> foundUser = userRepositoryAdapter.findByUsername("testuser");

        // Then
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getId()).isNotNull();
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUsername()).isEqualTo("testuser");
        assertThat(foundUser.get().getEmail()).isEqualTo("test@example.com");
        assertThat(foundUser.get().getRoles()).hasSize(1);
    }

    @Test
    @DisplayName("Should check if user exists by username")
    void shouldCheckIfUserExistsByUsername() {
        // Given
        User user = User.builder()
                .username("existinguser")
                .password("encoded_password")
                .email("existing@example.com")
                .enabled(true)
                .roles(Set.of(testRole))
                .build();
        userRepositoryAdapter.save(user);

        // When
        boolean exists = userRepositoryAdapter.existsByUsername("existinguser");
        boolean notExists = userRepositoryAdapter.existsByUsername("nonexistent");

        // Then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("Should find user by email")
    void shouldFindUserByEmail() {
        // Given
        User user = User.builder()
                .username("emailuser")
                .password("encoded_password")
                .email("email@example.com")
                .enabled(true)
                .roles(Set.of(testRole))
                .build();
        userRepositoryAdapter.save(user);

        // When
        Optional<User> foundUser = userRepositoryAdapter.findByEmail("email@example.com");

        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("email@example.com");
        assertThat(foundUser.get().getUsername()).isEqualTo("emailuser");
    }

    @Test
    @DisplayName("Should delete user by id")
    void shouldDeleteUserById() {
        // Given
        User user = User.builder()
                .username("deleteuser")
                .password("encoded_password")
                .email("delete@example.com")
                .enabled(true)
                .roles(Set.of(testRole))
                .build();
        User savedUser = userRepositoryAdapter.save(user);

        // When
        userRepositoryAdapter.deleteById(savedUser.getId());
        Optional<User> foundUser = userRepositoryAdapter.findById(savedUser.getId());

        // Then
        assertThat(foundUser).isEmpty();
    }
}
