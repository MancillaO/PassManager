-- Crear base de datos
CREATE DATABASE pass_manager;
USE pass_manager;

-- Crear tabla
CREATE TABLE passwords (
    id INT AUTO_INCREMENT PRIMARY KEY,
    servicio VARCHAR(100) NOT NULL,
    username VARCHAR(100) NOT NULL,
    password VARCHAR(255) NOT NULL,
    creation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Crear usuario seguro
CREATE USER 'manager'@'localhost' IDENTIFIED BY '123';
GRANT SELECT, INSERT, UPDATE, DELETE ON pass_manager.passwords TO 'manager'@'localhost';
FLUSH PRIVILEGES;