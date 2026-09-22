CREATE TABLE accessories (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    accessory_type VARCHAR(30) NOT NULL,
    brand VARCHAR(100) NOT NULL,
    model VARCHAR(150) NOT NULL,
    serial_number VARCHAR(150),
    asset VARCHAR(100) COLLATE NOCASE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    CONSTRAINT chk_accessory_type CHECK (accessory_type IN ('MONITOR', 'KEYBOARD', 'MOUSE', 'HEADSET', 'OTHER'))
);

CREATE UNIQUE INDEX uk_accessories_asset ON accessories(asset) WHERE asset IS NOT NULL;
CREATE UNIQUE INDEX uk_accessories_serial ON accessories(serial_number COLLATE NOCASE) WHERE serial_number IS NOT NULL;
CREATE INDEX idx_accessories_type ON accessories(accessory_type);

CREATE TABLE accessory_assignments (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    accessory_id INTEGER NOT NULL,
    person_id INTEGER NOT NULL,
    assigned_at DATE NOT NULL,
    assigned_by VARCHAR(100) NOT NULL,
    returned_at DATE,
    received_by VARCHAR(100),
    return_notes VARCHAR(1000),
    created_at DATETIME NOT NULL,
    CONSTRAINT fk_accessory_assignment_accessory FOREIGN KEY (accessory_id) REFERENCES accessories(id),
    CONSTRAINT fk_accessory_assignment_person FOREIGN KEY (person_id) REFERENCES people(id),
    CONSTRAINT chk_accessory_assignment_dates CHECK (returned_at IS NULL OR returned_at >= assigned_at)
);

CREATE UNIQUE INDEX uk_accessory_active_assignment ON accessory_assignments(accessory_id) WHERE returned_at IS NULL;
CREATE INDEX idx_accessory_assignments_person ON accessory_assignments(person_id, returned_at);
