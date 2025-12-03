-- Тестовые данные для mission-service

-- Миссии
INSERT INTO mission (mission_code, mission_name, mission_type, status, priority, commanding_officer_id, spacecraft_id, scheduled_departure, scheduled_arrival) VALUES
('MIS-2025-01', 'Mars Resupply Run', 'CARGO_TRANSPORT', 'SCHEDULED', 'HIGH', 3, 1, '2025-06-15 08:00:00', '2025-07-01 14:00:00'),
('MIS-2025-02', 'Jupiter Science Survey', 'SCIENCE_EXPEDITION', 'PLANNING', 'CRITICAL', 3, 3, '2025-09-01 00:00:00', '2026-03-01 00:00:00');

-- Назначения на миссии
INSERT INTO mission_assignment (mission_id, user_id, assignment_role, responsibility_zone) VALUES
(1, 3, 'COMMANDER', 'Bridge'),
(1, 2, 'CARGO_OFFICER', 'Cargo Bay'),
(2, 3, 'COMMANDER', 'Science Deck'),
(2, 4, 'ENGINEER', 'Engineering');

-- Связь spacecraft-mission
INSERT INTO spacecraft_mission (spacecraft_id, mission_id) VALUES
(1, 1),
(3, 2);

