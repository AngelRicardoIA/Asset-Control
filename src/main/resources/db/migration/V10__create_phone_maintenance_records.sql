CREATE TABLE phone_maintenance_records (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    phone_id INTEGER NOT NULL,
    record_type VARCHAR(30) NOT NULL,
    performed_at DATE NOT NULL,
    description VARCHAR(1000) NOT NULL,
    performed_by VARCHAR(100) NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    CONSTRAINT fk_phone_maintenance_records_phone
        FOREIGN KEY (phone_id) REFERENCES phones(id)
);

CREATE INDEX idx_phone_maintenance_records_phone_id
    ON phone_maintenance_records(phone_id);
