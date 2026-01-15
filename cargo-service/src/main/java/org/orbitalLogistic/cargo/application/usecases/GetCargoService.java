package org.orbitalLogistic.cargo.application.usecases;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.orbitalLogistic.cargo.application.ports.in.GetCargoUseCase;
import org.orbitalLogistic.cargo.application.ports.out.CargoRepository;
import org.orbitalLogistic.cargo.domain.model.Cargo;
import org.orbitalLogistic.cargo.domain.model.enums.CargoType;
import org.orbitalLogistic.cargo.domain.model.enums.HazardLevel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class GetCargoService implements GetCargoUseCase {

    private final CargoRepository cargoRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<Cargo> getCargoById(Long id) {
        log.debug("Finding cargo by id: {}", id);
        return cargoRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cargo> getAllCargos(int page, int size) {
        log.debug("Getting all cargos, page: {}, size: {}", page, size);
        int offset = page * size;
        return cargoRepository.findWithFilters(null, null, null, size, offset);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cargo> searchCargos(String name, CargoType cargoType, HazardLevel hazardLevel, int page, int size) {
        log.debug("Searching cargos with filters - name: {}, type: {}, hazard: {}", name, cargoType, hazardLevel);
        int offset = page * size;
        String typeStr = cargoType != null ? cargoType.name() : null;
        String hazardStr = hazardLevel != null ? hazardLevel.name() : null;
        return cargoRepository.findWithFilters(name, typeStr, hazardStr, size, offset);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean cargoExists(Long id) {
        log.debug("Checking if cargo exists by id: {}", id);
        return cargoRepository.existsById(id);
    }
}
