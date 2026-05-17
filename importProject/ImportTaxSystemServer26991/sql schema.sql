CREATE DATABASE IF NOT EXISTS import_tax_management_system_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE import_tax_management_system_db;

CREATE TABLE IF NOT EXISTS users (
    user_id BIGINT NOT NULL AUTO_INCREMENT,
    full_name VARCHAR(120) NOT NULL,
    email VARCHAR(150) NOT NULL,
    username VARCHAR(80) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    created_at DATE NOT NULL,
    PRIMARY KEY (user_id),
    UNIQUE KEY uk_users_email (email),
    UNIQUE KEY uk_users_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS taxes (
    tax_id BIGINT NOT NULL AUTO_INCREMENT,
    tax_name VARCHAR(120) NOT NULL,
    tax_rate DECIMAL(7,4) NOT NULL,
    description VARCHAR(1000),
    PRIMARY KEY (tax_id),
    UNIQUE KEY uk_taxes_tax_name (tax_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS invoices (
    invoice_id BIGINT NOT NULL AUTO_INCREMENT,
    invoice_number VARCHAR(80) NOT NULL,
    total_tax_amount DECIMAL(15,2) NOT NULL,
    issue_date DATE NOT NULL,
    PRIMARY KEY (invoice_id),
    UNIQUE KEY uk_invoices_invoice_number (invoice_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS notifications (
    notification_id BIGINT NOT NULL AUTO_INCREMENT,
    message VARCHAR(1000) NOT NULL,
    notification_type VARCHAR(80) NOT NULL,
    recipient VARCHAR(150) NOT NULL,
    sent_at DATE NOT NULL,
    status VARCHAR(50) NOT NULL,
    PRIMARY KEY (notification_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS import_items (
    item_id BIGINT NOT NULL AUTO_INCREMENT,
    item_name VARCHAR(150) NOT NULL,
    category VARCHAR(100) NOT NULL,
    description VARCHAR(1000),
    quantity INT NOT NULL,
    unit_price DECIMAL(15,2) NOT NULL,
    country_of_origin VARCHAR(100),
    importer_name VARCHAR(150) NOT NULL,
    tax_rate DECIMAL(5,2) NOT NULL,
    total_tax DECIMAL(15,2) NOT NULL,
    import_date DATE NOT NULL,
    status VARCHAR(50) NOT NULL,
    user_id BIGINT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (item_id),
    KEY idx_import_items_user_id (user_id),
    CONSTRAINT fk_import_items_user
        FOREIGN KEY (user_id) REFERENCES users (user_id)
        ON DELETE SET NULL
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS payments (
    payment_id BIGINT NOT NULL AUTO_INCREMENT,
    amount_paid DECIMAL(15,2) NOT NULL,
    payment_date DATE NOT NULL,
    payment_method VARCHAR(80) NOT NULL,
    payment_status VARCHAR(50) NOT NULL,
    invoice_id BIGINT NOT NULL,
    PRIMARY KEY (payment_id),
    UNIQUE KEY uk_payments_invoice_id (invoice_id),
    CONSTRAINT fk_payments_invoice
        FOREIGN KEY (invoice_id) REFERENCES invoices (invoice_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO users (full_name, email, username, password, role, created_at)
SELECT 'System Administrator', 'admin@importtax.local', 'admin', 'admin123', 'ADMIN', CURRENT_DATE
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE username = 'admin'
);

INSERT INTO taxes (tax_name, tax_rate, description)
SELECT 'Standard Import Duty', 18.0000, 'Default import duty used for general imported goods.'
WHERE NOT EXISTS (
    SELECT 1 FROM taxes WHERE tax_name = 'Standard Import Duty'
);
