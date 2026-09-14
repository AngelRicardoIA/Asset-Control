CREATE TABLE phone_assignments (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    phone_id INTEGER NOT NULL,
    person_id INTEGER NOT NULL,
    assignment_type VARCHAR(30) NOT NULL,
    assigned_at DATE NOT NULL,
    due_at DATE,
    returned_at DATE,
    observations VARCHAR(2000),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    FOREIGN KEY (phone_id) REFERENCES phones(id),
    FOREIGN KEY (person_id) REFERENCES people(id)
);

CREATE INDEX idx_phone_assignments_phone_id
    ON phone_assignments (phone_id);

CREATE INDEX idx_phone_assignments_person_id
    ON phone_assignments (person_id);

CREATE INDEX idx_phone_assignments_active
    ON phone_assignments (phone_id, returned_at);