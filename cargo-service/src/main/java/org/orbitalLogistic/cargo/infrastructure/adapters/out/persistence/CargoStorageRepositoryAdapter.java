package org.orbitalLogistic.cargo.infrastructure.adapters.out.persistence;

import lombok.RequiredArgsConstructor;
import org.orbitalLogistic.cargo.application.ports.out.CargoStorageRepository;
import org.orbitalLogistic.cargo.domain.model.CargoStorage;
import org.orbitalLogistic.cargo.infrastructure.adapters.out.persistence.entity.CargoStorageEntity;
import org.orbitalLogistic.cargo.infrastructure.adapters.out.persistence.mapper.CargoStoragePersistenceMapper;
import org.orbitalLogistic.cargo.infrastructure.adapters.out.persistence.repository.CargoStorageJdbcRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Repository
@RequiredArgsConstructor
public class CargoStorageRepositoryAdapter implements CargoStorageRepository {

    private final CargoStorageJdbcRepository jdbcRepository;
    private final CargoStoragePersistenceMapper mapper;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public CargoStorage save(CargoStorage cargoStorage) {
        CargoStorageEntity entity = mapper.toEntity(cargoStorage);
        CargoStorageEntity saved = jdbcRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<CargoStorage> findById(Long id) {
        return jdbcRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<CargoStorage> findAll() {
        return StreamSupport.stream(jdbcRepository.findAll().spliterator(), false)
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<CargoStorage> findByStorageUnitId(Long storageUnitId) {
        return jdbcRepository.findByStorageUnitId(storageUnitId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<CargoStorage> findByCargoId(Long cargoId) {
        return jdbcRepository.findByCargoId(cargoId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        jdbcRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return jdbcRepository.existsById(id);
    }

    @Override
    public List<CargoStorage> findWithFilters(Long storageUnitId, Long cargoId, Integer minQuantity, int limit, int offset) {
        StringBuilder sql = new StringBuilder("SELECT * FROM cargo_storage WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (storageUnitId != null) {
            sql.append(" AND storage_unit_id = ?");
            params.add(storageUnitId);
        }
        if (cargoId != null) {
            sql.append(" AND cargo_id = ?");
            params.add(cargoId);
        }
        if (minQuantity != null) {
            sql.append(" AND quantity >= ?");
            params.add(minQuantity);
        }

        sql.append(" ORDER BY id DESC LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);

        return jdbcTemplate.query(sql.toString(),
                (rs, rowNum) -> mapper.toDomain(mapResultSetToEntity(rs)),
                params.toArray());
    }

    @Override
    public long countWithFilters(Long storageUnitId, Long cargoId, Integer minQuantity) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM cargo_storage WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (storageUnitId != null) {
            sql.append(" AND storage_unit_id = ?");
            params.add(storageUnitId);
        }
        if (cargoId != null) {
            sql.append(" AND cargo_id = ?");
            params.add(cargoId);
        }
        if (minQuantity != null) {
            sql.append(" AND quantity >= ?");
            params.add(minQuantity);
        }

        Long count = jdbcTemplate.queryForObject(sql.toString(), Long.class, params.toArray());
        return count != null ? count : 0L;
    }

    @Override
    public Integer sumQuantityByCargoId(Long cargoId) {
        Integer sum = jdbcRepository.sumQuantityByCargoId(cargoId);
        return sum != null ? sum : 0;
    }

    private CargoStorageEntity mapResultSetToEntity(ResultSet rs) throws SQLException {
        Timestamp storedAtTimestamp = rs.getTimestamp("stored_at");
        Timestamp lastInventoryCheckTimestamp = rs.getTimestamp("last_inventory_check");
        
        return CargoStorageEntity.builder()
                .id(rs.getLong("id"))
                .storageUnitId(rs.getLong("storage_unit_id"))
                .cargoId(rs.getLong("cargo_id"))
                .quantity(rs.getInt("quantity"))
                .storedAt(storedAtTimestamp != null ? storedAtTimestamp.toLocalDateTime() : null)
                .lastInventoryCheck(lastInventoryCheckTimestamp != null ? lastInventoryCheckTimestamp.toLocalDateTime() : null)
                .lastCheckedByUserId(rs.getObject("last_checked_by_user_id") != null ? rs.getLong("last_checked_by_user_id") : null)
                .build();
    }
}
