package org.orbitalLogistic.file.infrastructure.adapters.out.persistence;

import lombok.RequiredArgsConstructor;
import org.orbitalLogistic.file.application.ports.out.CargoManifestRepository;
import org.orbitalLogistic.file.domain.model.CargoManifest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CargoManifestPersistenceAdapter implements CargoManifestRepository {

    private final CargoManifestJdbcRepository jdbcRepository;
    private final CargoManifestPersistenceMapper mapper;

    @Override
    public CargoManifest save(CargoManifest manifest) {
        CargoManifestEntity entity = mapper.toEntity(manifest);
        CargoManifestEntity saved = jdbcRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<CargoManifest> findById(Long id) {
        return jdbcRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<CargoManifest> findAll(int limit, int offset) {
        return jdbcRepository.findAllPaginated(limit, offset).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<CargoManifest> findBySpacecraftId(Long spacecraftId, int limit, int offset) {
        return jdbcRepository.findBySpacecraftIdPaginated(spacecraftId, limit, offset).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public long countAll() {
        return jdbcRepository.countAll();
    }

    @Override
    public long countBySpacecraftId(Long spacecraftId) {
        return jdbcRepository.countBySpacecraftId(spacecraftId);
    }
}
