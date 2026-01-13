package org.orbitalLogistic.inventory.application.ports.out;

import org.orbitalLogistic.inventory.domain.model.InventoryTransaction;

import java.util.List;

public interface InventoryTransactionRepository {
    InventoryTransaction save(InventoryTransaction transaction);
    List<InventoryTransaction> findAll(int limit, int offset);
    List<InventoryTransaction> findByCargoId(Long cargoId, int limit, int offset);
    long countAll();
    long countByCargoId(Long cargoId);
}
