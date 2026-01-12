package org.orbitalLogistic.inventory.application.usecases;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.orbitalLogistic.inventory.application.ports.in.GetInventoryTransactionsUseCase;
import org.orbitalLogistic.inventory.application.ports.out.InventoryTransactionRepository;
import org.orbitalLogistic.inventory.domain.model.InventoryTransaction;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetInventoryTransactionsService implements GetInventoryTransactionsUseCase {

    private final InventoryTransactionRepository inventoryTransactionRepository;

    @Override
    public List<InventoryTransaction> getAllTransactions(int page, int size) {
        log.debug("Getting all inventory transactions, page: {}, size: {}", page, size);
        int offset = page * size;
        return inventoryTransactionRepository.findAll(size, offset);
    }

    @Override
    public List<InventoryTransaction> getTransactionsByCargo(Long cargoId, int page, int size) {
        log.debug("Getting inventory transactions for cargo: {}, page: {}, size: {}", cargoId, page, size);
        int offset = page * size;
        return inventoryTransactionRepository.findByCargoId(cargoId, size, offset);
    }

    @Override
    public long countAllTransactions() {
        return inventoryTransactionRepository.countAll();
    }

    @Override
    public long countTransactionsByCargo(Long cargoId) {
        return inventoryTransactionRepository.countByCargoId(cargoId);
    }
}
