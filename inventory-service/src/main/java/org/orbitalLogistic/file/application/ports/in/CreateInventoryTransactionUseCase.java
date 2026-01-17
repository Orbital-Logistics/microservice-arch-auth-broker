package org.orbitalLogistic.file.application.ports.in;

import org.orbitalLogistic.file.domain.model.InventoryTransaction;

public interface CreateInventoryTransactionUseCase {
    InventoryTransaction createTransaction(CreateInventoryTransactionCommand command);
}
