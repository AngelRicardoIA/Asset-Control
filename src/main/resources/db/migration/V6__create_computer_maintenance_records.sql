CREATE TABLE computer_maintenance_records (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    computer_id INTEGER NOT NULL,
    record_type VARCHAR(30) NOT NULL,
    performed_at DATE NOT NULL,
    description VARCHAR(1000) NOT NULL,
    performed_by VARCHAR(100) NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    CONSTRAINT fk_computer_maintenance_records_computer
        FOREIGN KEY (computer_id) REFERENCES computers(id),
    CONSTRAINT chk_computer_maintenance_records_type
        CHECK (
            record_type IN (
                'MAINTENANCE',
                'REPAIR',
                'COMPONENT_REPLACEMENT',
                'NOTE'
            )
        )
);

CREATE INDEX idx_computer_maintenance_records_computer_id
    ON computer_maintenance_records(computer_id);