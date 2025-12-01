-- Тестовые данные для user-service

-- Роли
INSERT INTO role (name, description, permissions) VALUES
('admin', 'System Administrator', '{"cargo": "full", "spacecraft": "full", "users": "full"}'),
('logistics_officer', 'Manages cargo and storage', '{"cargo": "edit", "storage": "full"}'),
('mission_commander', 'Leads missions', '{"missions": "lead", "crew": "view"}'),
('technician', 'Performs maintenance', '{"maintenance": "edit"}');

-- Пользователи
INSERT INTO users (username, email, password_hash, role_id, is_active) VALUES
('admin_user', 'admin@orbital.log', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy' , 1, true),
('logi_officer', 'logi@orbital.log', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 2, true),
('cmdr_reyes', 'reyes@orbital.log', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 3, true),
('tech_jones', 'jones@orbital.log', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 4, true);
