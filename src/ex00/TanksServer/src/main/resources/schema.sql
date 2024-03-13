DROP SCHEMA IF EXISTS tanks CASCADE;
CREATE SCHEMA IF NOT EXISTS tanks;
CREATE TABLE IF NOT EXISTS tanks.users (
    id SERIAL PRIMARY KEY,
    name VARCHAR(30) UNIQUE,
    password VARCHAR(60),
    status VARCHAR(10),
    hits INTEGER,
    misses INTEGER
);