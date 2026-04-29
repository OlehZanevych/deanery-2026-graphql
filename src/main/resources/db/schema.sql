DROP SCHEMA public CASCADE;
CREATE SCHEMA public;

CREATE TABLE faculties
(
    id      BIGSERIAL PRIMARY KEY,
    name    VARCHAR(128)  NOT NULL UNIQUE,
    website VARCHAR(24)  NOT NULL,
    email   VARCHAR(32)  NOT NULL,
    phone   VARCHAR(32)  NOT NULL,
    address VARCHAR(128) NOT NULL,
    info    TEXT
);

CREATE TABLE departments
(
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(128) NOT NULL UNIQUE,
    faculty_id BIGINT REFERENCES faculties (id) ON DELETE CASCADE,
    email      VARCHAR(32) NOT NULL,
    phone      VARCHAR(32) NOT NULL,
    info       TEXT
);