CREATE TYPE spacecraft_status_enum AS ENUM ('DOCKED', 'IN_TRANSIT', 'MAINTENANCE', 'DECOMMISSIONED');
CREATE TYPE spacecraft_classification_enum AS ENUM ('CARGO_HAULER', 'PERSONNEL_TRANSPORT', 'SCIENCE_VESSEL');

CREATE TABLE spacecraft_type (
    id BIGSERIAL PRIMARY KEY,
    type_name VARCHAR(50) NOT NULL UNIQUE,
    classification spacecraft_classification_enum NOT NULL,
    max_crew_capacity INTEGER
);

CREATE TABLE spacecraft (
    id BIGSERIAL PRIMARY KEY,
    registry_code VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    spacecraft_type_id BIGINT NOT NULL REFERENCES spacecraft_type(id) ON DELETE RESTRICT,
    mass_capacity DECIMAL(15,2) NOT NULL,
    volume_capacity DECIMAL(15,2) NOT NULL,
    status spacecraft_status_enum NOT NULL DEFAULT 'DOCKED',
    current_location VARCHAR(100)
);

