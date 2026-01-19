package org.orbitalLogistic.file.application.usecase;

import lombok.RequiredArgsConstructor;
import org.orbitalLogistic.file.application.model.FileCategory;
import org.orbitalLogistic.file.application.ports.StorageOperations;
import org.orbitalLogistic.file.application.services.FileFormat;

import java.util.List;

@RequiredArgsConstructor
public class GetReportsUserUseCase {

    private final StorageOperations storageOperations;

    public List<String> execute(String reportsFormat, Long id) {
        String path = String.format(FileFormat.getParent(reportsFormat), id);
        return storageOperations.getListDir(FileCategory.USER, path);
    }

}
