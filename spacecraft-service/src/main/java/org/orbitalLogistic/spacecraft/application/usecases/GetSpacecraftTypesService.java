package org.orbitalLogistic.spacecraft.application.usecases;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.orbitalLogistic.spacecraft.application.ports.in.GetSpacecraftTypesUseCase;
import org.orbitalLogistic.spacecraft.application.ports.out.SpacecraftTypeRepository;
import org.orbitalLogistic.spacecraft.domain.model.SpacecraftType;
import org.orbitalLogistic.spacecraft.exceptions.SpacecraftTypeNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetSpacecraftTypesService implements GetSpacecraftTypesUseCase {

    private final SpacecraftTypeRepository spacecraftTypeRepository;

    @Override
    public List<SpacecraftType> getAllSpacecraftTypes(int limit, int offset) {
        log.debug("Getting all spacecraft types with limit: {}, offset: {}", limit, offset);
        return spacecraftTypeRepository.findAll(limit, offset);
    }

    @Override
    public long countAllSpacecraftTypes() {
        return spacecraftTypeRepository.countAll();
    }

    @Override
    public SpacecraftType getSpacecraftTypeById(Long id) {
        log.debug("Getting spacecraft type by id: {}", id);
        return spacecraftTypeRepository.findById(id)
                .orElseThrow(() -> new SpacecraftTypeNotFoundException("Spacecraft type not found with id: " + id));
    }
}

