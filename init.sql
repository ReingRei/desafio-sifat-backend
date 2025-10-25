CREATE DATABASE IF NOT EXISTS product_db;
CREATE DATABASE IF NOT EXISTS inventory_db;

CREATE USER 'sifat_user'@'%' IDENTIFIED BY 'sifat_pass';
GRANT ALL PRIVILEGES ON product_db.* TO 'sifat_user'@'%';
GRANT ALL PRIVILEGES ON inventory_db.* TO 'sifat_user'@'%';