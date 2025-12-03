package org.orbitalLogistic.cargo.services;

import lombok.RequiredArgsConstructor;
import org.orbitalLogistic.cargo.dto.common.PageResponseDTO;
import org.orbitalLogistic.cargo.dto.request.StorageUnitRequestDTO;
import org.orbitalLogistic.cargo.dto.response.CargoStorageResponseDTO;
import org.orbitalLogistic.cargo.dto.response.StorageUnitResponseDTO;
import org.orbitalLogistic.cargo.entities.StorageUnit;
import org.orbitalLogistic.cargo.exceptions.StorageUnitAlreadyExistsException;
import org.orbitalLogistic.cargo.exceptions.StorageUnitNotFoundException;
import org.orbitalLogistic.cargo.mappers.StorageUnitMapper;
import org.orbitalLogistic.cargo.repositories.CargoStorageRepository;
import org.orbitalLogistic.cargo.repositories.StorageUnitRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StorageUnitService {

    private final StorageUnitRepository storageUnitRepository;
    private final StorageUnitMapper storageUnitMapper;
    private final CargoStorageRepository cargoStorageRepository;
    private final CargoStorageService cargoStorageService;

    public PageResponseDTO<StorageUnitResponseDTO> getStorageUnits(int page, int size) {
        int offset = page * size;
        List<StorageUnit> units = storageUnitRepository.findAllPaged(size, offset);
        long total = storageUnitRepository.countAll();

        List<StorageUnitResponseDTO> unitDTOs = units.stream()
                .map(this::toResponseDTO)
                .toList();

        int totalPages = (int) Math.ceil((double) total / size);
        return new PageResponseDTO<>(unitDTOs, page, size, total, totalPages, page == 0, page >= totalPages - 1);
    }

    public StorageUnitResponseDTO getStorageUnitById(Long id) {
        StorageUnit unit = storageUnitRepository.findById(id)
                .orElseThrow(() -> new StorageUnitNotFoundException("Storage unit not found with id: " + id));
        return toResponseDTO(unit);
    }

    public StorageUnitResponseDTO createStorageUnit(StorageUnitRequestDTO request) {
        if (storageUnitRepository.existsByUnitCode(request.unitCode())) {
            throw new StorageUnitAlreadyExistsException("Storage unit with code already exists: " + request.unitCode());
        }

        StorageUnit unit = storageUnitMapper.toEntity(request);
        StorageUnit saved = storageUnitRepository.save(unit);
        return toResponseDTO(saved);
    }

    public StorageUnitResponseDTO updateStorageUnit(Long id, StorageUnitRequestDTO request) {
        StorageUnit unit = storageUnitRepository.findById(id)
                .orElseThrow(() -> new StorageUnitNotFoundException("Storage unit not found with id: " + id));

        if (!unit.getUnitCode().equals(request.unitCode()) && storageUnitRepository.existsByUnitCode(request.unitCode())) {
            throw new StorageUnitAlreadyExistsException("Storage unit with code already exists: " + request.unitCode());
        }

        unit.setUnitCode(request.unitCode());
        unit.setLocation(request.location());
        unit.setStorageType(request.storageType());
        unit.setTotalMassCapacity(request.totalMassCapacity());
        unit.setTotalVolumeCapacity(request.totalVolumeCapacity());

        StorageUnit updated = storageUnitRepository.save(unit);
        return toResponseDTO(updated);
    }

    public PageResponseDTO<CargoStorageResponseDTO> getStorageUnitInventory(Long id, int page, int size) {
        if (!storageUnitRepository.existsById(id)) {
            throw new StorageUnitNotFoundException("Storage unit not found with id: " + id);
        }

        return cargoStorageService.getStorageUnitCargo(id, page, size);
    }

    private StorageUnitResponseDTO toResponseDTO(StorageUnit unit) {
        BigDecimal availableMass = unit.getTotalMassCapacity().subtract(unit.getCurrentMass());
        BigDecimal availableVolume = unit.getTotalVolumeCapacity().subtract(unit.getCurrentVolume());

        Double massUsagePercentage = unit.getCurrentMass()
                .multiply(BigDecimal.valueOf(100))
                .divide(unit.getTotalMassCapacity(), 2, RoundingMode.HALF_UP)
                .doubleValue();

        Double volumeUsagePercentage = unit.getCurrentVolume()
                .multiply(BigDecimal.valueOf(100))
                .divide(unit.getTotalVolumeCapacity(), 2, RoundingMode.HALF_UP)
                .doubleValue();

        return storageUnitMapper.toResponseDTO(
                unit,
                availableMass,
                availableVolume,
                massUsagePercentage,
                volumeUsagePercentage
        );
    }

    public boolean storageUnitExists(Long id) {
        return storageUnitRepository.existsById(id);
    }
}
