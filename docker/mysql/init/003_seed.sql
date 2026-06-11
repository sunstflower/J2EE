SET NAMES utf8mb4;

USE drug_management;

INSERT INTO user_account (user_id, user_name, role, password, enabled, created_by, updated_by, deleted)
VALUES
  (1001, '张药师', 'PHARMACIST', 'pharm123', 1, 'seed', 'seed', 0),
  (2001, '王医生', 'DOCTOR', 'doctor123', 1, 'seed', 'seed', 0)
ON DUPLICATE KEY UPDATE
  user_name = VALUES(user_name),
  role = VALUES(role),
  password = VALUES(password),
  enabled = VALUES(enabled),
  updated_by = VALUES(updated_by),
  deleted = VALUES(deleted);

INSERT INTO drug (
  drug_code, drug_name, generic_name, category, specification, unit,
  manufacturer, approval_number, purchase_price, sale_price, low_stock_threshold,
  enabled, created_by, updated_by, deleted
)
VALUES
  ('DRUG-001', '阿莫西林胶囊', '阿莫西林', '抗生素', '0.25g*24粒', '盒', '华北制药', '国药准字H11020001', 8.50, 12.00, 20, 1, 'seed', 'seed', 0),
  ('DRUG-002', '布洛芬缓释胶囊', '布洛芬', '解热镇痛', '0.3g*20粒', '盒', '白云山', '国药准字H44020002', 9.20, 14.50, 18, 1, 'seed', 'seed', 0),
  ('DRUG-003', '感冒灵颗粒', '感冒灵', '中成药', '10g*9袋', '盒', '999药业', '国药准字Z44020003', 10.00, 15.00, 15, 1, 'seed', 'seed', 0),
  ('DRUG-004', '头孢克肟分散片', '头孢克肟', '抗生素', '50mg*12片', '盒', '石药集团', '国药准字H13020004', 13.50, 18.00, 12, 1, 'seed', 'seed', 0),
  ('DRUG-005', '氯雷他定片', '氯雷他定', '抗过敏', '10mg*12片', '盒', '扬子江药业', '国药准字H32020005', 6.30, 10.00, 10, 1, 'seed', 'seed', 0),
  ('DRUG-006', '奥美拉唑肠溶胶囊', '奥美拉唑', '消化系统', '20mg*14粒', '盒', '修正药业', '国药准字H22020006', 7.80, 11.50, 10, 1, 'seed', 'seed', 0),
  ('DRUG-007', '甲硝唑片', '甲硝唑', '抗感染', '0.2g*24片', '盒', '东北制药', '国药准字H21020007', 4.80, 7.20, 8, 1, 'seed', 'seed', 0),
  ('DRUG-008', '维生素C片', '维生素C', '维生素', '100mg*100片', '瓶', '哈药集团', '国药准字H23020008', 5.50, 8.00, 25, 1, 'seed', 'seed', 0),
  ('DRUG-009', '银翘解毒片', '银翘解毒', '中成药', '24片', '盒', '同仁堂', '国药准字Z11020009', 8.00, 12.50, 10, 1, 'seed', 'seed', 0),
  ('DRUG-010', '葡萄糖酸钙口服液', '葡萄糖酸钙', '补钙', '10ml*12支', '盒', '三精制药', '国药准字H23020010', 11.00, 16.00, 8, 1, 'seed', 'seed', 0),
  ('DRUG-011', '盐酸左氧氟沙星片', '左氧氟沙星', '抗生素', '0.5g*10片', '盒', '齐鲁制药', '国药准字H37020011', 14.00, 19.80, 9, 1, 'seed', 'seed', 0),
  ('DRUG-012', '复方丹参片', '复方丹参', '心脑血管', '60片', '瓶', '天士力', '国药准字Z12020012', 12.50, 18.50, 6, 1, 'seed', 'seed', 0)
ON DUPLICATE KEY UPDATE
  drug_name = VALUES(drug_name),
  generic_name = VALUES(generic_name),
  category = VALUES(category),
  specification = VALUES(specification),
  unit = VALUES(unit),
  manufacturer = VALUES(manufacturer),
  approval_number = VALUES(approval_number),
  purchase_price = VALUES(purchase_price),
  sale_price = VALUES(sale_price),
  low_stock_threshold = VALUES(low_stock_threshold),
  enabled = VALUES(enabled),
  updated_by = VALUES(updated_by);

INSERT INTO inventory (
  drug_id, batch_no, expiry_date, quantity, locked_quantity, location_code, created_by, updated_by, deleted
)
VALUES
  (1, 'BATCH-001', '2027-12-31', 26, 0, 'A-01', 'seed', 'seed', 0),
  (2, 'BATCH-002', '2027-10-31', 14, 0, 'A-02', 'seed', 'seed', 0),
  (3, 'BATCH-003', '2027-11-30', 9, 0, 'A-03', 'seed', 'seed', 0),
  (4, 'BATCH-004', '2027-09-30', 5, 0, 'A-04', 'seed', 'seed', 0),
  (5, 'BATCH-005', '2027-08-31', 7, 0, 'A-05', 'seed', 'seed', 0),
  (6, 'BATCH-006', '2027-12-15', 18, 0, 'A-06', 'seed', 'seed', 0),
  (7, 'BATCH-007', '2027-10-15', 3, 0, 'A-07', 'seed', 'seed', 0),
  (8, 'BATCH-008', '2028-01-31', 40, 0, 'B-01', 'seed', 'seed', 0),
  (9, 'BATCH-009', '2027-07-31', 4, 0, 'B-02', 'seed', 'seed', 0),
  (10, 'BATCH-010', '2027-06-30', 12, 0, 'B-03', 'seed', 'seed', 0),
  (11, 'BATCH-011', '2027-05-31', 6, 0, 'B-04', 'seed', 'seed', 0),
  (12, 'BATCH-012', '2027-04-30', 2, 0, 'B-05', 'seed', 'seed', 0)
ON DUPLICATE KEY UPDATE
  quantity = VALUES(quantity),
  locked_quantity = VALUES(locked_quantity),
  location_code = VALUES(location_code),
  updated_by = VALUES(updated_by);
