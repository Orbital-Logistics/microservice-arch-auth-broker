package org.orbitalLogistic.file.application.services;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FileFormatTest {

    @Test
    void getParent_NormalPath_ReturnsParent() {
        // Arrange
        String path = "users/123/reports/report.pdf";

        // Act
        String result = FileFormat.getParent(path);

        // Assert
        assertEquals("users/123/reports/", result);
    }

    @Test
    void getParent_NestedPath_ReturnsParent() {
        // Arrange
        String path = "a/b/c/d/file.txt";

        // Act
        String result = FileFormat.getParent(path);

        // Assert
        assertEquals("a/b/c/d/", result);
    }

    @Test
    void getParent_RootPath_ReturnsEmpty() {
        // Arrange
        String path = "/";

        // Act
        String result = FileFormat.getParent(path);

        // Assert
        assertEquals("", result);
    }

    @Test
    void getParent_DeepNestedPath_ReturnsParent() {
        // Arrange
        String path = "users/123/photos/2024/01/image.jpg";

        // Act
        String result = FileFormat.getParent(path);

        // Assert
        assertEquals("users/123/photos/2024/01/", result);
    }

    @Test
    void getParent_TwoLevelPath_ReturnsParent() {
        // Arrange
        String path = "folder/file.txt";

        // Act
        String result = FileFormat.getParent(path);

        // Assert
        assertEquals("folder/", result);
    }
}