package org.orbitalLogistic.file.application.ports.in;

import org.orbitalLogistic.file.domain.model.InventoryTransaction;

import java.util.List;

public interface GetInventoryTransactionsUseCase {
    List<InventoryTransaction> getAllTransactions(int page, int size);
    List<InventoryTransaction> getTransactionsByCargo(Long cargoId, int page, int size);
    long countAllTransactions();
    long countTransactionsByCargo(Long cargoId);
}
