package org.orbitalLogistic.file.adapters.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.orbitalLogistic.file.adapters.exceptions.dto.ErrorResponseDTO;
import org.orbitalLogistic.file.domain.exceptions.FileNotFoundException;
import org.orbitalLogistic.file.domain.exceptions.FileTypeException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDTO> handleJsonParseException(HttpMessageNotReadableException ex) {
        String message = "Invalid JSON format";
        if (ex.getMessage() != null && ex.getMessage().contains("JSON parse error")) {
            message = "Invalid JSON: check field types and format";
        }

        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                message,
                LocalDateTime.now(),
                null
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidationException(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                errorMessage,
                LocalDateTime.now(),
                null
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(FileNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> minioException(
            FileNotFoundException ex
    ) {
        log.error(ex.getMessage());
        ErrorResponseDTO error = new ErrorResponseDTO(
                ex.getMessage(),
                LocalDateTime.now(),
                null
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(FileTypeException.class)
    public ResponseEntity<ErrorResponseDTO> fileTypeException(
            FileTypeException ex
    ) {
        log.error(ex.getMessage());
        ErrorResponseDTO error = new ErrorResponseDTO(
                "Wrong file type. " + ex.getMessage(),
                LocalDateTime.now(),
                null
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDTO> handleIllegalArgumentException(
            IllegalArgumentException ex
    ) {
        log.error(ex.getMessage());
        ErrorResponseDTO error = new ErrorResponseDTO(
                ex.getMessage(),
                LocalDateTime.now(),
                null
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}
