package org.orbitalLogistic.file.domain.exceptions;

public class FileNotFoundException extends RuntimeException {
    public FileNotFoundException(String path) {
        super("File not found: " + path);
    }
}
