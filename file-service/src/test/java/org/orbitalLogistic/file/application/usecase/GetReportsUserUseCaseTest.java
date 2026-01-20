package org.orbitalLogistic.file.application.usecase;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.orbitalLogistic.file.application.model.FileCategory;
import org.orbitalLogistic.file.application.ports.StorageOperations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetReportsUserUseCaseTest {

    @Mock
    private StorageOperations storageOperations;

    @InjectMocks
    private GetReportsUserUseCase getReportsUserUseCase;

    @Test
    void execute_Success() {
        String parent = "reports/123/";
        Long userId = 123L;
        String reportsFormat = "reports/%d/%s.pdf";
        List<String> expectedFiles = List.of("test1.pdf", "test2.pdf");

        when(storageOperations.getListDir(FileCategory.USER, parent)).thenReturn(expectedFiles);

        
        List<String> result = getReportsUserUseCase.execute(reportsFormat, userId);

        
        assertEquals(2, result.size());
        assertEquals("test1.pdf", result.get(0));
        assertEquals("test2.pdf", result.get(1));
        verify(storageOperations).getListDir(FileCategory.USER, parent);
    }

    @Test
    void execute_EmptyList() {
        String parent = "reports/123/";
        Long userId = 123L;
        String reportsFormat = "reports/%d/%s.pdf";

        when(storageOperations.getListDir(FileCategory.USER, parent)).thenReturn(List.of());

        
        List<String> result = getReportsUserUseCase.execute(reportsFormat, userId);

        
        assertTrue(result.isEmpty());
        verify(storageOperations).getListDir(FileCategory.USER, parent);
    }
}
