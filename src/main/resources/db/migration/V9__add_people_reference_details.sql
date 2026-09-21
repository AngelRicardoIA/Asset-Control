ALTER TABLE people ADD COLUMN job_title VARCHAR(150);

ALTER TABLE people ADD COLUMN department VARCHAR(150);

ALTER TABLE people ADD COLUMN manager_name VARCHAR(150);

CREATE INDEX idx_people_department ON people(department);
