USE drug_management;

CREATE TABLE IF NOT EXISTS user_account (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    user_name VARCHAR(64) NOT NULL,
    role VARCHAR(32) NOT NULL,
    password VARCHAR(128) NOT NULL,
    enabled TINYINT NOT NULL DEFAULT 1,
    created_by VARCHAR(64) NOT NULL DEFAULT 'system',
    updated_by VARCHAR(64) NOT NULL DEFAULT 'system',
    deleted TINYINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_user_account_user_id UNIQUE (user_id)
);

CREATE TABLE IF NOT EXISTS drug (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    drug_code VARCHAR(64) NOT NULL,
    drug_name VARCHAR(128) NOT NULL,
    generic_name VARCHAR(128) NULL,
    category VARCHAR(64) NULL,
    specification VARCHAR(128) NULL,
    unit VARCHAR(32) NOT NULL,
    manufacturer VARCHAR(128) NULL,
    approval_number VARCHAR(128) NULL,
    purchase_price DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    sale_price DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    low_stock_threshold INT NOT NULL DEFAULT 0,
    enabled TINYINT NOT NULL DEFAULT 1,
    created_by VARCHAR(64) NOT NULL DEFAULT 'system',
    updated_by VARCHAR(64) NOT NULL DEFAULT 'system',
    deleted TINYINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_drug_code UNIQUE (drug_code),
    CONSTRAINT chk_drug_purchase_price CHECK (purchase_price >= 0),
    CONSTRAINT chk_drug_sale_price CHECK (sale_price >= 0),
    CONSTRAINT chk_drug_low_stock_threshold CHECK (low_stock_threshold >= 0)
);

CREATE TABLE IF NOT EXISTS inventory (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    drug_id BIGINT NOT NULL,
    batch_no VARCHAR(64) NOT NULL,
    expiry_date DATE NOT NULL,
    quantity INT NOT NULL DEFAULT 0,
    locked_quantity INT NOT NULL DEFAULT 0,
    location_code VARCHAR(64) NULL,
    created_by VARCHAR(64) NOT NULL DEFAULT 'system',
    updated_by VARCHAR(64) NOT NULL DEFAULT 'system',
    deleted TINYINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_inventory_drug_batch_expiry UNIQUE (drug_id, batch_no, expiry_date),
    CONSTRAINT fk_inventory_drug FOREIGN KEY (drug_id) REFERENCES drug(id),
    CONSTRAINT chk_inventory_quantity CHECK (quantity >= 0),
    CONSTRAINT chk_inventory_locked_quantity CHECK (locked_quantity >= 0)
);

CREATE TABLE IF NOT EXISTS inventory_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    drug_id BIGINT NOT NULL,
    inventory_id BIGINT NOT NULL,
    record_type VARCHAR(32) NOT NULL,
    quantity_change INT NOT NULL,
    before_quantity INT NOT NULL,
    after_quantity INT NOT NULL,
    biz_no VARCHAR(64) NOT NULL,
    operator_name VARCHAR(64) NOT NULL,
    operated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    remark VARCHAR(255) NULL,
    created_by VARCHAR(64) NOT NULL DEFAULT 'system',
    updated_by VARCHAR(64) NOT NULL DEFAULT 'system',
    deleted TINYINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_inventory_record_drug FOREIGN KEY (drug_id) REFERENCES drug(id),
    CONSTRAINT fk_inventory_record_inventory FOREIGN KEY (inventory_id) REFERENCES inventory(id),
    CONSTRAINT uk_inventory_record_biz_type_inventory UNIQUE (biz_no, record_type, inventory_id)
);

CREATE TABLE IF NOT EXISTS prescription (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    prescription_no VARCHAR(64) NOT NULL,
    patient_name VARCHAR(64) NOT NULL,
    created_by_user_id BIGINT NOT NULL,
    created_by_role VARCHAR(32) NOT NULL,
    doctor_id BIGINT NOT NULL,
    doctor_name VARCHAR(64) NOT NULL,
    status VARCHAR(32) NOT NULL,
    doctor_approval_status VARCHAR(32) NOT NULL DEFAULT 'NONE',
    doctor_approved_at DATETIME NULL,
    pharmacist_operator_id BIGINT NULL,
    audit_by VARCHAR(64) NULL,
    audit_time DATETIME NULL,
    dispense_by VARCHAR(64) NULL,
    dispense_time DATETIME NULL,
    reject_reason VARCHAR(255) NULL,
    remark VARCHAR(255) NULL,
    created_by VARCHAR(64) NOT NULL DEFAULT 'system',
    updated_by VARCHAR(64) NOT NULL DEFAULT 'system',
    deleted TINYINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_prescription_no UNIQUE (prescription_no)
);

CREATE TABLE IF NOT EXISTS prescription_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    prescription_id BIGINT NOT NULL,
    drug_id BIGINT NOT NULL,
    dosage VARCHAR(64) NULL,
    frequency VARCHAR(64) NULL,
    days INT NOT NULL DEFAULT 1,
    quantity INT NOT NULL,
    created_by VARCHAR(64) NOT NULL DEFAULT 'system',
    updated_by VARCHAR(64) NOT NULL DEFAULT 'system',
    deleted TINYINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_prescription_item_prescription FOREIGN KEY (prescription_id) REFERENCES prescription(id),
    CONSTRAINT fk_prescription_item_drug FOREIGN KEY (drug_id) REFERENCES drug(id),
    CONSTRAINT chk_prescription_item_days CHECK (days > 0),
    CONSTRAINT chk_prescription_item_quantity CHECK (quantity > 0)
);

CREATE TABLE IF NOT EXISTS warning_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    warning_type VARCHAR(32) NOT NULL,
    drug_id BIGINT NOT NULL,
    inventory_id BIGINT NULL,
    content VARCHAR(255) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'NEW',
    remark VARCHAR(255) NULL,
    created_by VARCHAR(64) NOT NULL DEFAULT 'system',
    updated_by VARCHAR(64) NOT NULL DEFAULT 'system',
    deleted TINYINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_warning_record_drug FOREIGN KEY (drug_id) REFERENCES drug(id),
    CONSTRAINT fk_warning_record_inventory FOREIGN KEY (inventory_id) REFERENCES inventory(id)
);

CREATE INDEX idx_inventory_drug_id ON inventory(drug_id);
CREATE INDEX idx_inventory_expiry_date ON inventory(expiry_date);
CREATE INDEX idx_inventory_record_drug_id ON inventory_record(drug_id);
CREATE INDEX idx_inventory_record_biz_no ON inventory_record(biz_no);
CREATE INDEX idx_user_account_role ON user_account(role);
CREATE INDEX idx_prescription_status ON prescription(status);
CREATE INDEX idx_prescription_doctor_id ON prescription(doctor_id);
CREATE INDEX idx_prescription_created_by_user_id ON prescription(created_by_user_id);
CREATE INDEX idx_prescription_item_prescription_id ON prescription_item(prescription_id);
CREATE INDEX idx_warning_record_status ON warning_record(status);
CREATE INDEX idx_warning_record_warning_type ON warning_record(warning_type);

INSERT INTO user_account (
    user_id,
    user_name,
    role,
    password,
    enabled,
    created_by,
    updated_by,
    deleted
) VALUES
    (1001, '张药师', 'PHARMACIST', 'pharm123', 1, 'system', 'system', 0),
    (2001, '王医生', 'DOCTOR', 'doctor123', 1, 'system', 'system', 0)
ON DUPLICATE KEY UPDATE
    user_name = VALUES(user_name),
    role = VALUES(role),
    password = VALUES(password),
    enabled = VALUES(enabled),
    updated_by = VALUES(updated_by),
    deleted = VALUES(deleted);
