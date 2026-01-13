package org.orbitalLogistic.spacecraft.infrastructure.adapters.out.persistence;

import lombok.RequiredArgsConstructor;
import org.orbitalLogistic.spacecraft.application.ports.out.SpacecraftTypeRepository;
import org.orbitalLogistic.spacecraft.domain.model.SpacecraftType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SpacecraftTypePersistenceAdapter implements SpacecraftTypeRepository {

    private final SpacecraftTypeJdbcRepository jdbcRepository;
    private final SpacecraftTypePersistenceMapper mapper;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public SpacecraftType save(SpacecraftType spacecraftType) {
        if (spacecraftType.getId() == null) {
            // Create new
            String sql = "INSERT INTO spacecraft_type " +
                    "(type_name, classification, max_crew_capacity) " +
                    "VALUES (?, ?::spacecraft_classification_enum, ?) " +
                    "RETURNING id";

            Long newId = jdbcTemplate.queryForObject(sql, Long.class,
                    spacecraftType.getTypeName(),
                    spacecraftType.getClassification().name(),
                    spacecraftType.getMaxCrewCapacity()
            );

            return spacecraftType.toBuilder().id(newId).build();
        } else {
            // Update existing
            String sql = "UPDATE spacecraft_type SET " +
                    "type_name = ?, " +
                    "classification = ?::spacecraft_classification_enum, " +
                    "max_crew_capacity = ? " +
                    "WHERE id = ?";

            jdbcTemplate.update(sql,
                    spacecraftType.getTypeName(),
                    spacecraftType.getClassification().name(),
                    spacecraftType.getMaxCrewCapacity(),
                    spacecraftType.getId()
            );

            return spacecraftType;
        }
    }

    @Override
    public Optional<SpacecraftType> findById(Long id) {
        return jdbcRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<SpacecraftType> findAll(int limit, int offset) {
        return jdbcRepository.findAllPaginated(limit, offset).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public long countAll() {
        return jdbcRepository.countAll();
    }

    @Override
    public boolean existsById(Long id) {
        return jdbcRepository.existsById(id);
    }

    @Override
    public void deleteById(Long id) {
        jdbcRepository.deleteById(id);
    }
}

