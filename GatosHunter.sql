-- Crear base de datos
CREATE DATABASE IF NOT EXISTS Gatos_Hunter;
USE Gatos_Hunter;

-- Tabla Users
CREATE TABLE Users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) UNIQUE NOT NULL,
    pwd VARCHAR(255),
    Dinero DOUBLE,
    Img_Path VARCHAR(255)
);

-- Tabla Gatos
CREATE TABLE Gatos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    Peso DOUBLE,
    Localidad VARCHAR(255),
    Descripcion TEXT,
    Emocion VARCHAR(255),
    Img_Path VARCHAR(255)
);


-- Tabla GatosUser (gatos asignados a usuarios)
CREATE TABLE GatosUser (
    id INT AUTO_INCREMENT PRIMARY KEY,
    cat_id INT,
    idUser INT,
    fecha datetime,
    FOREIGN KEY (idUser) REFERENCES Users(id)
);

-- Tabla Compradores
CREATE TABLE Compradores (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    Dinero DOUBLE,
    Localidad VARCHAR(255),
    Img_Path VARCHAR(255)
);
