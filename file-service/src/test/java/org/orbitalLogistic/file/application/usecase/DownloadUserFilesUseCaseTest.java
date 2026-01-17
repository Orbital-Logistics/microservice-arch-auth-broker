package org.orbitalLogistic.file.application.usecase;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.orbitalLogistic.file.application.model.FileCategory;
import org.orbitalLogistic.file.application.ports.StorageOperations;
import org.orbitalLogistic.file.application.ports.dto.FileDto;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DownloadUserFilesUseCaseTest {

    @Mock
    private StorageOperations storageOperations;

    @InjectMocks
    private DownloadUserFilesUseCase downloadUserFilesUseCase;

    @Test
    void execute_Success() {
        // Arrange
        Long userId = 123L;
        String fileFormat = "reports/%d/%s.pdf";
        String missionCode = "test";

        String expectedFilename = "reports/123/test.pdf";
        InputStream mockStream = new ByteArrayInputStream("test data".getBytes());
        FileDto expectedFileDto = new FileDto("report.pdf", mockStream);

        when(storageOperations.download(FileCategory.USER, expectedFilename)).thenReturn(expectedFileDto);

        // Act
        FileDto result = downloadUserFilesUseCase.execute(userId, fileFormat, missionCode);

        // Assert
        assertNotNull(result);
        assertEquals("report.pdf", result.filename());
        assertNotNull(result.inputStream());
        verify(storageOperations).download(FileCategory.USER, expectedFilename);
    }

}