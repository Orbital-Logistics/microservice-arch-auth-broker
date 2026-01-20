package org.orbitalLogistic.file.minio;

import io.minio.*;
import io.minio.errors.ErrorResponseException;
import io.minio.messages.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.orbitalLogistic.file.adapters.minio.MinioOperations;
import org.orbitalLogistic.file.application.model.FileCategory;
import org.orbitalLogistic.file.application.ports.dto.FileDto;
import org.orbitalLogistic.file.domain.exceptions.FileNotFoundException;
import org.orbitalLogistic.file.domain.exceptions.StorageTechnicalException;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MinioOperationsTest {

    @Mock
    private MinioClient minioClient;

    @Mock
    private ObjectWriteResponse objectWriteResponse;

    @Mock
    private GetObjectResponse getObjectResponse;

    private MinioOperations minioOperations;

    @BeforeEach
    void setUp() {
        minioOperations = new MinioOperations(minioClient);
        ReflectionTestUtils.setField(minioOperations, "userFilesBucket", "user-files");
        ReflectionTestUtils.setField(minioOperations, "defaultBucket", "default");
    }

    @Test
    void upload_Success() throws Exception {
        
        FileCategory category = FileCategory.USER;
        String path = "test/file.txt";
        InputStream inputStream = new ByteArrayInputStream("data".getBytes());
        long size = 100L;
        String contentType = "text/plain";

        when(minioClient.putObject(any(PutObjectArgs.class)))
                .thenReturn(objectWriteResponse);

        
        assertDoesNotThrow(() ->
                minioOperations.upload(category, path, inputStream, size, contentType)
        );

        verify(minioClient).putObject(any(PutObjectArgs.class));
    }

    @Test
    void upload_DefaultBucket_Success() throws Exception {
        
        FileCategory category = FileCategory.DEFAULT;
        String path = "default/file.pdf";
        InputStream inputStream = new ByteArrayInputStream("pdf".getBytes());
        long size = 50L;
        String contentType = "application/pdf";

        when(minioClient.putObject(any(PutObjectArgs.class)))
                .thenReturn(objectWriteResponse);

        
        minioOperations.upload(category, path, inputStream, size, contentType);

        
        verify(minioClient).putObject(any(PutObjectArgs.class));
    }

    @Test
    void upload_ThrowsMinioException_OnError() throws Exception {
        
        FileCategory category = FileCategory.USER;
        String path = "error/file.txt";
        InputStream inputStream = new ByteArrayInputStream("data".getBytes());

        when(minioClient.putObject(any(PutObjectArgs.class)))
                .thenThrow(new RuntimeException("Upload failed"));

        
        assertThrows(Exception.class,
                () -> minioOperations.upload(category, path, inputStream, 50L, "text/plain"));
    }

    @Test
    void download_Success() throws Exception {
        
        FileCategory category = FileCategory.USER;
        String path = "downloads/file.pdf";

        when(minioClient.getObject(any(GetObjectArgs.class)))
                .thenReturn(getObjectResponse);

        
        FileDto result = minioOperations.download(category, path);

        
        assertNotNull(result);
        assertEquals("file.pdf", result.filename());
        assertNotNull(result.inputStream());
        verify(minioClient).getObject(any(GetObjectArgs.class));
    }

    @Test
    void download_FileNotFound_ThrowsFileNotFoundException() throws Exception {
        
        FileCategory category = FileCategory.USER;
        String path = "missing/file.txt";

        ErrorResponse errorResponse = new ErrorResponse(
                "NoSuchKey",
                "The specified key does not exist",
                "user-files",
                path,
                "",
                "",
                ""
        );
        ErrorResponseException exception = new ErrorResponseException(
                errorResponse,
                null,
                "test"
        );

        when(minioClient.getObject(any(GetObjectArgs.class)))
                .thenThrow(exception);

        
        FileNotFoundException thrown = assertThrows(FileNotFoundException.class,
                () -> minioOperations.download(category, path));

        assertTrue(thrown.getMessage().contains(path));
    }

    @Test
    void download_TechnicalError_ThrowsStorageTechnicalException() throws Exception {
        
        FileCategory category = FileCategory.DEFAULT;
        String path = "error/file.txt";

        when(minioClient.getObject(any(GetObjectArgs.class)))
                .thenThrow(new RuntimeException("Connection error"));

        
        assertThrows(StorageTechnicalException.class,
                () -> minioOperations.download(category, path));
    }

    @Test
    void getListDir_EmptyDirectory_ReturnsEmptyList() {
        
        FileCategory category = FileCategory.USER;
        String path = "empty/";

        when(minioClient.listObjects(any(ListObjectsArgs.class)))
                .thenReturn(new ArrayList<>());

        
        var result = minioOperations.getListDir(category, path);

        
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void remove_Success() throws Exception {
        
        FileCategory category = FileCategory.USER;
        String path = "temp/file.jpg";

        doNothing().when(minioClient).removeObject(any(RemoveObjectArgs.class));

        
        assertDoesNotThrow(() -> minioOperations.remove(category, path));
        verify(minioClient).removeObject(any(RemoveObjectArgs.class));
    }

    @Test
    void remove_FileNotFound_ThrowsFileNotFoundException() throws Exception {
        
        FileCategory category = FileCategory.USER;
        String path = "missing/file.txt";

        ErrorResponse errorResponse = new ErrorResponse(
                "NoSuchKey",
                "The specified key does not exist",
                "user-files",
                path,
                "",
                "",
                ""
        );
        ErrorResponseException exception = new ErrorResponseException(
                errorResponse,
                null,
                "test"
        );

        doThrow(exception).when(minioClient).removeObject(any(RemoveObjectArgs.class));

        
        assertThrows(FileNotFoundException.class,
                () -> minioOperations.remove(category, path));
    }

    @Test
    void remove_TechnicalError_ThrowsStorageTechnicalException() throws Exception {
        
        FileCategory category = FileCategory.USER;
        String path = "error/file.txt";

        doThrow(new RuntimeException("Connection error"))
                .when(minioClient).removeObject(any(RemoveObjectArgs.class));

        
        assertThrows(StorageTechnicalException.class,
                () -> minioOperations.remove(category, path));
    }
}
