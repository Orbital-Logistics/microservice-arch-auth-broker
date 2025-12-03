-- Тестовые данные для inventory-service

-- Inventory Transactions
INSERT INTO inventory_transaction (
    transaction_type, cargo_id, quantity, from_storage_unit_id, to_storage_unit_id,
    from_spacecraft_id, to_spacecraft_id, performed_by_user_id, transaction_date,
    reason_code, reference_number, notes
) VALUES
    ('LOAD', 1, 500, 1, NULL, NULL, 1, 2, CURRENT_TIMESTAMP - INTERVAL '2 days', 'MISSION_PREP', 'MIS-2025-01-LOAD-001', 'Loading rations for Mars mission'),
    ('TRANSFER', 2, 50, 1, 2, NULL, NULL, 2, CURRENT_TIMESTAMP - INTERVAL '1 day', 'RESTOCK', 'TRANS-2025-001', 'Transfer oxygen tanks between stations');

-- Cargo Manifests
INSERT INTO cargo_manifest (
    spacecraft_id, cargo_id, storage_unit_id, quantity, loaded_at, loaded_by_user_id,
    manifest_status, priority
) VALUES
    (1, 1, 1, 500, CURRENT_TIMESTAMP - INTERVAL '2 days', 2, 'LOADED', 'HIGH'),
    (1, 2, 1, 100, CURRENT_TIMESTAMP - INTERVAL '2 days', 2, 'LOADED', 'NORMAL');

