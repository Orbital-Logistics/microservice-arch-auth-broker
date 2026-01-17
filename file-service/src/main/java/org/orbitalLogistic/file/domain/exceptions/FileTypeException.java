package org.orbitalLogistic.file.domain.exceptions;

public class FileTypeException extends RuntimeException {
    public FileTypeException(String expected, String actual) {
        super("Expected: " + expected + ", actual: " + actual);
    }
}
