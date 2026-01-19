package org.orbitalLogistic.file.application.usecases;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.orbitalLogistic.file.application.ports.in.GetCargoManifestsUseCase;
import org.orbitalLogistic.file.application.ports.out.CargoManifestRepository;
import org.orbitalLogistic.file.domain.model.CargoManifest;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetCargoManifestsService implements GetCargoManifestsUseCase {

    private final CargoManifestRepository cargoManifestRepository;

    @Override
    public List<CargoManifest> getAllManifests(int page, int size) {
        log.debug("Getting all cargo manifests, page: {}, size: {}", page, size);
        int offset = page * size;
        return cargoManifestRepository.findAll(size, offset);
    }

    @Override
    public List<CargoManifest> getManifestsBySpacecraft(Long spacecraftId, int page, int size) {
        log.debug("Getting cargo manifests for spacecraft: {}, page: {}, size: {}", spacecraftId, page, size);
        int offset = page * size;
        return cargoManifestRepository.findBySpacecraftId(spacecraftId, size, offset);
    }

    @Override
    public CargoManifest getManifestById(Long id) {
        log.debug("Getting cargo manifest by id: {}", id);
        return cargoManifestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cargo manifest not found with id: " + id));
    }

    @Override
    public long countAllManifests() {
        return cargoManifestRepository.countAll();
    }

    @Override
    public long countManifestsBySpacecraft(Long spacecraftId) {
        return cargoManifestRepository.countBySpacecraftId(spacecraftId);
    }
}
