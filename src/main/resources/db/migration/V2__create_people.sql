CREATE TABLE people (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    external_id VARCHAR(100) NOT NULL COLLATE NOCASE,
    full_name VARCHAR(150) NOT NULL,
    email VARCHAR(254) NOT NULL COLLATE NOCASE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    CONSTRAINT uk_people_external_id UNIQUE (external_id)
);

CREATE INDEX idx_people_full_name ON people(full_name);