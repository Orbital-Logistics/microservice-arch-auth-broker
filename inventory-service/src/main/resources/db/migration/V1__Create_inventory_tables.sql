-- Inventory Service Schema

-- Inventory Transactions table
CREATE TABLE IF NOT EXISTS inventory_transaction (
    id BIGSERIAL PRIMARY KEY,
    transaction_type VARCHAR(50) NOT NULL,
    cargo_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL,
    from_storage_unit_id BIGINT,
    to_storage_unit_id BIGINT,
    from_spacecraft_id BIGINT,
    to_spacecraft_id BIGINT,
    performed_by_user_id BIGINT NOT NULL,
    transaction_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reason_code VARCHAR(50),
    reference_number VARCHAR(100),
    notes TEXT,

    -- Проверочные constraints
    CONSTRAINT chk_quantity_positive CHECK (quantity > 0),
    CONSTRAINT chk_transaction_source CHECK (
        (from_storage_unit_id IS NOT NULL OR from_spacecraft_id IS NOT NULL) OR
        transaction_type IN ('LOAD', 'ADJUSTMENT')
    ),
    CONSTRAINT chk_transaction_destination CHECK (
        (to_storage_unit_id IS NOT NULL OR to_spacecraft_id IS NOT NULL) OR
        transaction_type IN ('UNLOAD', 'ADJUSTMENT', 'CONSUMPTION')
    )
);

CREATE INDEX idx_inventory_transaction_cargo ON inventory_transaction(cargo_id);
CREATE INDEX idx_inventory_transaction_from_storage ON inventory_transaction(from_storage_unit_id);
CREATE INDEX idx_inventory_transaction_to_storage ON inventory_transaction(to_storage_unit_id);
CREATE INDEX idx_inventory_transaction_from_spacecraft ON inventory_transaction(from_spacecraft_id);
CREATE INDEX idx_inventory_transaction_to_spacecraft ON inventory_transaction(to_spacecraft_id);
CREATE INDEX idx_inventory_transaction_user ON inventory_transaction(performed_by_user_id);
CREATE INDEX idx_inventory_transaction_date ON inventory_transaction(transaction_date);
CREATE INDEX idx_inventory_transaction_type ON inventory_transaction(transaction_type);

-- Cargo Manifest table
CREATE TABLE IF NOT EXISTS cargo_manifest (
    id BIGSERIAL PRIMARY KEY,
    spacecraft_id BIGINT NOT NULL,
    cargo_id BIGINT NOT NULL,
    storage_unit_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL,
    loaded_at TIMESTAMP,
    unloaded_at TIMESTAMP,
    loaded_by_user_id BIGINT NOT NULL,
    unloaded_by_user_id BIGINT,
    manifest_status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    priority VARCHAR(50) NOT NULL DEFAULT 'NORMAL',

    -- Проверочные constraints
    CONSTRAINT chk_manifest_quantity_positive CHECK (quantity > 0),
    CONSTRAINT chk_manifest_dates CHECK (
        unloaded_at IS NULL OR loaded_at IS NULL OR unloaded_at >= loaded_at
    ),
    CONSTRAINT chk_manifest_unloader CHECK (
        (unloaded_by_user_id IS NULL AND manifest_status != 'UNLOADED') OR
        (unloaded_by_user_id IS NOT NULL AND manifest_status = 'UNLOADED')
    )
);

CREATE INDEX idx_cargo_manifest_spacecraft ON cargo_manifest(spacecraft_id);
CREATE INDEX idx_cargo_manifest_cargo ON cargo_manifest(cargo_id);
CREATE INDEX idx_cargo_manifest_storage ON cargo_manifest(storage_unit_id);
CREATE INDEX idx_cargo_manifest_status ON cargo_manifest(manifest_status);
CREATE INDEX idx_cargo_manifest_priority ON cargo_manifest(priority);
CREATE INDEX idx_cargo_manifest_loaded_by ON cargo_manifest(loaded_by_user_id);

