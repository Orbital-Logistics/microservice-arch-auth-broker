package org.orbitalLogistic.file.application.usecase;

import lombok.RequiredArgsConstructor;
import org.orbitalLogistic.file.application.model.FileCategory;
import org.orbitalLogistic.file.application.ports.StorageOperations;
import org.orbitalLogistic.file.application.ports.dto.FileDto;

@RequiredArgsConstructor
public class DownloadUserFilesUseCase {

    private final StorageOperations storageOperations;

    public FileDto execute(Long id, String fileFormat, String missionCode) {
        String path = String.format(fileFormat, id, missionCode);
        return storageOperations.download(FileCategory.USER, path);
    }

}
