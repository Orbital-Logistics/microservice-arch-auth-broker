package org.orbitalLogistic.file.infrastructure.adapters.out.persistence;

import lombok.RequiredArgsConstructor;
import org.orbitalLogistic.file.application.ports.out.InventoryTransactionRepository;
import org.orbitalLogistic.file.domain.model.InventoryTransaction;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class InventoryTransactionPersistenceAdapter implements InventoryTransactionRepository {

    private final InventoryTransactionJdbcRepository jdbcRepository;
    private final InventoryTransactionPersistenceMapper mapper;

    @Override
    public InventoryTransaction save(InventoryTransaction transaction) {
        InventoryTransactionEntity entity = mapper.toEntity(transaction);
        InventoryTransactionEntity saved = jdbcRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public List<InventoryTransaction> findAll(int limit, int offset) {
        return jdbcRepository.findAllPaginated(limit, offset).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<InventoryTransaction> findByCargoId(Long cargoId, int limit, int offset) {
        return jdbcRepository.findByCargoIdPaginated(cargoId, limit, offset).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public long countAll() {
        return jdbcRepository.countAll();
    }

    @Override
    public long countByCargoId(Long cargoId) {
        return jdbcRepository.countByCargoId(cargoId);
    }
}
