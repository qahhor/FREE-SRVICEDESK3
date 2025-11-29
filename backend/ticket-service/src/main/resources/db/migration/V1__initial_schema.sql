-- Initial database schema for Service Desk Platform

-- Users table
CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone VARCHAR(50),
    avatar VARCHAR(500),
    role VARCHAR(20) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true,
    email_verified BOOLEAN NOT NULL DEFAULT false,
    language VARCHAR(5) DEFAULT 'en',
    timezone VARCHAR(50) DEFAULT 'UTC',
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    last_modified_by VARCHAR(255),
    version BIGINT,
    deleted BOOLEAN NOT NULL DEFAULT false
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);

-- Teams table
CREATE TABLE teams (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    manager_id UUID REFERENCES users(id),
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    last_modified_by VARCHAR(255),
    version BIGINT,
    deleted BOOLEAN NOT NULL DEFAULT false
);

-- Team members join table
CREATE TABLE team_members (
    team_id UUID NOT NULL REFERENCES teams(id),
    user_id UUID NOT NULL REFERENCES users(id),
    PRIMARY KEY (team_id, user_id)
);

-- Projects table
CREATE TABLE projects (
    id UUID PRIMARY KEY,
    key VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(2000),
    default_team_id UUID REFERENCES teams(id),
    active BOOLEAN NOT NULL DEFAULT true,
    color VARCHAR(20),
    icon VARCHAR(50),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    last_modified_by VARCHAR(255),
    version BIGINT,
    deleted BOOLEAN NOT NULL DEFAULT false
);

CREATE INDEX idx_projects_key ON projects(key);

-- Tickets table
CREATE TABLE tickets (
    id UUID PRIMARY KEY,
    ticket_number VARCHAR(50) NOT NULL UNIQUE,
    subject VARCHAR(500) NOT NULL,
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'NEW',
    priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    channel VARCHAR(20) NOT NULL,
    project_id UUID NOT NULL REFERENCES projects(id),
    requester_id UUID NOT NULL REFERENCES users(id),
    assignee_id UUID REFERENCES users(id),
    team_id UUID REFERENCES teams(id),
    category VARCHAR(100),
    tags VARCHAR(500),
    first_response_at TIMESTAMP,
    resolved_at TIMESTAMP,
    closed_at TIMESTAMP,
    due_date TIMESTAMP,
    sla_breach_minutes INTEGER,
    is_public BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    last_modified_by VARCHAR(255),
    version BIGINT,
    deleted BOOLEAN NOT NULL DEFAULT false
);

CREATE INDEX idx_tickets_number ON tickets(ticket_number);
CREATE INDEX idx_tickets_status ON tickets(status);
CREATE INDEX idx_tickets_priority ON tickets(priority);
CREATE INDEX idx_tickets_created_at ON tickets(created_at);
CREATE INDEX idx_tickets_project ON tickets(project_id);
CREATE INDEX idx_tickets_assignee ON tickets(assignee_id);
CREATE INDEX idx_tickets_requester ON tickets(requester_id);

-- Comments table
CREATE TABLE comments (
    id UUID PRIMARY KEY,
    ticket_id UUID NOT NULL REFERENCES tickets(id) ON DELETE CASCADE,
    author_id UUID NOT NULL REFERENCES users(id),
    content TEXT NOT NULL,
    is_internal BOOLEAN NOT NULL DEFAULT false,
    is_automated BOOLEAN NOT NULL DEFAULT false,
    attachments VARCHAR(2000),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    last_modified_by VARCHAR(255),
    version BIGINT,
    deleted BOOLEAN NOT NULL DEFAULT false
);

CREATE INDEX idx_comments_ticket ON comments(ticket_id);
CREATE INDEX idx_comments_created_at ON comments(created_at);

-- Ticket sequence table for generating ticket numbers
CREATE TABLE ticket_sequences (
    project_id UUID PRIMARY KEY REFERENCES projects(id),
    next_val BIGINT NOT NULL DEFAULT 1
);

-- Insert default admin user (password: admin123 - should be changed in production)
-- Password is BCrypt hash of 'admin123'
INSERT INTO users (id, email, password, first_name, last_name, role, active, email_verified, created_at, updated_at, deleted)
VALUES (
    gen_random_uuid(),
    'admin@servicedesk.io',
    '$2a$10$8cjz47bjbR4Mn8GMg9IZx.vyjhLXR/SKKMSZ9.mP9vpMu0ssKi8GW',
    'System',
    'Administrator',
    'ADMIN',
    true,
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    false
);

-- Insert default project
INSERT INTO projects (id, key, name, description, active, created_at, updated_at, deleted)
VALUES (
    gen_random_uuid(),
    'DESK',
    'Service Desk',
    'Default service desk project',
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    false
);
