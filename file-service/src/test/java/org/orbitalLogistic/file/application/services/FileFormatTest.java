package org.orbitalLogistic.file.application.services;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FileFormatTest {

    @Test
    void getParent_NormalPath_ReturnsParent() {
        String path = "users/123/reports/report.pdf";

        
        String result = FileFormat.getParent(path);

        
        assertEquals("users/123/reports/", result);
    }

    @Test
    void getParent_NestedPath_ReturnsParent() {
        String path = "a/b/c/d/file.txt";

        
        String result = FileFormat.getParent(path);

        
        assertEquals("a/b/c/d/", result);
    }

    @Test
    void getParent_RootPath_ReturnsEmpty() {
        String path = "/";

        
        String result = FileFormat.getParent(path);

        
        assertEquals("", result);
    }

    @Test
    void getParent_DeepNestedPath_ReturnsParent() {
        String path = "users/123/photos/2024/01/image.jpg";

        
        String result = FileFormat.getParent(path);

        
        assertEquals("users/123/photos/2024/01/", result);
    }

    @Test
    void getParent_TwoLevelPath_ReturnsParent() {
        String path = "folder/file.txt";

        
        String result = FileFormat.getParent(path);

        
        assertEquals("folder/", result);
    }
}
