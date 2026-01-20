package org.orbitalLogistic.file.application.services;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.nio.file.Paths;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FileFormat {

    /**
     * Get parent directory
     * @param path - path to specified directory with filename
     * @return - parent directory
     */
    public static String getParent(String path) {
        if (path.equals("/"))
            return "";
        java.nio.file.Path parent = Paths.get(path).getParent();
        if (parent == null) {
            return "";
        }
        return parent.toString().replace("\\", "/") + "/";
    }
}
