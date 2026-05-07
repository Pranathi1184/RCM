CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'VIEWER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Seed an initial admin user (password is 'admin123' hashed with BCrypt)
INSERT INTO users (email, password, role) 
VALUES 
('admin@example.com', '$2a$10$XnJ1Q92a1X98pP1Yn5tNjeaWj/G5iM60s5YtW2j8B8S08A7C6Jd8K', 'ADMIN'),
('manager@example.com', '$2a$10$XnJ1Q92a1X98pP1Yn5tNjeaWj/G5iM60s5YtW2j8B8S08A7C6Jd8K', 'MANAGER'),
('viewer@example.com', '$2a$10$XnJ1Q92a1X98pP1Yn5tNjeaWj/G5iM60s5YtW2j8B8S08A7C6Jd8K', 'VIEWER');
