CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'VIEWER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Seed an initial admin user (password is 'admin123' hashed with BCrypt)
--INSERT INTO users (email, password, role) 
--VALUES 
--('admin@example.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.TVuHOn2', 'ADMIN'),
--('manager@example.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.TVuHOn2', 'MANAGER'),
--('viewer@example.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.TVuHOn2', 'VIEWER');

INSERT INTO users (email, password, role) 
VALUES 
('admin@example.com', 'admin123', 'ADMIN'),
('manager@example.com', 'admin123', 'MANAGER'),
('viewer@example.com', 'admin123', 'VIEWER');
