package org.orbitalLogistic.cargo.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.orbitalLogistic.cargo.CargoServiceApplication;
import org.orbitalLogistic.cargo.TestcontainersConfiguration;
import org.orbitalLogistic.cargo.entities.CargoCategory;
import org.orbitalLogistic.cargo.repositories.CargoCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJdbcTest
@Import(TestcontainersConfiguration.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = CargoServiceApplication.class)
@ActiveProfiles("test")
@Tag("integration-tests")
@TestPropertySource(properties = {
        "spring.cloud.config.enabled=false"
})
class CargoCategoryRepositoryTest {

    @Autowired
    private CargoCategoryRepository cargoCategoryRepository;

    private CargoCategory rootCategory;

    @BeforeEach
    void setUp() {
        rootCategory = cargoCategoryRepository.save(CargoCategory.builder()
                .name("Test Root Category")
                .parentCategoryId(null)
                .description("Root category for testing")
                .build());
    }

    @Test
    void save_Success() {
        CargoCategory category = CargoCategory.builder()
                .name("New Category")
                .parentCategoryId(null)
                .description("Test description")
                .build();

        CargoCategory saved = cargoCategoryRepository.save(category);

        assertNotNull(saved.getId());
        assertEquals("New Category", saved.getName());
    }

    @Test
    void findById_Success() {
        Optional<CargoCategory> found = cargoCategoryRepository.findById(rootCategory.getId());

        assertTrue(found.isPresent());
        assertEquals("Test Root Category", found.get().getName());
    }

    @Test
    void findById_NotFound() {
        Optional<CargoCategory> found = cargoCategoryRepository.findById(999L);
        assertFalse(found.isPresent());
    }

    @Test
    void findByName_Success() {
        Optional<CargoCategory> found = cargoCategoryRepository.findByName("Test Root Category");

        assertTrue(found.isPresent());
        assertEquals("Test Root Category", found.get().getName());
    }

    @Test
    void existsByName_True() {
        boolean exists = cargoCategoryRepository.existsByName("Test Root Category");
        assertTrue(exists);
    }

    @Test
    void existsByName_False() {
        boolean exists = cargoCategoryRepository.existsByName("NonExistent");
        assertFalse(exists);
    }

    @Test
    void findByParentCategoryIdIsNull_Success() {
        List<CargoCategory> rootCategories = cargoCategoryRepository.findByParentCategoryIdIsNull();

        assertFalse(rootCategories.isEmpty());
        assertTrue(rootCategories.stream().anyMatch(c -> c.getId().equals(rootCategory.getId())));
    }

    @Test
    void findByParentCategoryId_Success() {
        CargoCategory child = cargoCategoryRepository.save(CargoCategory.builder()
                .name("Child Category")
                .parentCategoryId(rootCategory.getId())
                .description("Child category")
                .build());

        List<CargoCategory> children = cargoCategoryRepository.findByParentCategoryId(rootCategory.getId());

        assertEquals(1, children.size());
        assertEquals("Child Category", children.getFirst().getName());
    }

    @Test
    void findWithFilters_All() {
        CargoCategory saved = cargoCategoryRepository.save(CargoCategory.builder()
                .name("Filtered Category")
                .parentCategoryId(null)
                .description("For filter testing")
                .build());

        List<CargoCategory> categories = cargoCategoryRepository.findWithFilters(null, 20, 0);

        assertFalse(categories.isEmpty());
        assertTrue(categories.stream().anyMatch(c -> c.getId().equals(saved.getId())));
    }

    @Test
    void findWithFilters_ByName() {
        cargoCategoryRepository.save(CargoCategory.builder()
                .name("Filtered Category")
                .parentCategoryId(null)
                .description("For filter testing")
                .build());

        List<CargoCategory> categories = cargoCategoryRepository.findWithFilters("Filtered", 20, 0);

        assertEquals(1, categories.size());
        assertEquals("Filtered Category", categories.getFirst().getName());
    }

    @Test
    void update_Success() {
        CargoCategory category = cargoCategoryRepository.findById(rootCategory.getId()).orElseThrow();
        category.setName("Updated Category");
        category.setDescription("Updated description");

        CargoCategory updated = cargoCategoryRepository.save(category);

        assertEquals("Updated Category", updated.getName());
        assertEquals("Updated description", updated.getDescription());
    }

    @Test
    void deleteById_Success() {
        CargoCategory category = cargoCategoryRepository.save(CargoCategory.builder()
                .name("To Delete")
                .parentCategoryId(null)
                .description("Will be deleted")
                .build());

        cargoCategoryRepository.deleteById(category.getId());

        assertFalse(cargoCategoryRepository.existsById(category.getId()));
    }
}

