CREATE TABLE phone_lines (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    number VARCHAR(30) NOT NULL,
    carrier VARCHAR(100),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE UNIQUE INDEX uq_phone_lines_number
    ON phone_lines (LOWER(number));

CREATE TABLE phones (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    imei VARCHAR(32) NOT NULL,
    brand VARCHAR(100) NOT NULL,
    model VARCHAR(100) NOT NULL,
    status VARCHAR(30) NOT NULL,
    site_id INTEGER NOT NULL,
    phone_line_id INTEGER,
    observations VARCHAR(2000),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    FOREIGN KEY (site_id) REFERENCES sites(id),
    FOREIGN KEY (phone_line_id) REFERENCES phone_lines(id)
);

CREATE UNIQUE INDEX uq_phones_imei
    ON phones (LOWER(imei));

CREATE UNIQUE INDEX uq_phones_phone_line
    ON phones (phone_line_id)
    WHERE phone_line_id IS NOT NULL;