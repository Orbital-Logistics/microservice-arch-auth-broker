package org.orbitalLogistic.spacecraft.infrastructure.adapters.out.persistence;

import lombok.RequiredArgsConstructor;
import org.orbitalLogistic.spacecraft.application.ports.out.SpacecraftRepository;
import org.orbitalLogistic.spacecraft.domain.model.Spacecraft;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SpacecraftPersistenceAdapter implements SpacecraftRepository {

    private final SpacecraftJdbcRepository jdbcRepository;
    private final SpacecraftPersistenceMapper mapper;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Spacecraft save(Spacecraft spacecraft) {
        if (spacecraft.getId() == null) {
            String sql = "INSERT INTO spacecraft " +
                    "(registry_code, name, spacecraft_type_id, mass_capacity, volume_capacity, status, current_location) " +
                    "VALUES (?, ?, ?, ?, ?, ?::spacecraft_status_enum, ?) " +
                    "RETURNING id";

            Long newId = jdbcTemplate.queryForObject(sql, Long.class,
                    spacecraft.getRegistryCode(),
                    spacecraft.getName(),
                    spacecraft.getSpacecraftTypeId(),
                    spacecraft.getMassCapacity(),
                    spacecraft.getVolumeCapacity(),
                    spacecraft.getStatus().name(),
                    spacecraft.getCurrentLocation()
            );

            return spacecraft.toBuilder().id(newId).build();
        } else {
            String sql = "UPDATE spacecraft SET " +
                    "registry_code = ?, " +
                    "name = ?, " +
                    "spacecraft_type_id = ?, " +
                    "mass_capacity = ?, " +
                    "volume_capacity = ?, " +
                    "status = ?::spacecraft_status_enum, " +
                    "current_location = ? " +
                    "WHERE id = ?";

            jdbcTemplate.update(sql,
                    spacecraft.getRegistryCode(),
                    spacecraft.getName(),
                    spacecraft.getSpacecraftTypeId(),
                    spacecraft.getMassCapacity(),
                    spacecraft.getVolumeCapacity(),
                    spacecraft.getStatus().name(),
                    spacecraft.getCurrentLocation(),
                    spacecraft.getId()
            );

            return spacecraft;
        }
    }

    @Override
    public Optional<Spacecraft> findById(Long id) {
        return jdbcRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<Spacecraft> findWithFilters(String name, String status, int limit, int offset) {
        return jdbcRepository.findWithFilters(name, status, limit, offset).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public long countWithFilters(String name, String status) {
        return jdbcRepository.countWithFilters(name, status);
    }

    @Override
    public List<Spacecraft> findAvailableForMission() {
        return jdbcRepository.findAvailableForMission().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsById(Long id) {
        return jdbcRepository.existsById(id);
    }

    @Override
    public boolean existsByRegistryCode(String registryCode) {
        return jdbcRepository.existsByRegistryCode(registryCode);
    }

    @Override
    public void deleteById(Long id) {
        jdbcRepository.deleteById(id);
    }
}

