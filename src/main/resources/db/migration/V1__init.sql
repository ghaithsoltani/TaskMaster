-- ============================================
-- TASKMASTER DATABASE SCHEMA
-- Version: 1.0.0
-- ============================================

-- Drop tables if they exist (for clean initialization)
DROP TABLE IF EXISTS tasks;
DROP TABLE IF EXISTS users;

-- Users Table
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

-- Tasks Table
CREATE TABLE IF NOT EXISTS tasks (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'TODO',
    priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    assignee VARCHAR(50),
    due_date DATE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Constraints
    CONSTRAINT chk_status CHECK (status IN ('TODO', 'IN_PROGRESS', 'DONE')),
    CONSTRAINT chk_priority CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'URGENT'))
    );

-- Indexes for performance
CREATE INDEX idx_tasks_user_id ON tasks(user_id);
CREATE INDEX idx_tasks_status ON tasks(status);
CREATE INDEX idx_tasks_priority ON tasks(priority);
CREATE INDEX idx_tasks_assignee ON tasks(assignee);

-- Insert sample admin user (password: 'admin123' - bcrypt encoded)
-- In production, NEVER commit real passwords to Git
INSERT INTO users (username, email, password_hash, role)
VALUES ('admin', 'admin@taskmaster.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO', 'ADMIN');

-- Insert sample tasks
INSERT INTO tasks (user_id, title, description, status, priority, assignee, due_date)
VALUES
    (1, 'Setup project repository', 'Initialize Git repo and create initial structure', 'DONE', 'HIGH', 'admin', '2026-08-25'),
    (1, 'Design database schema', 'Create tables for users and tasks', 'DONE', 'HIGH', 'admin', '2026-08-26'),
    (1, 'Implement authentication', 'Add JWT-based login and registration', 'IN_PROGRESS', 'HIGH', 'admin', '2026-08-30'),
    (1, 'Create task API endpoints', 'Build REST endpoints for CRUD operations', 'TODO', 'MEDIUM', 'admin', '2026-09-01'),
    (1, 'Write unit tests', 'Achieve 80% code coverage', 'TODO', 'MEDIUM', 'admin', '2026-09-05');
