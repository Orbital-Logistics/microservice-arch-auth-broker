package org.orbitalLogistic.inventory.application.usecase;

import lombok.RequiredArgsConstructor;
import org.orbitalLogistic.inventory.application.model.FileCategory;
import org.orbitalLogistic.inventory.application.ports.StorageOperations;
import org.orbitalLogistic.inventory.application.services.FileFormat;

import java.util.List;

@RequiredArgsConstructor
public class GetListUserUseCase {

    private final StorageOperations storageOperations;

    public List<String> execute(String reportsFormat, Long id) {
        String path = String.format(FileFormat.getParent(reportsFormat), id);
        return storageOperations.getListDir(FileCategory.USER, path);
    }

}
