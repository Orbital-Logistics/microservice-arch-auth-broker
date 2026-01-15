package org.orbitalLogistic.cargo.infrastructure.adapters.out.persistence;

import lombok.RequiredArgsConstructor;
import org.orbitalLogistic.cargo.application.ports.out.StorageUnitRepository;
import org.orbitalLogistic.cargo.domain.model.StorageUnit;
import org.orbitalLogistic.cargo.infrastructure.adapters.out.persistence.entity.StorageUnitEntity;
import org.orbitalLogistic.cargo.infrastructure.adapters.out.persistence.mapper.StorageUnitPersistenceMapper;
import org.orbitalLogistic.cargo.infrastructure.adapters.out.persistence.repository.StorageUnitJdbcRepository;
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
public class StorageUnitRepositoryAdapter implements StorageUnitRepository {

    private final StorageUnitJdbcRepository jdbcRepository;
    private final StorageUnitPersistenceMapper mapper;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public StorageUnit save(StorageUnit storageUnit) {
        StorageUnitEntity entity = mapper.toEntity(storageUnit);
        StorageUnitEntity saved = jdbcRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<StorageUnit> findById(Long id) {
        return jdbcRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<StorageUnit> findAll() {
        return StreamSupport.stream(jdbcRepository.findAll().spliterator(), false)
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<StorageUnit> findByUnitCode(String unitCode) {
        return jdbcRepository.findByUnitCode(unitCode)
                .map(mapper::toDomain);
    }

    @Override
    public boolean existsByUnitCode(String unitCode) {
        return jdbcRepository.existsByUnitCode(unitCode);
    }

    @Override
    public List<StorageUnit> findByLocation(String location, int limit, int offset) {
        String sql = "SELECT * FROM storage_unit WHERE location = ? ORDER BY id DESC LIMIT ? OFFSET ?";
        
        return jdbcTemplate.query(sql,
                (rs, rowNum) -> mapper.toDomain(mapResultSetToEntity(rs)),
                location, limit, offset);
    }

    @Override
    public long countByLocation(String location) {
        return jdbcRepository.countByLocation(location);
    }

    @Override
    public void deleteById(Long id) {
        jdbcRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return jdbcRepository.existsById(id);
    }

    private StorageUnitEntity mapResultSetToEntity(ResultSet rs) throws SQLException {
        return StorageUnitEntity.builder()
                .id(rs.getLong("id"))
                .unitCode(rs.getString("unit_code"))
                .location(rs.getString("location"))
                .storageType(rs.getString("storage_type"))
                .totalMassCapacity(rs.getBigDecimal("total_mass_capacity"))
                .totalVolumeCapacity(rs.getBigDecimal("total_volume_capacity"))
                .currentMass(rs.getBigDecimal("current_mass"))
                .currentVolume(rs.getBigDecimal("current_volume"))
                .build();
    }
}
