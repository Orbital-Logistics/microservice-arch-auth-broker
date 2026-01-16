package org.orbitalLogistic.inventory.domain.exceptions;

public class FileNotFoundException extends RuntimeException {
    public FileNotFoundException(String path) {
        super("File not found: " + path);
    }
}
