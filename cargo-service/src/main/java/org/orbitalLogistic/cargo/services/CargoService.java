package org.orbitalLogistic.cargo.services;

import lombok.RequiredArgsConstructor;
import org.orbitalLogistic.cargo.dto.common.PageResponseDTO;
import org.orbitalLogistic.cargo.dto.request.CargoRequestDTO;
import org.orbitalLogistic.cargo.dto.response.CargoResponseDTO;
import org.orbitalLogistic.cargo.entities.Cargo;
import org.orbitalLogistic.cargo.entities.CargoCategory;
import org.orbitalLogistic.cargo.exceptions.CargoAlreadyExistsException;
import org.orbitalLogistic.cargo.exceptions.CargoNotFoundException;
import org.orbitalLogistic.cargo.mappers.CargoMapper;
import org.orbitalLogistic.cargo.repositories.CargoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CargoService {

    private final CargoRepository cargoRepository;
    private final CargoMapper cargoMapper;
    private final CargoCategoryService cargoCategoryService;
    private final CargoStorageService cargoStorageService;

    public List<CargoResponseDTO> getCargosScroll(int page, int size) {
        int offset = page * size;
        List<Cargo> cargos = cargoRepository.findWithFilters(null, null, null, size + 1, offset);

        return cargos.stream()
                .limit(size)
                .map(this::toResponseDTO)
                .toList();
    }

    public PageResponseDTO<CargoResponseDTO> getCargosPaged(String name, String cargoType, String hazardLevel, int page, int size) {
        int offset = page * size;
        List<Cargo> cargos = cargoRepository.findWithFilters(name, cargoType, hazardLevel, size, offset);
        long total = cargoRepository.countWithFilters(name, cargoType, hazardLevel);

        List<CargoResponseDTO> cargoDTOs = cargos.stream()
                .map(this::toResponseDTO)
                .toList();

        int totalPages = (int) Math.ceil((double) total / size);
        return new PageResponseDTO<>(cargoDTOs, page, size, total, totalPages, page == 0, page >= totalPages - 1);
    }

    public CargoResponseDTO getCargoById(Long id) {
        Cargo cargo = cargoRepository.findById(id)
                .orElseThrow(() -> new CargoNotFoundException("Cargo not found with id: " + id));
        return toResponseDTO(cargo);
    }

    public CargoResponseDTO createCargo(CargoRequestDTO request) {
        if (cargoRepository.existsByName(request.name())) {
            throw new CargoAlreadyExistsException("Cargo with name already exists: " + request.name());
        }

        CargoCategory cargoCategory = cargoCategoryService.getEntityById(request.cargoCategoryId());

        Cargo cargo = cargoMapper.toEntity(request);
        cargo.setCargoCategoryId(cargoCategory.getId());

        Cargo saved = cargoRepository.save(cargo);
        return toResponseDTO(saved);
    }

    public CargoResponseDTO updateCargo(Long id, CargoRequestDTO request) {
        Cargo cargo = cargoRepository.findById(id)
                .orElseThrow(() -> new CargoNotFoundException("Cargo not found with id: " + id));

        if (!cargo.getName().equals(request.name()) && cargoRepository.existsByName(request.name())) {
            throw new CargoAlreadyExistsException("Cargo with name already exists: " + request.name());
        }

        CargoCategory cargoCategory = cargoCategoryService.getEntityById(request.cargoCategoryId());

        cargo.setName(request.name());
        cargo.setCargoCategoryId(cargoCategory.getId());
        cargo.setMassPerUnit(request.massPerUnit());
        cargo.setVolumePerUnit(request.volumePerUnit());
        cargo.setCargoType(request.cargoType());
        cargo.setHazardLevel(request.hazardLevel());

        Cargo updated = cargoRepository.save(cargo);
        return toResponseDTO(updated);
    }

    public void deleteCargo(Long id) {
        if (!cargoRepository.existsById(id)) {
            throw new CargoNotFoundException("Cargo not found with id: " + id);
        }

        cargoRepository.deleteById(id);
    }

    public PageResponseDTO<CargoResponseDTO> searchCargos(String name, String cargoType, String hazardLevel, int page, int size) {
        return getCargosPaged(name, cargoType, hazardLevel, page, size);
    }

    public Cargo getEntityById(Long id) {
        return cargoRepository.findById(id)
                .orElseThrow(() -> new CargoNotFoundException("Cargo not found with id: " + id));
    }

    public boolean cargoExists(Long id) {
        return cargoRepository.existsById(id);
    }

    private CargoResponseDTO toResponseDTO(Cargo cargo) {
        CargoCategory cargoCategory = cargoCategoryService.getEntityById(cargo.getCargoCategoryId());
        Integer totalQuantity = cargoStorageService.calculateTotalQuantityForCargo(cargo.getId());

        return cargoMapper.toResponseDTO(
            cargo,
            cargoCategory.getName(),
            totalQuantity
        );
    }
}
