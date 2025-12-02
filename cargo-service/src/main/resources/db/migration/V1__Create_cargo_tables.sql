-- Cargo Category Table
CREATE TABLE cargo_category (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    parent_category_id BIGINT,
    description VARCHAR(500),
    CONSTRAINT fk_parent_category FOREIGN KEY (parent_category_id) REFERENCES cargo_category(id)
);

-- Cargo Table
CREATE TABLE cargo (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    cargo_category_id BIGINT NOT NULL,
    mass_per_unit DECIMAL(10, 2) NOT NULL CHECK (mass_per_unit > 0),
    volume_per_unit DECIMAL(10, 2) NOT NULL CHECK (volume_per_unit > 0),
    cargo_type VARCHAR(50) NOT NULL,
    hazard_level VARCHAR(20) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    CONSTRAINT fk_cargo_category FOREIGN KEY (cargo_category_id) REFERENCES cargo_category(id)
);

-- Storage Unit Table
CREATE TABLE storage_unit (
    id BIGSERIAL PRIMARY KEY,
    unit_code VARCHAR(20) NOT NULL UNIQUE,
    location VARCHAR(100) NOT NULL,
    storage_type VARCHAR(50) NOT NULL,
    total_mass_capacity DECIMAL(15, 2) NOT NULL CHECK (total_mass_capacity > 0),
    total_volume_capacity DECIMAL(15, 2) NOT NULL CHECK (total_volume_capacity > 0),
    current_mass DECIMAL(15, 2) NOT NULL DEFAULT 0 CHECK (current_mass >= 0),
    current_volume DECIMAL(15, 2) NOT NULL DEFAULT 0 CHECK (current_volume >= 0)
);

-- Cargo Storage Table
CREATE TABLE cargo_storage (
    id BIGSERIAL PRIMARY KEY,
    storage_unit_id BIGINT NOT NULL,
    cargo_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity >= 0),
    stored_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_inventory_check TIMESTAMP,
    last_checked_by_user_id BIGINT,
    CONSTRAINT fk_storage_unit FOREIGN KEY (storage_unit_id) REFERENCES storage_unit(id),
    CONSTRAINT fk_cargo FOREIGN KEY (cargo_id) REFERENCES cargo(id)
);

-- Create indexes for better performance
CREATE INDEX idx_cargo_category_parent ON cargo_category(parent_category_id);
CREATE INDEX idx_cargo_category_id ON cargo(cargo_category_id);
CREATE INDEX idx_cargo_type ON cargo(cargo_type);
CREATE INDEX idx_cargo_hazard_level ON cargo(hazard_level);
CREATE INDEX idx_storage_unit_code ON storage_unit(unit_code);
CREATE INDEX idx_cargo_storage_unit ON cargo_storage(storage_unit_id);
CREATE INDEX idx_cargo_storage_cargo ON cargo_storage(cargo_id);

