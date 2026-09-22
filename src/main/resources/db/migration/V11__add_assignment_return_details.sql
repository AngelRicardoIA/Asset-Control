ALTER TABLE computer_assignments ADD COLUMN received_by VARCHAR(150);
ALTER TABLE computer_assignments ADD COLUMN return_notes VARCHAR(1000);

ALTER TABLE phone_assignments ADD COLUMN received_by VARCHAR(150);
ALTER TABLE phone_assignments ADD COLUMN return_notes VARCHAR(1000);
