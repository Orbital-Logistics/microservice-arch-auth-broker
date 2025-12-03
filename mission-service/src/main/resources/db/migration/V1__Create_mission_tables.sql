-- Mission Service Schema

CREATE TABLE IF NOT EXISTS mission (
    id BIGSERIAL PRIMARY KEY,
    mission_code VARCHAR(20) UNIQUE NOT NULL,
    mission_name VARCHAR(200) NOT NULL,
    mission_type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PLANNING',
    priority VARCHAR(50) NOT NULL DEFAULT 'MEDIUM',
    commanding_officer_id BIGINT NOT NULL,
    spacecraft_id BIGINT NOT NULL,
    scheduled_departure TIMESTAMP,
    scheduled_arrival TIMESTAMP
);

CREATE INDEX idx_mission_code ON mission(mission_code);
CREATE INDEX idx_mission_status ON mission(status);
CREATE INDEX idx_mission_commanding_officer ON mission(commanding_officer_id);
CREATE INDEX idx_mission_spacecraft ON mission(spacecraft_id);

CREATE TABLE IF NOT EXISTS mission_assignment (
    id BIGSERIAL PRIMARY KEY,
    mission_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    assignment_role VARCHAR(50) NOT NULL,
    responsibility_zone VARCHAR(100),
    UNIQUE(mission_id, user_id)
);

CREATE INDEX idx_mission_assignment_mission ON mission_assignment(mission_id);
CREATE INDEX idx_mission_assignment_user ON mission_assignment(user_id);

CREATE TABLE IF NOT EXISTS spacecraft_mission (
    id BIGSERIAL PRIMARY KEY,
    spacecraft_id BIGINT NOT NULL,
    mission_id BIGINT NOT NULL,
    UNIQUE(spacecraft_id, mission_id)
);

CREATE INDEX idx_spacecraft_mission_spacecraft ON spacecraft_mission(spacecraft_id);
CREATE INDEX idx_spacecraft_mission_mission ON spacecraft_mission(mission_id);

