-- Типы кораблей
INSERT INTO spacecraft_type (type_name, classification, max_crew_capacity) VALUES
('Freighter-X9', 'CARGO_HAULER', 6),
('Orion-Class', 'PERSONNEL_TRANSPORT', 24),
('Voyager-7', 'SCIENCE_VESSEL', 12);

-- Корабли
INSERT INTO spacecraft (registry_code, name, spacecraft_type_id, mass_capacity, volume_capacity, status, current_location) VALUES
('SC-001', 'Star Mule', 1, 50000.00, 1200.00, 'DOCKED', 'Orbital Station Alpha'),
('SC-002', 'Aurora', 2, 10000.00, 800.00, 'IN_TRANSIT', 'En route to Mars'),
('SC-003', 'Nebula Explorer', 3, 8000.00, 600.00, 'MAINTENANCE', 'Dry Dock 4');

