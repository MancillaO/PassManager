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

-- Usuario que me funciono
CREATE USER 'alanmy'@'%' IDENTIFIED BY '123';
GRANT SELECT, INSERT, UPDATE, DELETE ON pass_manager.* TO 'alanmy'@'%';
FLUSH PRIVILEGES;

mysql -u <USUARIO_CREADO> -p -h <IP_DESTINO>
mysql -u alanmy -p -h 192.168.56.1