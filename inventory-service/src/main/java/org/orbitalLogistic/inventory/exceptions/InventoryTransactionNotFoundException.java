package org.orbitalLogistic.inventory.exceptions;

public class InventoryTransactionNotFoundException extends RuntimeException {
    public InventoryTransactionNotFoundException(String message) {
        super(message);
    }
}

