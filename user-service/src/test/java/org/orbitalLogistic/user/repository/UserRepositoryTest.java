package org.orbitalLogistic.user.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.orbitalLogistic.user.entities.User;
import org.orbitalLogistic.user.entities.Role;
import org.orbitalLogistic.user.TestcontainersConfiguration;
import org.orbitalLogistic.user.repositories.RoleRepository;
import org.orbitalLogistic.user.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
@Tag("integration-tests")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private Role testRole;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        if (roleRepository.findByName("test_role").isEmpty()) {
            Role role = new Role();
            role.setName("test_role");
            testRole = roleRepository.save(role);
        } else {
            testRole = roleRepository.findByName("test_role").get();
        }
    }

    @Test
    @DisplayName("Сохранение и поиск пользователя по ID")
    void saveAndFindById() {
        User user = User.builder()
                .email("test@example.com")
                .username("testuser")
                .password("hashedpassword")
                .enabled(true)
                .build();
        user.getRoles().add(testRole);

        User savedUser = userRepository.save(user);
        Optional<User> foundUser = userRepository.findById(savedUser.getId());

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("test@example.com");
        assertThat(foundUser.get().getUsername()).isEqualTo("testuser");
        assertThat(foundUser.get().getRoles()).isNotEmpty();
    }

    @Test
    @DisplayName("Поиск пользователя по email")
    void findByEmail() {
        User user = User.builder()
                .email("findme@example.com")
                .username("findme")
                .password("password")
                .enabled(true)
                .build();
        user.getRoles().add(testRole);
        userRepository.save(user);

        Optional<User> foundUser = userRepository.findByEmail("findme@example.com");

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("findme@example.com");
        assertThat(foundUser.get().getUsername()).isEqualTo("findme");
    }

    @Test
    @DisplayName("Проверка существования пользователя по email")
    void existsByEmail() {
        User user = User.builder()
                .email("exists@example.com")
                .username("existsuser")
                .password("password")
                .enabled(true)
                .build();
        user.getRoles().add(testRole);
        userRepository.save(user);

        assertThat(userRepository.existsByEmail("exists@example.com")).isTrue();
        assertThat(userRepository.existsByEmail("notexists@example.com")).isFalse();
    }

    @Test
    @DisplayName("Проверка существования пользователя по username")
    void existsByUsername() {
        User user = User.builder()
                .email("user@example.com")
                .username("uniqueusername")
                .password("password")
                .enabled(true)
                .build();
        user.getRoles().add(testRole);
        userRepository.save(user);

        assertThat(userRepository.existsByUsername("uniqueusername")).isTrue();
        assertThat(userRepository.existsByUsername("nonexistent")).isFalse();
    }

    @Test
    @DisplayName("Сохранение нескольких пользователей и получение списка через findAll")
    void saveMultipleAndFindAll() {
        for (int i = 1; i <= 3; i++) {
            User user = User.builder()
                    .email("user" + i + "@example.com")
                    .username("user" + i)
                    .password("password")
                    .enabled(true)
                    .build();
            user.getRoles().add(testRole);
            userRepository.save(user);
        }

        Iterable<User> all = userRepository.findAll();
        long count = all.spliterator().getExactSizeIfKnown();
        if (count == -1) {
            // fall back to manual count
            count = 0;
            for (User u : all) count++;
        }
        assertThat(count).isEqualTo(3);
    }

    @Test
    @DisplayName("Обновление пользователя")
    void updateUser() {
        User user = User.builder()
                .email("original@example.com")
                .username("original")
                .password("password")
                .enabled(true)
                .build();
        User savedUser = userRepository.save(user);

        savedUser.setUsername("updated");
        savedUser.setEmail("updated@example.com");
        User updatedUser = userRepository.save(savedUser);

        Optional<User> foundUser = userRepository.findById(updatedUser.getId());
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUsername()).isEqualTo("updated");
        assertThat(foundUser.get().getEmail()).isEqualTo("updated@example.com");
    }

    @Test
    @DisplayName("Удаление пользователя")
    void deleteUser() {
        User user = User.builder()
                .email("delete@example.com")
                .username("deleteuser")
                .password("password")
                .enabled(true)
                .build();
        User savedUser = userRepository.save(user);
        Long userId = savedUser.getId();

        userRepository.deleteById(userId);

        assertThat(userRepository.findById(userId)).isEmpty();
        assertThat(userRepository.existsById(userId)).isFalse();
    }

    @Test
    @DisplayName("Проверка существования пользователя по ID")
    void existsById() {
        User user = User.builder()
                .email("check@example.com")
                .username("checkuser")
                .password("password")
                .enabled(true)
                .build();
        User savedUser = userRepository.save(user);

        assertThat(userRepository.existsById(savedUser.getId())).isTrue();
        assertThat(userRepository.existsById(999999L)).isFalse();
    }

    @Test
    @DisplayName("Подсчет всех пользователей")
    void countAllUsers() {
        for (int i = 1; i <= 3; i++) {
            User user = User.builder()
                    .email("user" + i + "@example.com")
                    .username("user" + i)
                    .password("password")
                    .enabled(true)
                    .build();
            userRepository.save(user);
        }

        long count = userRepository.count();

        assertThat(count).isEqualTo(3);
    }
}
