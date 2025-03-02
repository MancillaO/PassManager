-- Crear base de datos
CREATE DATABASE pass_manager;
\c pass_manager;

-- Crear tabla
CREATE TABLE passwords (
    id SERIAL PRIMARY KEY,
    servicio VARCHAR(100) NOT NULL,
    username VARCHAR(100) NOT NULL,
    password VARCHAR(255) NOT NULL,
    creation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Crear rol seguro
CREATE ROLE manager WITH LOGIN PASSWORD '123';
GRANT CONNECT ON DATABASE pass_manager TO manager;
GRANT USAGE ON SCHEMA public TO manager;
GRANT SELECT, INSERT, UPDATE, DELETE ON passwords TO manager;