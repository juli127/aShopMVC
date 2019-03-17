CREATE DATABASE IF NOT EXISTS eshopdb CHARACTER SET utf8 COLLATE utf8_general_ci;

CREATE TABLE IF NOT EXISTS users(
    id INT  AUTO_INCREMENT,
    login VARCHAR(40) UNIQUE NOT NULL,
    password VARCHAR(80) NOT NULL,
    name VARCHAR(30),
    address VARCHAR(50),
    comment VARCHAR(100),
    PRIMARY KEY (orderNumber)
    );

CREATE TABLE IF NOT EXISTS categories (
id INT AUTO_INCREMENT,
name VARCHAR(100) UNIQUE NOT NULL,
PRIMARY KEY (orderNumber));

CREATE TABLE IF NOT EXISTS products (
id INT AUTO_INCREMENT,
name VARCHAR(100) UNIQUE NOT NULL,
category INT,
price INT,
description VARCHAR(300),
image VARCHAR(100),
PRIMARY KEY (orderNumber),
FOREIGN KEY (category) REFERENCES categories(orderNumber));


CREATE TABLE IF NOT EXISTS carts (
 id INT,
 userId INT,
 productId INT,
 quantity INT,
 PRIMARY KEY (orderNumber),
 FOREIGN KEY (userId) REFERENCES users(orderNumber),
 FOREIGN KEY (productId) REFERENCES products(orderNumber));


CREATE TABLE IF NOT EXISTS orders (
 orderNumber INT,
 status VARCHAR(100),
 userId INT,
 productId INT,
 quantity INT,
 FOREIGN KEY (userId) REFERENCES users(orderNumber),
 FOREIGN KEY (productId) REFERENCES products(orderNumber));
