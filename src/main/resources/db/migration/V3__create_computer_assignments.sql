CREATE TABLE computer_assignments (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    computer_id INTEGER NOT NULL,
    person_id INTEGER NOT NULL,
    assignment_type VARCHAR(30) NOT NULL,
    assigned_at DATE NOT NULL,
    due_date DATE,
    returned_at DATE,
    notes VARCHAR(1000),
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    CONSTRAINT fk_computer_assignments_computer
        FOREIGN KEY (computer_id) REFERENCES computers(id),
    CONSTRAINT fk_computer_assignments_person
        FOREIGN KEY (person_id) REFERENCES people(id),
    CONSTRAINT chk_computer_assignments_type
        CHECK (assignment_type IN ('ASSIGNMENT', 'LOAN')),
    CONSTRAINT chk_computer_assignments_due_date
        CHECK (
            (assignment_type = 'LOAN' AND due_date IS NOT NULL)
            OR
            (assignment_type = 'ASSIGNMENT' AND due_date IS NULL)
        )
);

CREATE INDEX idx_computer_assignments_computer_id
    ON computer_assignments(computer_id);

CREATE INDEX idx_computer_assignments_person_id
    ON computer_assignments(person_id);

CREATE UNIQUE INDEX uk_active_computer_person_assignment
    ON computer_assignments(computer_id, person_id)
    WHERE returned_at IS NULL;