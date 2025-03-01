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
