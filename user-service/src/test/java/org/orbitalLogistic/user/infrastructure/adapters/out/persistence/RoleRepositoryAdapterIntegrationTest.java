package org.orbitalLogistic.user.infrastructure.adapters.out.persistence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.orbitalLogistic.user.domain.model.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({RoleRepositoryAdapter.class, RolePersistenceMapper.class})
@DisplayName("RoleRepositoryAdapter Integration Tests")
@Tag("integration-tests")
class RoleRepositoryAdapterIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private RoleRepositoryAdapter roleRepositoryAdapter;

    @Autowired
    private RoleJdbcRepository roleJdbcRepository;

    @BeforeEach
    void setUp() {
        roleJdbcRepository.deleteAll();
    }

    @Test
    @DisplayName("Should save and find role by name")
    void shouldSaveAndFindRoleByName() {
        // Given
        Role role = Role.builder()
                .name("TEST_ROLE_1")
                .build();

        // When
        Role savedRole = roleRepositoryAdapter.save(role);
        Optional<Role> foundRole = roleRepositoryAdapter.findByName("TEST_ROLE_1");

        // Then
        assertTrue(foundRole.isPresent());
        assertEquals("TEST_ROLE_1", foundRole.get().getName());
        assertNotNull(savedRole.getId());
    }

    @Test
    @DisplayName("Should find all roles")
    void shouldFindAllRoles() {
        // Given
        Role role1 = Role.builder().name("TEST_ROLE_2").build();
        Role role2 = Role.builder().name("TEST_ROLE_3").build();
        
        roleRepositoryAdapter.save(role1);
        roleRepositoryAdapter.save(role2);

        // When
        List<Role> roles = roleRepositoryAdapter.findAll();

        // Then
        assertTrue(roles.size() >= 2);
    }

    @Test
    @DisplayName("Should check if role exists by name")
    void shouldCheckIfRoleExistsByName() {
        // Given
        Role role = Role.builder().name("TEST_ROLE_4").build();
        roleRepositoryAdapter.save(role);

        // When
        boolean exists = roleRepositoryAdapter.existsByName("TEST_ROLE_4");
        boolean notExists = roleRepositoryAdapter.existsByName("UNKNOWN_ROLE_XYZ");

        // Then
        assertTrue(exists);
        assertFalse(notExists);
    }

    @Test
    @DisplayName("Should delete role by id")
    void shouldDeleteRoleById() {
        // Given
        Role role = Role.builder().name("TEST_ROLE_5").build();
        Role savedRole = roleRepositoryAdapter.save(role);

        // When
        roleRepositoryAdapter.deleteById(savedRole.getId());
        Optional<Role> foundRole = roleRepositoryAdapter.findById(savedRole.getId());

        // Then
        assertFalse(foundRole.isPresent());
    }
}
