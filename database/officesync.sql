CREATE DATABASE IF NOT EXISTS officesync;
USE officesync;

-- Rebuild the schema from scratch. Running this file resets existing data.
DROP TABLE IF EXISTS request_details;
DROP TABLE IF EXISTS requests;
DROP TABLE IF EXISTS supplies;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS departments;

-- Departments group users and control what Department Head accounts can see.
CREATE TABLE departments (
    department_id INT PRIMARY KEY AUTO_INCREMENT,
    department_name VARCHAR(100) NOT NULL UNIQUE
);

-- Users are login accounts. Passwords are stored as SHA-256 hashes.
CREATE TABLE users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(64) NOT NULL,
    role VARCHAR(50) NOT NULL,
    department_id INT NOT NULL,
    FOREIGN KEY (department_id) REFERENCES departments(department_id)
);

-- Supplies are inventory items. The app marks an item as low stock when
-- is_available is true and quantity_in_stock is less than or equal to reorder_level.
CREATE TABLE supplies (
    supply_id INT PRIMARY KEY AUTO_INCREMENT,
    supply_name VARCHAR(100) NOT NULL,
    category VARCHAR(50) NOT NULL,
    quantity_in_stock INT NOT NULL,
    reorder_level INT NOT NULL,
    is_available BOOLEAN NOT NULL DEFAULT TRUE
);

-- Requests store who submitted the request, the date, and review status.
CREATE TABLE requests (
    request_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    request_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'Pending',
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- Request details store the requested supply and quantity.
-- The current UI creates one detail row per request.
CREATE TABLE request_details (
    request_detail_id INT PRIMARY KEY AUTO_INCREMENT,
    request_id INT NOT NULL,
    supply_id INT NOT NULL,
    quantity_requested INT NOT NULL,
    FOREIGN KEY (request_id) REFERENCES requests(request_id),
    FOREIGN KEY (supply_id) REFERENCES supplies(supply_id)
);

INSERT INTO departments (department_name) VALUES
('Administration'),
('IT Department'),
('HR Department'),
('Operations');

INSERT INTO users (full_name, email, password_hash, role, department_id) VALUES
('Alyssa Reyes', 'admin@officesync.local', SHA2('1234', 256), 'Admin', 1),
('Marco Santos', 'head@officesync.local', SHA2('1234', 256), 'Department Head', 2),
('Juan Dela Cruz', 'employee@officesync.local', SHA2('1234', 256), 'Employee', 4),
('Maria Santos', 'maria@officesync.local', SHA2('1234', 256), 'Employee', 3);

INSERT INTO supplies (supply_name, category, quantity_in_stock, reorder_level, is_available) VALUES
('Bond Paper', 'Paper', 8, 10, TRUE),
('Ballpen', 'Writing', 35, 12, TRUE),
('Stapler', 'Desk Tool', 14, 5, TRUE),
('Folder', 'Filing', 26, 10, TRUE),
('Marker', 'Writing', 0, 8, FALSE),
('Sticky Notes', 'Paper', 18, 6, TRUE);

INSERT INTO requests (user_id, request_date, status) VALUES
(3, '2026-06-01', 'Pending'),
(4, '2026-06-02', 'Pending'),
(2, '2026-06-03', 'Pending'),
(1, '2026-06-04', 'Approved');

INSERT INTO request_details (request_id, supply_id, quantity_requested) VALUES
(1, 1, 5),
(2, 4, 3),
(3, 5, 4),
(4, 2, 10);
