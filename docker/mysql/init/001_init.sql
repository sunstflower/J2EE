CREATE DATABASE IF NOT EXISTS drug_management
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'drug_user'@'%' IDENTIFIED BY 'drug_pass';
GRANT ALL PRIVILEGES ON drug_management.* TO 'drug_user'@'%';
FLUSH PRIVILEGES;
