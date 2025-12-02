-- Тестовые данные для cargo-service

-- 5. Категории грузов
INSERT INTO cargo_category (name, parent_category_id, description) VALUES
('Supplies', NULL, 'General supplies'),
('Food', 1, 'Edible items'),
('Equipment', NULL, 'Tools and machinery'),
('Scientific Instruments', 3, 'Lab and research gear');

-- 6. Грузы
INSERT INTO cargo (name, cargo_category_id, mass_per_unit, volume_per_unit, cargo_type, hazard_level, is_active) VALUES
('Dehydrated Rations', 2, 0.50, 0.01, 'FOOD', 'NONE', true),
('Oxygen Tanks', 1, 10.00, 0.10, 'EQUIPMENT', 'LOW', true),
('Plasma Spectrometer', 4, 25.00, 0.30, 'SCIENTIFIC', 'NONE', true),
('Reinforced Steel Beams', 3, 500.00, 2.00, 'CONSTRUCTION_MATERIALS', 'NONE', true);

-- 7. Склады
INSERT INTO storage_unit (unit_code, location, storage_type, total_mass_capacity, total_volume_capacity, current_mass, current_volume) VALUES
('SU-ALPHA-01', 'Orbital Station Alpha', 'AMBIENT', 100000.00, 2000.00, 0.00, 0.00),
('SU-MARS-05', 'Mars Colony Base', 'PRESSURIZED', 50000.00, 1000.00, 0.00, 0.00),
('SU-HAZ-01', 'Lunar Quarantine Zone', 'HAZMAT', 10000.00, 200.00, 0.00, 0.00);

-- 10. Складские остатки
INSERT INTO cargo_storage (storage_unit_id, cargo_id, quantity, stored_at, last_checked_by_user_id) VALUES
(1, 1, 10000, CURRENT_TIMESTAMP - INTERVAL '2 days', 2),
(1, 2, 500, CURRENT_TIMESTAMP - INTERVAL '1 day', 2),
(1, 3, 20, CURRENT_TIMESTAMP, 2),
(2, 1, 2000, CURRENT_TIMESTAMP - INTERVAL '5 days', NULL);

-- Обновление текущей массы и объема в складских единицах
UPDATE storage_unit su
SET
    current_mass = COALESCE((
        SELECT SUM(c.mass_per_unit * cs.quantity)
        FROM cargo_storage cs
        JOIN cargo c ON cs.cargo_id = c.id
        WHERE cs.storage_unit_id = su.id
    ), 0),
    current_volume = COALESCE((
        SELECT SUM(c.volume_per_unit * cs.quantity)
        FROM cargo_storage cs
        JOIN cargo c ON cs.cargo_id = c.id
        WHERE cs.storage_unit_id = su.id
    ), 0);

