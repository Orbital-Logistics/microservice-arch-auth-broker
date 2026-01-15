package org.orbitalLogistic.cargo.infrastructure.adapters.out.persistence;

import lombok.RequiredArgsConstructor;
import org.orbitalLogistic.cargo.application.ports.out.CargoCategoryRepository;
import org.orbitalLogistic.cargo.domain.model.CargoCategory;
import org.orbitalLogistic.cargo.infrastructure.adapters.out.persistence.entity.CargoCategoryEntity;
import org.orbitalLogistic.cargo.infrastructure.adapters.out.persistence.mapper.CargoCategoryPersistenceMapper;
import org.orbitalLogistic.cargo.infrastructure.adapters.out.persistence.repository.CargoCategoryJdbcRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Repository
@RequiredArgsConstructor
public class CargoCategoryRepositoryAdapter implements CargoCategoryRepository {

    private final CargoCategoryJdbcRepository jdbcRepository;
    private final CargoCategoryPersistenceMapper mapper;

    @Override
    public CargoCategory save(CargoCategory cargoCategory) {
        CargoCategoryEntity entity = mapper.toEntity(cargoCategory);
        CargoCategoryEntity saved = jdbcRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<CargoCategory> findById(Long id) {
        return jdbcRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<CargoCategory> findAll() {
        return StreamSupport.stream(jdbcRepository.findAll().spliterator(), false)
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<CargoCategory> findByParentCategoryIdIsNull() {
        return jdbcRepository.findByParentCategoryIdIsNull().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<CargoCategory> findByParentCategoryId(Long parentCategoryId) {
        return jdbcRepository.findByParentCategoryId(parentCategoryId).stream()
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
}
