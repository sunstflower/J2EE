USE drug_management;

INSERT INTO drug (
    drug_code,
    drug_name,
    generic_name,
    category,
    specification,
    unit,
    manufacturer,
    approval_number,
    purchase_price,
    sale_price,
    low_stock_threshold,
    enabled,
    created_by,
    updated_by
) VALUES
    ('DRUG-001', '阿莫西林胶囊', '阿莫西林', '抗生素', '0.25g*24粒', '盒', '示例制药厂', '国药准字X0000001', 8.50, 12.00, 20, 1, 'seed', 'seed'),
    ('DRUG-002', '布洛芬片', '布洛芬', '解热镇痛', '0.2g*24片', '盒', '示例制药厂', '国药准字X0000002', 5.20, 9.80, 15, 1, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
    drug_name = VALUES(drug_name),
    updated_by = 'seed';
