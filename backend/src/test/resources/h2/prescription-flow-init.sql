DROP TABLE IF EXISTS inventory_record;
DROP TABLE IF EXISTS prescription_item;
DROP TABLE IF EXISTS inventory;
DROP TABLE IF EXISTS prescription;
DROP TABLE IF EXISTS drug;

CREATE TABLE drug (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    drug_code VARCHAR(64) NOT NULL,
    drug_name VARCHAR(128) NOT NULL,
    generic_name VARCHAR(128),
    category VARCHAR(64),
    specification VARCHAR(128),
    unit VARCHAR(32) NOT NULL,
    manufacturer VARCHAR(128),
    approval_number VARCHAR(128),
    purchase_price DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    sale_price DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    low_stock_threshold INT NOT NULL DEFAULT 0,
    enabled TINYINT NOT NULL DEFAULT 1,
    created_by VARCHAR(64) NOT NULL DEFAULT 'system',
    updated_by VARCHAR(64) NOT NULL DEFAULT 'system',
    deleted TINYINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_drug_code UNIQUE (drug_code)
);

CREATE TABLE inventory (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    drug_id BIGINT NOT NULL,
    batch_no VARCHAR(64) NOT NULL,
    expiry_date DATE NOT NULL,
    quantity INT NOT NULL DEFAULT 0,
    locked_quantity INT NOT NULL DEFAULT 0,
    location_code VARCHAR(64),
    created_by VARCHAR(64) NOT NULL DEFAULT 'system',
    updated_by VARCHAR(64) NOT NULL DEFAULT 'system',
    deleted TINYINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_inventory_drug_batch_expiry UNIQUE (drug_id, batch_no, expiry_date),
    CONSTRAINT fk_inventory_drug FOREIGN KEY (drug_id) REFERENCES drug(id)
);

CREATE TABLE inventory_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    drug_id BIGINT NOT NULL,
    inventory_id BIGINT NOT NULL,
    record_type VARCHAR(32) NOT NULL,
    quantity_change INT NOT NULL,
    before_quantity INT NOT NULL,
    after_quantity INT NOT NULL,
    biz_no VARCHAR(64) NOT NULL,
    operator_name VARCHAR(64) NOT NULL,
    operated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    remark VARCHAR(255),
    created_by VARCHAR(64) NOT NULL DEFAULT 'system',
    updated_by VARCHAR(64) NOT NULL DEFAULT 'system',
    deleted TINYINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_inventory_record_drug FOREIGN KEY (drug_id) REFERENCES drug(id),
    CONSTRAINT fk_inventory_record_inventory FOREIGN KEY (inventory_id) REFERENCES inventory(id),
    CONSTRAINT uk_inventory_record_biz_type_inventory UNIQUE (biz_no, record_type, inventory_id)
);

CREATE TABLE prescription (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    prescription_no VARCHAR(64) NOT NULL,
    patient_name VARCHAR(64) NOT NULL,
    created_by_user_id BIGINT NOT NULL,
    created_by_role VARCHAR(32) NOT NULL,
    doctor_id BIGINT NOT NULL,
    doctor_name VARCHAR(64) NOT NULL,
    status VARCHAR(32) NOT NULL,
    doctor_approval_status VARCHAR(32) NOT NULL DEFAULT 'NONE',
    doctor_approved_at TIMESTAMP NULL,
    pharmacist_operator_id BIGINT NULL,
    audit_by VARCHAR(64) NULL,
    audit_time TIMESTAMP NULL,
    dispense_by VARCHAR(64) NULL,
    dispense_time TIMESTAMP NULL,
    reject_reason VARCHAR(255) NULL,
    remark VARCHAR(255) NULL,
    created_by VARCHAR(64) NOT NULL DEFAULT 'system',
    updated_by VARCHAR(64) NOT NULL DEFAULT 'system',
    deleted TINYINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_prescription_no UNIQUE (prescription_no)
);

CREATE TABLE prescription_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    prescription_id BIGINT NOT NULL,
    drug_id BIGINT NOT NULL,
    dosage VARCHAR(64),
    frequency VARCHAR(64),
    days INT NOT NULL DEFAULT 1,
    quantity INT NOT NULL,
    created_by VARCHAR(64) NOT NULL DEFAULT 'system',
    updated_by VARCHAR(64) NOT NULL DEFAULT 'system',
    deleted TINYINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_prescription_item_prescription FOREIGN KEY (prescription_id) REFERENCES prescription(id),
    CONSTRAINT fk_prescription_item_drug FOREIGN KEY (drug_id) REFERENCES drug(id)
);

INSERT INTO drug (
    id, drug_code, drug_name, generic_name, category, specification, unit, manufacturer,
    approval_number, purchase_price, sale_price, low_stock_threshold, enabled, created_by, updated_by
) VALUES (
    1, 'DRUG-001', '阿莫西林胶囊', '阿莫西林', '抗生素', '0.25g*24粒', '盒', '示例制药厂',
    '国药准字X0000001', 8.50, 12.00, 20, 1, 'seed', 'seed'
);

INSERT INTO inventory (
    id, drug_id, batch_no, expiry_date, quantity, locked_quantity, location_code, created_by, updated_by, deleted
) VALUES
    (1, 1, 'BATCH-001', DATE '2026-06-10', 10, 0, 'A-01', 'seed', 'seed', 0),
    (2, 1, 'BATCH-002', DATE '2026-06-20', 10, 0, 'A-02', 'seed', 'seed', 0);
