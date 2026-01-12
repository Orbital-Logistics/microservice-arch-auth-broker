package org.orbitalLogistic.inventory.application.ports.in;

import org.orbitalLogistic.inventory.domain.model.InventoryTransaction;

public interface CreateInventoryTransactionUseCase {
    InventoryTransaction createTransaction(CreateInventoryTransactionCommand command);
}
