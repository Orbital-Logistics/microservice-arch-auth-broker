package org.orbitalLogistic.cargo.services;

import lombok.RequiredArgsConstructor;
import org.orbitalLogistic.cargo.dto.common.PageResponseDTO;
import org.orbitalLogistic.cargo.dto.request.CargoStorageRequestDTO;
import org.orbitalLogistic.cargo.dto.response.CargoStorageResponseDTO;
import org.orbitalLogistic.cargo.dto.response.SpacecraftCargoUsageDTO;
import org.orbitalLogistic.cargo.entities.Cargo;
import org.orbitalLogistic.cargo.entities.CargoStorage;
import org.orbitalLogistic.cargo.entities.StorageUnit;
import org.orbitalLogistic.cargo.exceptions.CargoStorageNotFoundException;
import org.orbitalLogistic.cargo.exceptions.InsufficientCapacityException;
import org.orbitalLogistic.cargo.exceptions.StorageUnitNotFoundException;
import org.orbitalLogistic.cargo.mappers.CargoStorageMapper;
import org.orbitalLogistic.cargo.repositories.CargoRepository;
import org.orbitalLogistic.cargo.repositories.CargoStorageRepository;
import org.orbitalLogistic.cargo.repositories.StorageUnitRepository;
import org.orbitalLogistic.cargo.clients.ResilientUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CargoStorageService {

    private final CargoStorageRepository cargoStorageRepository;
    private final CargoStorageMapper cargoStorageMapper;
    private final CargoRepository cargoRepository;
    private final StorageUnitRepository storageUnitRepository;
    private final ResilientUserService userServiceClient;
    // private final UserServiceClient userServiceClient;

    public PageResponseDTO<CargoStorageResponseDTO> getAllCargoStorage(int page, int size) {
        int offset = page * size;
        List<CargoStorage> storages = cargoStorageRepository.findWithFilters(null, null, null, size, offset);
        long total = cargoStorageRepository.countWithFilters(null, null, null);

        List<CargoStorageResponseDTO> storageDTOs = storages.stream()
                .map(this::toResponseDTO)
                .toList();

        int totalPages = (int) Math.ceil((double) total / size);
        return new PageResponseDTO<>(storageDTOs, page, size, total, totalPages, page == 0, page >= totalPages - 1);
    }

    @Transactional
    public CargoStorageResponseDTO addCargoToStorage(CargoStorageRequestDTO request) {
        StorageUnit storageUnit = storageUnitRepository.findById(request.storageUnitId())
                .orElseThrow(() -> new StorageUnitNotFoundException("Storage unit not found"));
        Cargo cargo = cargoRepository.findById(request.cargoId())
                .orElseThrow(() -> new CargoStorageNotFoundException("Cargo not found"));
        
        userServiceClient.getUserById(request.updatedByUserId());

        BigDecimal requiredMass = cargo.getMassPerUnit().multiply(BigDecimal.valueOf(request.quantity()));
        BigDecimal requiredVolume = cargo.getVolumePerUnit().multiply(BigDecimal.valueOf(request.quantity()));

        BigDecimal availableMass = storageUnit.getTotalMassCapacity().subtract(storageUnit.getCurrentMass());
        BigDecimal availableVolume = storageUnit.getTotalVolumeCapacity().subtract(storageUnit.getCurrentVolume());

        if (requiredMass.compareTo(availableMass) > 0 || requiredVolume.compareTo(availableVolume) > 0) {
            throw new InsufficientCapacityException("Insufficient storage capacity");
        }

        CargoStorage cargoStorage = cargoStorageMapper.toEntity(request);
        CargoStorage saved = cargoStorageRepository.save(cargoStorage);

        storageUnit.setCurrentMass(storageUnit.getCurrentMass().add(requiredMass));
        storageUnit.setCurrentVolume(storageUnit.getCurrentVolume().add(requiredVolume));
        storageUnitRepository.save(storageUnit);

        return toResponseDTO(saved);
    }

    @Transactional
    public CargoStorageResponseDTO updateQuantity(Long id, CargoStorageRequestDTO request) {
        CargoStorage cargoStorage = cargoStorageRepository.findById(id)
                .orElseThrow(() -> new CargoStorageNotFoundException("Cargo storage not found with id: " + id));

        StorageUnit storageUnit = storageUnitRepository.findById(cargoStorage.getStorageUnitId())
                .orElseThrow(() -> new RuntimeException("Storage unit not found"));
        Cargo cargo = cargoRepository.findById(cargoStorage.getCargoId())
                .orElseThrow(() -> new RuntimeException("Cargo not found"));

        BigDecimal oldMass = cargo.getMassPerUnit().multiply(BigDecimal.valueOf(cargoStorage.getQuantity()));
        BigDecimal oldVolume = cargo.getVolumePerUnit().multiply(BigDecimal.valueOf(cargoStorage.getQuantity()));

        BigDecimal newMass = cargo.getMassPerUnit().multiply(BigDecimal.valueOf(request.quantity()));
        BigDecimal newVolume = cargo.getVolumePerUnit().multiply(BigDecimal.valueOf(request.quantity()));

        storageUnit.setCurrentMass(storageUnit.getCurrentMass().subtract(oldMass).add(newMass));
        storageUnit.setCurrentVolume(storageUnit.getCurrentVolume().subtract(oldVolume).add(newVolume));

        cargoStorage.setQuantity(request.quantity());
        CargoStorage updated = cargoStorageRepository.save(cargoStorage);
        storageUnitRepository.save(storageUnit);

        return toResponseDTO(updated);
    }

    public PageResponseDTO<CargoStorageResponseDTO> getStorageUnitCargo(Long storageUnitId, int page, int size) {
        int offset = page * size;
        List<CargoStorage> storages = cargoStorageRepository.findWithFilters(storageUnitId, null, null, size, offset);
        long total = cargoStorageRepository.countWithFilters(storageUnitId, null, null);

        List<CargoStorageResponseDTO> storageDTOs = storages.stream()
                .map(this::toResponseDTO)
                .toList();

        int totalPages = (int) Math.ceil((double) total / size);
        return new PageResponseDTO<>(storageDTOs, page, size, total, totalPages, page == 0, page >= totalPages - 1);
    }

    public Integer calculateTotalQuantityForCargo(Long cargoId) {
        Integer total = cargoStorageRepository.getTotalQuantityByCargoId(cargoId);
        return total != null ? total : 0;
    }

    public SpacecraftCargoUsageDTO getSpacecraftCargoUsage(Long spacecraftId) {
        List<StorageUnit> storageUnits = storageUnitRepository.findBySpacecraftId(spacecraftId);

        BigDecimal totalMassUsage = storageUnits.stream()
                .map(StorageUnit::getCurrentMass)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalVolumeUsage = storageUnits.stream()
                .map(StorageUnit::getCurrentVolume)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new SpacecraftCargoUsageDTO(spacecraftId, totalMassUsage, totalVolumeUsage);
    }

    private CargoStorageResponseDTO toResponseDTO(CargoStorage cargoStorage) {
        StorageUnit storageUnit = storageUnitRepository.findById(cargoStorage.getStorageUnitId()).orElse(null);
        Cargo cargo = cargoRepository.findById(cargoStorage.getCargoId()).orElse(null);

        String storageUnitCode = storageUnit != null ? storageUnit.getUnitCode() : "Unknown";
        String storageLocation = storageUnit != null ? storageUnit.getLocation() : "Unknown";
        String cargoName = cargo != null ? cargo.getName() : "Unknown";

        String lastCheckedByUserName = null;
        if (cargoStorage.getLastCheckedByUserId() != null) {
            lastCheckedByUserName = userServiceClient.getUserById(cargoStorage.getLastCheckedByUserId());
        }

        return cargoStorageMapper.toResponseDTO(
                cargoStorage,
                storageUnitCode,
                storageLocation,
                cargoName,
                lastCheckedByUserName
        );
    }
}
