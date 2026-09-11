CREATE TABLE sites (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name VARCHAR(100) NOT NULL COLLATE NOCASE,
    description VARCHAR(255),
    is_active BOOLEAN NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    CONSTRAINT uk_sites_name UNIQUE (name)
);

CREATE TABLE computers (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    asset VARCHAR(100) NOT NULL COLLATE NOCASE,
    host VARCHAR(100) NOT NULL COLLATE NOCASE,
    computer_type VARCHAR(30) NOT NULL,
    brand VARCHAR(100) NOT NULL,
    model VARCHAR(150) NOT NULL,
    serial_number VARCHAR(150) NOT NULL,
    operating_system VARCHAR(100),
    status VARCHAR(30) NOT NULL,
    site_id INTEGER NOT NULL,
    observations VARCHAR(1000),
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    CONSTRAINT uk_computers_asset UNIQUE (asset),
    CONSTRAINT uk_computers_host UNIQUE (host),
    CONSTRAINT fk_computers_site FOREIGN KEY (site_id) REFERENCES sites(id)
);

CREATE INDEX idx_computers_site_id ON computers(site_id);
CREATE INDEX idx_computers_status ON computers(status);