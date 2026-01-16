package org.orbitalLogistic.inventory.application.usecase;

import lombok.RequiredArgsConstructor;
import org.orbitalLogistic.inventory.application.model.FileCategory;
import org.orbitalLogistic.inventory.application.ports.StorageOperations;
import org.orbitalLogistic.inventory.application.services.FileFormat;
import org.orbitalLogistic.inventory.application.ports.dto.LocalTimeDto;
import org.orbitalLogistic.inventory.application.ports.dto.FileDto;

@RequiredArgsConstructor
public class DownloadUserFilesUseCase {

    private final StorageOperations storageOperations;

    public FileDto execute(Long id, String fileFormat, String dateFormat, LocalTimeDto localTimeDto) {
        String filename = String.format(fileFormat, id,
                FileFormat.getStringDateFormat(localTimeDto, dateFormat));
        return storageOperations.download(FileCategory.USER, filename);
    }

}
