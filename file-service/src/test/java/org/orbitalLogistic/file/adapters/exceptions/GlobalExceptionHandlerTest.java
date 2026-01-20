package org.orbitalLogistic.file.adapters.exceptions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.orbitalLogistic.file.adapters.exceptions.dto.ErrorResponseDTO;
import org.orbitalLogistic.file.domain.exceptions.FileNotFoundException;
import org.orbitalLogistic.file.domain.exceptions.FileTypeException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler exceptionHandler;

    @Test
    void shouldHandleHttpMessageNotReadableException() {
        HttpMessageNotReadableException exception = mock(HttpMessageNotReadableException.class);
        when(exception.getMessage()).thenReturn("Some error");

        ResponseEntity<ErrorResponseDTO> response = exceptionHandler.handleJsonParseException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Invalid JSON format", response.getBody().message());
        assertNotNull(response.getBody().timestamp());
        assertNull(response.getBody().details());
    }

    @Test
    void shouldHandleHttpMessageNotReadableExceptionWithJsonParseError() {
        HttpMessageNotReadableException exception = mock(HttpMessageNotReadableException.class);
        when(exception.getMessage()).thenReturn("JSON parse error: Invalid format");

        ResponseEntity<ErrorResponseDTO> response = exceptionHandler.handleJsonParseException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Invalid JSON: check field types and format", response.getBody().message());
        assertNotNull(response.getBody().timestamp());
    }

    @Test
    void shouldHandleMethodArgumentNotValidExceptionWithSingleError() {
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("object", "username", "must not be blank");
        
        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<ErrorResponseDTO> response = exceptionHandler.handleValidationException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().message().contains("username"));
        assertTrue(response.getBody().message().contains("must not be blank"));
        assertNotNull(response.getBody().timestamp());
    }

    @Test
    void shouldHandleMethodArgumentNotValidExceptionWithMultipleErrors() {
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        
        List<FieldError> fieldErrors = Arrays.asList(
                new FieldError("object", "username", "must not be blank"),
                new FieldError("object", "email", "must be a valid email"),
                new FieldError("object", "age", "must be greater than 0")
        );
        
        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(fieldErrors);

        ResponseEntity<ErrorResponseDTO> response = exceptionHandler.handleValidationException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        String message = response.getBody().message();
        assertTrue(message.contains("username"));
        assertTrue(message.contains("email"));
        assertTrue(message.contains("age"));
        assertNotNull(response.getBody().timestamp());
    }

    @Test
    void shouldHandleFileNotFoundException() {
        String filePath = "reports/test-report.pdf";
        FileNotFoundException exception = new FileNotFoundException(filePath);

        ResponseEntity<ErrorResponseDTO> response = exceptionHandler.minioException(exception);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().message().contains("File not found"));
        assertTrue(response.getBody().message().contains(filePath));
        assertNotNull(response.getBody().timestamp());
        assertNull(response.getBody().details());
    }

    @Test
    void shouldHandleFileNotFoundExceptionWithVariousPaths() {
        String[] paths = {
                "reports/mission-report.pdf",
                "uploads/document.txt",
                "files/image.png",
                "user/data/config.json"
        };

        for (String path : paths) {
            FileNotFoundException exception = new FileNotFoundException(path);

            ResponseEntity<ErrorResponseDTO> response = exceptionHandler.minioException(exception);

            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
            assertTrue(response.getBody().message().contains(path));
        }
    }

    @Test
    void shouldHandleFileTypeException() {
        String expected = "PDF";
        String actual = "TXT";
        FileTypeException exception = new FileTypeException(expected, actual);

        ResponseEntity<ErrorResponseDTO> response = exceptionHandler.fileTypeException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().message().contains("Wrong file type"));
        assertTrue(response.getBody().message().contains("Expected: " + expected));
        assertTrue(response.getBody().message().contains("actual: " + actual));
        assertNotNull(response.getBody().timestamp());
        assertNull(response.getBody().details());
    }

    @Test
    void shouldHandleFileTypeExceptionWithVariousTypes() {
        String[][] fileTypes = {
                {"PDF", "DOCX"},
                {"IMAGE", "VIDEO"},
                {"JSON", "XML"},
                {"TEXT", "BINARY"}
        };

        for (String[] types : fileTypes) {
            FileTypeException exception = new FileTypeException(types[0], types[1]);

            ResponseEntity<ErrorResponseDTO> response = exceptionHandler.fileTypeException(exception);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertTrue(response.getBody().message().contains(types[0]));
            assertTrue(response.getBody().message().contains(types[1]));
        }
    }

    @Test
    void shouldReturnNotFoundStatusForFileNotFoundException() {
        FileNotFoundException exception = new FileNotFoundException("test.pdf");

        ResponseEntity<ErrorResponseDTO> response = exceptionHandler.minioException(exception);

        assertEquals(404, response.getStatusCode().value());
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void shouldReturnBadRequestStatusForFileTypeException() {
        FileTypeException exception = new FileTypeException("PDF", "TXT");

        ResponseEntity<ErrorResponseDTO> response = exceptionHandler.fileTypeException(exception);

        assertEquals(400, response.getStatusCode().value());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void shouldSetTimestampInErrorResponse() {
        FileNotFoundException fileNotFound = new FileNotFoundException("test.pdf");
        FileTypeException fileType = new FileTypeException("PDF", "TXT");
        HttpMessageNotReadableException jsonError = mock(HttpMessageNotReadableException.class);
        when(jsonError.getMessage()).thenReturn("error");

        ResponseEntity<ErrorResponseDTO> response1 = exceptionHandler.minioException(fileNotFound);
        ResponseEntity<ErrorResponseDTO> response2 = exceptionHandler.fileTypeException(fileType);
        ResponseEntity<ErrorResponseDTO> response3 = exceptionHandler.handleJsonParseException(jsonError);

        assertNotNull(response1.getBody().timestamp());
        assertNotNull(response2.getBody().timestamp());
        assertNotNull(response3.getBody().timestamp());
    }

    @Test
    void shouldHandleValidationExceptionWithEmptyErrors() {
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        
        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of());

        ResponseEntity<ErrorResponseDTO> response = exceptionHandler.handleValidationException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("", response.getBody().message());
    }

    @Test
    void shouldFormatMultipleFieldErrorsWithCommaSeparator() {
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        
        List<FieldError> fieldErrors = Arrays.asList(
                new FieldError("object", "field1", "error1"),
                new FieldError("object", "field2", "error2")
        );
        
        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(fieldErrors);

        ResponseEntity<ErrorResponseDTO> response = exceptionHandler.handleValidationException(exception);

        String message = response.getBody().message();
        assertTrue(message.contains(","));
        assertTrue(message.contains("field1: error1"));
        assertTrue(message.contains("field2: error2"));
    }

    @Test
    void shouldIncludeWrongFileTypePrefixInResponse() {
        FileTypeException exception = new FileTypeException("PDF", "TXT");

        ResponseEntity<ErrorResponseDTO> response = exceptionHandler.fileTypeException(exception);

        assertTrue(response.getBody().message().startsWith("Wrong file type"));
    }

    @Test
    void shouldSetDetailsToNullInErrorResponses() {
        FileNotFoundException fileNotFound = new FileNotFoundException("test.pdf");
        FileTypeException fileType = new FileTypeException("PDF", "TXT");
        HttpMessageNotReadableException jsonError = mock(HttpMessageNotReadableException.class);
        when(jsonError.getMessage()).thenReturn("error");

        ResponseEntity<ErrorResponseDTO> response1 = exceptionHandler.minioException(fileNotFound);
        ResponseEntity<ErrorResponseDTO> response2 = exceptionHandler.fileTypeException(fileType);
        ResponseEntity<ErrorResponseDTO> response3 = exceptionHandler.handleJsonParseException(jsonError);

        assertNull(response1.getBody().details());
        assertNull(response2.getBody().details());
        assertNull(response3.getBody().details());
    }
}

