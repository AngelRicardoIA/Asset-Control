ALTER TABLE computer_maintenance_records ADD COLUMN preventive BOOLEAN NOT NULL DEFAULT 0;
ALTER TABLE computer_maintenance_records ADD COLUMN updates_check VARCHAR(20);
ALTER TABLE computer_maintenance_records ADD COLUMN drivers_check VARCHAR(20);
ALTER TABLE computer_maintenance_records ADD COLUMN external_cleaning VARCHAR(20);
ALTER TABLE computer_maintenance_records ADD COLUMN internal_cleaning VARCHAR(20);
ALTER TABLE computer_maintenance_records ADD COLUMN checklist_notes VARCHAR(1000);

CREATE TABLE maintenance_policy (
    id INTEGER PRIMARY KEY,
    interval_months INTEGER NOT NULL,
    warning_days INTEGER NOT NULL,
    CONSTRAINT chk_maintenance_interval CHECK (interval_months BETWEEN 1 AND 60),
    CONSTRAINT chk_maintenance_warning CHECK (warning_days BETWEEN 0 AND 180)
);

INSERT INTO maintenance_policy (id, interval_months, warning_days) VALUES (1, 12, 30);

CREATE TABLE computer_maintenance_schedule_changes (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    computer_id INTEGER NOT NULL,
    next_due_at DATE NOT NULL,
    reason VARCHAR(500) NOT NULL,
    recorded_by VARCHAR(100) NOT NULL,
    created_at DATETIME NOT NULL,
    CONSTRAINT fk_schedule_computer FOREIGN KEY (computer_id) REFERENCES computers(id)
);

CREATE INDEX idx_maintenance_schedule_computer ON computer_maintenance_schedule_changes(computer_id, id);
CREATE INDEX idx_maintenance_record_preventive ON computer_maintenance_records(preventive, computer_id, performed_at);
