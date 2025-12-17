package org.orbitalLogistic.user.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.orbitalLogistic.user.entities.User;
import org.orbitalLogistic.user.entities.UserRole;
import org.orbitalLogistic.user.repositories.UserRepository;
import org.orbitalLogistic.user.repositories.UserRoleRepository;
import org.orbitalLogistic.user.TestcontainersConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
@Tag("integration-tests")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    private UserRole testRole;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        if (userRoleRepository.findByName("test_role").isEmpty()) {
            UserRole role = new UserRole();
            role.setName("test_role");
            role.setDescription("Test Role");
            testRole = userRoleRepository.save(role);
        } else {
            testRole = userRoleRepository.findByName("test_role").get();
        }
    }

    @Test
    @DisplayName("Сохранение и поиск пользователя по ID")
    void saveAndFindById() {
        User user = User.builder()
                .email("test@example.com")
                .username("testuser")
                .passwordHash("hashedpassword")
                .roleId(testRole.getId())
                .build();

        User savedUser = userRepository.save(user);
        Optional<User> foundUser = userRepository.findById(savedUser.getId());

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("test@example.com");
        assertThat(foundUser.get().getUsername()).isEqualTo("testuser");
        assertThat(foundUser.get().getRoleId()).isEqualTo(testRole.getId());
    }

    @Test
    @DisplayName("Поиск пользователя по email")
    void findByEmail() {
        User user = User.builder()
                .email("findme@example.com")
                .username("findme")
                .passwordHash("password")
                .roleId(testRole.getId())
                .build();
        userRepository.save(user);

        Optional<User> foundUser = userRepository.findByEmail("findme@example.com");

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("findme@example.com");
        assertThat(foundUser.get().getUsername()).isEqualTo("findme");
    }

    @Test
    @DisplayName("Поиск несуществующего пользователя по email")
    void findByEmail_NotFound() {
        Optional<User> foundUser = userRepository.findByEmail("nonexistent@example.com");

        assertThat(foundUser).isEmpty();
    }

    @Test
    @DisplayName("Проверка существования пользователя по email")
    void existsByEmail() {
        User user = User.builder()
                .email("exists@example.com")
                .username("existsuser")
                .passwordHash("password")
                .roleId(testRole.getId())
                .build();
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
                .passwordHash("password")
                .roleId(testRole.getId())
                .build();
        userRepository.save(user);

        assertThat(userRepository.existsByUsername("uniqueusername")).isTrue();
        assertThat(userRepository.existsByUsername("nonexistent")).isFalse();
    }

    @Test
    @DisplayName("Поиск пользователей с фильтрами по email и username")
    void findUsersWithFilters() {
        User user1 = User.builder()
                .email("alice@example.com")
                .username("alice")
                .passwordHash("password")
                .roleId(testRole.getId())
                .build();

        User user2 = User.builder()
                .email("bob@example.com")
                .username("bob")
                .passwordHash("password")
                .roleId(testRole.getId())
                .build();

        User user3 = User.builder()
                .email("alice.smith@example.com")
                .username("alicesmith")
                .passwordHash("password")
                .roleId(testRole.getId())
                .build();

        userRepository.save(user1);
        userRepository.save(user2);
        userRepository.save(user3);

        List<User> foundByEmail = userRepository.findUsersWithFilters("alice", null, 10, 0);

        assertThat(foundByEmail).hasSize(2);
        assertThat(foundByEmail).extracting(User::getEmail)
                .containsExactlyInAnyOrder("alice@example.com", "alice.smith@example.com");
    }

    @Test
    @DisplayName("Поиск пользователей с фильтрами - фильтр по username")
    void findUsersWithFilters_ByUsername() {
        User user1 = User.builder()
                .email("test1@example.com")
                .username("johndoe")
                .passwordHash("password")
                .roleId(testRole.getId())
                .build();

        User user2 = User.builder()
                .email("test2@example.com")
                .username("janedoe")
                .passwordHash("password")
                .roleId(testRole.getId())
                .build();

        userRepository.save(user1);
        userRepository.save(user2);

        List<User> foundByUsername = userRepository.findUsersWithFilters(null, "john", 10, 0);

        assertThat(foundByUsername).hasSize(1);
        assertThat(foundByUsername.getFirst().getUsername()).isEqualTo("johndoe");
    }

    @Test
    @DisplayName("Поиск пользователей с пагинацией")
    void findUsersWithFilters_Pagination() {
        for (int i = 1; i <= 5; i++) {
            User user = User.builder()
                    .email("user" + i + "@example.com")
                    .username("user" + i)
                    .passwordHash("password")
                    .roleId(testRole.getId())
                    .build();
            userRepository.save(user);
        }

        List<User> page1 = userRepository.findUsersWithFilters(null, null, 2, 0);
        List<User> page2 = userRepository.findUsersWithFilters(null, null, 2, 2);

        assertThat(page1).hasSize(2);
        assertThat(page2).hasSize(2);
        assertThat(page1.getFirst().getId()).isNotEqualTo(page2.getFirst().getId());
    }

    @Test
    @DisplayName("Подсчет пользователей с фильтрами")
    void countUsersWithFilters() {
        User user1 = User.builder()
                .email("alice@example.com")
                .username("alice")
                .passwordHash("password")
                .roleId(testRole.getId())
                .build();

        User user2 = User.builder()
                .email("bob@example.com")
                .username("bob")
                .passwordHash("password")
                .roleId(testRole.getId())
                .build();

        User user3 = User.builder()
                .email("alice.smith@example.com")
                .username("alicesmith")
                .passwordHash("password")
                .roleId(testRole.getId())
                .build();

        userRepository.save(user1);
        userRepository.save(user2);
        userRepository.save(user3);

        long countByEmail = userRepository.countUsersWithFilters("alice", null);
        long countByUsername = userRepository.countUsersWithFilters(null, "bob");
        long countAll = userRepository.countUsersWithFilters(null, null);

        assertThat(countByEmail).isEqualTo(2);
        assertThat(countByUsername).isEqualTo(1);
        assertThat(countAll).isEqualTo(3);
    }

    @Test
    @DisplayName("Обновление пользователя")
    void updateUser() {
        User user = User.builder()
                .email("original@example.com")
                .username("original")
                .passwordHash("password")
                .roleId(testRole.getId())
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
                .passwordHash("password")
                .roleId(testRole.getId())
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
                .passwordHash("password")
                .roleId(testRole.getId())
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
                    .passwordHash("password")
                    .roleId(testRole.getId())
                    .build();
            userRepository.save(user);
        }

        long count = userRepository.count();

        assertThat(count).isEqualTo(3);
    }
}

