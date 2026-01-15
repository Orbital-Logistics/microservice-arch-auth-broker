package org.orbitalLogistic.cargo.infrastructure.adapters.out.persistence;

import lombok.RequiredArgsConstructor;
import org.orbitalLogistic.cargo.application.ports.out.CargoRepository;
import org.orbitalLogistic.cargo.domain.model.Cargo;
import org.orbitalLogistic.cargo.infrastructure.adapters.out.persistence.entity.CargoEntity;
import org.orbitalLogistic.cargo.infrastructure.adapters.out.persistence.mapper.CargoPersistenceMapper;
import org.orbitalLogistic.cargo.infrastructure.adapters.out.persistence.repository.CargoJdbcRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Repository
@RequiredArgsConstructor
public class CargoRepositoryAdapter implements CargoRepository {

    private final CargoJdbcRepository jdbcRepository;
    private final CargoPersistenceMapper mapper;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Cargo save(Cargo cargo) {
        CargoEntity entity = mapper.toEntity(cargo);
        CargoEntity saved = jdbcRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Cargo> findById(Long id) {
        return jdbcRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<Cargo> findAll() {
        return StreamSupport.stream(jdbcRepository.findAll().spliterator(), false)
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Cargo> findByName(String name) {
        return jdbcRepository.findByName(name).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByName(String name) {
        return jdbcRepository.existsByName(name);
    }

    @Override
    public boolean existsById(Long id) {
        return jdbcRepository.existsById(id);
    }

    @Override
    public void deleteById(Long id) {
        jdbcRepository.deleteById(id);
    }

    @Override
    public List<Cargo> findWithFilters(String name, String cargoType, String hazardLevel, int limit, int offset) {
        StringBuilder sql = new StringBuilder("SELECT * FROM cargo WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (name != null && !name.isBlank()) {
            sql.append(" AND LOWER(name) LIKE LOWER(?)");
            params.add("%" + name + "%");
        }
        if (cargoType != null && !cargoType.isBlank()) {
            sql.append(" AND cargo_type = ?");
            params.add(cargoType);
        }
        if (hazardLevel != null && !hazardLevel.isBlank()) {
            sql.append(" AND hazard_level = ?");
            params.add(hazardLevel);
        }

        sql.append(" ORDER BY id DESC LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);

        return jdbcTemplate.query(sql.toString(),
                (rs, rowNum) -> mapper.toDomain(mapResultSetToEntity(rs)),
                params.toArray());
    }

    @Override
    public long countWithFilters(String name, String cargoType, String hazardLevel) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM cargo WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (name != null && !name.isBlank()) {
            sql.append(" AND LOWER(name) LIKE LOWER(?)");
            params.add("%" + name + "%");
        }
        if (cargoType != null && !cargoType.isBlank()) {
            sql.append(" AND cargo_type = ?");
            params.add(cargoType);
        }
        if (hazardLevel != null && !hazardLevel.isBlank()) {
            sql.append(" AND hazard_level = ?");
            params.add(hazardLevel);
        }

        Long count = jdbcTemplate.queryForObject(sql.toString(), Long.class, params.toArray());
        return count != null ? count : 0L;
    }

    private CargoEntity mapResultSetToEntity(ResultSet rs) throws SQLException {
        return CargoEntity.builder()
                .id(rs.getLong("id"))
                .name(rs.getString("name"))
                .cargoCategoryId(rs.getLong("cargo_category_id"))
                .massPerUnit(rs.getBigDecimal("mass_per_unit"))
                .volumePerUnit(rs.getBigDecimal("volume_per_unit"))
                .cargoType(rs.getString("cargo_type"))
                .hazardLevel(rs.getString("hazard_level"))
                .isActive(rs.getBoolean("is_active"))
                .build();
    }
}
