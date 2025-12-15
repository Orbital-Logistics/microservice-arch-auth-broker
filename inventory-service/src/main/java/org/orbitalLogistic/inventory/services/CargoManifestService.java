package org.orbitalLogistic.inventory.services;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.orbitalLogistic.inventory.clients.*;
import org.orbitalLogistic.inventory.clients.resilient.ResilientCargoServiceClient;
import org.orbitalLogistic.inventory.clients.resilient.ResilientSpacecraftService;
import org.orbitalLogistic.inventory.clients.resilient.ResilientUserService;
import org.orbitalLogistic.inventory.dto.common.PageResponseDTO;
import org.orbitalLogistic.inventory.dto.request.CargoManifestRequestDTO;
import org.orbitalLogistic.inventory.dto.response.CargoManifestResponseDTO;
import org.orbitalLogistic.inventory.entities.CargoManifest;
import org.orbitalLogistic.inventory.exceptions.CargoManifestNotFoundException;
import org.orbitalLogistic.inventory.exceptions.InvalidOperationException;
import org.orbitalLogistic.inventory.mappers.CargoManifestMapper;
import org.orbitalLogistic.inventory.repositories.CargoManifestRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CargoManifestService {

    private final CargoManifestRepository cargoManifestRepository;
    private final CargoManifestMapper cargoManifestMapper;
    private final ResilientUserService userServiceClient;
    private final ResilientCargoServiceClient cargoServiceClient;
    private final ResilientSpacecraftService spacecraftServiceClient;

    public PageResponseDTO<CargoManifestResponseDTO> getAllManifests(int page, int size) {
        int offset = page * size;
        List<CargoManifest> manifests = cargoManifestRepository.findAllPaginated(size, offset);
        long total = cargoManifestRepository.countAll();

        List<CargoManifestResponseDTO> manifestDTOs = manifests.stream()
                .map(this::toResponseDTO)
                .toList();

        int totalPages = (int) Math.ceil((double) total / size);
        return new PageResponseDTO<>(manifestDTOs, page, size, total, totalPages, page == 0, page >= totalPages - 1);
    }

    public PageResponseDTO<CargoManifestResponseDTO> getManifestsBySpacecraft(Long spacecraftId, int page, int size) {
        int offset = page * size;
        List<CargoManifest> manifests = cargoManifestRepository.findBySpacecraftIdPaginated(spacecraftId, size, offset);
        long total = cargoManifestRepository.countBySpacecraftId(spacecraftId);

        List<CargoManifestResponseDTO> manifestDTOs = manifests.stream()
                .map(this::toResponseDTO)
                .toList();

        int totalPages = (int) Math.ceil((double) total / size);
        return new PageResponseDTO<>(manifestDTOs, page, size, total, totalPages, page == 0, page >= totalPages - 1);
    }

    public CargoManifestResponseDTO getManifestById(Long id) {
        CargoManifest manifest = cargoManifestRepository.findById(id)
                .orElseThrow(() -> new CargoManifestNotFoundException("Cargo manifest not found with id: " + id));
        return toResponseDTO(manifest);
    }

    public CargoManifestResponseDTO createManifest(CargoManifestRequestDTO request) {
        validateEntity(spacecraftServiceClient.spacecraftExists(request.spacecraftId()),
                "Spacecraft", request.spacecraftId(), "Spacecraft service");

        validateEntity(cargoServiceClient.cargoExists(request.cargoId()),
                "Cargo", request.cargoId(), "Cargo service");

        validateEntity(cargoServiceClient.storageUnitExists(request.storageUnitId()),
                "Storage unit", request.storageUnitId(), "Cargo service");

        validateEntity(userServiceClient.userExists(request.loadedByUserId()),
                "Loaded by user", request.loadedByUserId(), "User service");

        if (request.unloadedByUserId() != null) {
            validateEntity(userServiceClient.userExists(request.unloadedByUserId()),
                    "Unloaded by user", request.unloadedByUserId(), "User service");
        }

        CargoManifest manifest = cargoManifestMapper.toEntity(request);

        if (manifest.getManifestStatus() == null) {
            manifest.setManifestStatus(org.orbitalLogistic.inventory.entities.enums.ManifestStatus.PENDING);
        }
        if (manifest.getPriority() == null) {
            manifest.setPriority(org.orbitalLogistic.inventory.entities.enums.ManifestPriority.NORMAL);
        }

        CargoManifest saved = cargoManifestRepository.save(manifest);
        return toResponseDTO(saved);
    }

    public CargoManifestResponseDTO updateManifest(Long id, CargoManifestRequestDTO request) {
        CargoManifest manifest = cargoManifestRepository.findById(id)
                .orElseThrow(() -> new CargoManifestNotFoundException("Cargo manifest not found with id: " + id));

        if (request.manifestStatus() != null) {
            manifest.setManifestStatus(request.manifestStatus());
        }
        if (request.priority() != null) {
            manifest.setPriority(request.priority());
        }
        if (request.unloadedAt() != null) {
            manifest.setUnloadedAt(request.unloadedAt());
        }
        if (request.unloadedByUserId() != null) {
            validateEntity(userServiceClient.userExists(request.unloadedByUserId()),
                    "Unloaded by user", request.unloadedByUserId(), "User service");
            manifest.setUnloadedByUserId(request.unloadedByUserId());
        }

        CargoManifest updated = cargoManifestRepository.save(manifest);
        return toResponseDTO(updated);
    }

    private void validateEntity(Boolean exists, String entityName, Long id, String serviceName) {
        try {
            if (exists == null || !exists) {
                throw new InvalidOperationException(entityName + " not found with id: " + id);
            }
        } catch (Exception e) {
            log.error("Failed to validate {}: {}", entityName, e.getMessage());
            throw new InvalidOperationException("Unable to validate " + entityName.toLowerCase() + ". " + serviceName + " may be unavailable.");
        }
    }

    private CargoManifestResponseDTO toResponseDTO(CargoManifest manifest) {
        String spacecraftName = "Unknown";
        String cargoName = "Unknown";
        String storageUnitCode = "Unknown";
        String loadedByUserName = "Unknown";
        String unloadedByUserName = null;

        try {
            SpacecraftDTO spacecraft = spacecraftServiceClient.getSpacecraftById(manifest.getSpacecraftId());
            if (spacecraft != null) spacecraftName = spacecraft.name();
        } catch (Exception e) {
            log.warn("Failed to fetch spacecraft name: {}", e.getMessage());
        }

        try {
            CargoDTO cargo = cargoServiceClient.getCargoById(manifest.getCargoId());
            if (cargo != null) cargoName = cargo.name();
        } catch (Exception e) {
            log.warn("Failed to fetch cargo name: {}", e.getMessage());
        }

        try {
            StorageUnitDTO storageUnit = cargoServiceClient.getStorageUnitById(manifest.getStorageUnitId());
            if (storageUnit != null) storageUnitCode = storageUnit.unitCode();
        } catch (Exception e) {
            log.warn("Failed to fetch storage unit code: {}", e.getMessage());
        }

        try {
            UserDTO user = userServiceClient.getUserById(manifest.getLoadedByUserId());
            if (user != null) loadedByUserName = user.username();
        } catch (Exception e) {
            log.warn("Failed to fetch loaded by user name: {}", e.getMessage());
        }

        if (manifest.getUnloadedByUserId() != null) {
            try {
                UserDTO user = userServiceClient.getUserById(manifest.getUnloadedByUserId());
                if (user != null) unloadedByUserName = user.username();
            } catch (Exception e) {
                log.warn("Failed to fetch unloaded by user name: {}", e.getMessage());
            }
        }

        return cargoManifestMapper.toResponseDTO(
                manifest,
                spacecraftName,
                cargoName,
                storageUnitCode,
                loadedByUserName,
                unloadedByUserName
        );
    }
}

