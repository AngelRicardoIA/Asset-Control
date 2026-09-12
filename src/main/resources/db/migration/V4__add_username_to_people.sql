ALTER TABLE people ADD COLUMN username VARCHAR(100) COLLATE NOCASE;

CREATE UNIQUE INDEX uk_people_username
    ON people(username);