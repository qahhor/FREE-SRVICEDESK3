-- V6__add_sla_tables.sql
-- SLA Management System - Business Hours, Policies, Priorities, Escalations

-- Business Hours
CREATE TABLE sla_business_hours (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    timezone VARCHAR(100) DEFAULT 'UTC',
    schedule TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    last_modified_by VARCHAR(255),
    version BIGINT,
    deleted BOOLEAN NOT NULL DEFAULT false
);

-- Holidays
CREATE TABLE sla_holidays (
    id UUID PRIMARY KEY,
    business_hours_id UUID REFERENCES sla_business_hours(id),
    name VARCHAR(255) NOT NULL,
    holiday_date DATE NOT NULL,
    recurring BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    last_modified_by VARCHAR(255),
    version BIGINT,
    deleted BOOLEAN NOT NULL DEFAULT false
);

CREATE INDEX idx_sla_holidays_business_hours ON sla_holidays(business_hours_id);
CREATE INDEX idx_sla_holidays_date ON sla_holidays(holiday_date);

-- SLA Policies
CREATE TABLE sla_policies (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    business_hours_id UUID REFERENCES sla_business_hours(id),
    is_default BOOLEAN DEFAULT FALSE,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    last_modified_by VARCHAR(255),
    version BIGINT,
    deleted BOOLEAN NOT NULL DEFAULT false
);

CREATE INDEX idx_sla_policies_active ON sla_policies(active);
CREATE INDEX idx_sla_policies_default ON sla_policies(is_default);

-- SLA Priority Targets
CREATE TABLE sla_priority_targets (
    id UUID PRIMARY KEY,
    policy_id UUID REFERENCES sla_policies(id) NOT NULL,
    priority VARCHAR(50) NOT NULL,
    first_response_minutes INTEGER,
    resolution_minutes INTEGER,
    next_response_minutes INTEGER,
    first_response_enabled BOOLEAN DEFAULT TRUE,
    resolution_enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    last_modified_by VARCHAR(255),
    version BIGINT,
    deleted BOOLEAN NOT NULL DEFAULT false,
    UNIQUE(policy_id, priority)
);

CREATE INDEX idx_sla_priority_targets_policy ON sla_priority_targets(policy_id);

-- SLA Escalations
CREATE TABLE sla_escalations (
    id UUID PRIMARY KEY,
    policy_id UUID REFERENCES sla_policies(id) NOT NULL,
    name VARCHAR(255) NOT NULL,
    escalation_type VARCHAR(50) NOT NULL,
    trigger_minutes_before INTEGER NOT NULL,
    action VARCHAR(50) NOT NULL,
    reassign_to_user_id UUID REFERENCES users(id),
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    last_modified_by VARCHAR(255),
    version BIGINT,
    deleted BOOLEAN NOT NULL DEFAULT false
);

CREATE INDEX idx_sla_escalations_policy ON sla_escalations(policy_id);
CREATE INDEX idx_sla_escalations_active ON sla_escalations(active);

-- Escalation notify users (many-to-many)
CREATE TABLE sla_escalation_notify_users (
    escalation_id UUID REFERENCES sla_escalations(id) ON DELETE CASCADE,
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    PRIMARY KEY (escalation_id, user_id)
);

-- Project SLA Policies (many-to-many)
CREATE TABLE project_sla_policies (
    project_id UUID REFERENCES projects(id) ON DELETE CASCADE,
    policy_id UUID REFERENCES sla_policies(id) ON DELETE CASCADE,
    PRIMARY KEY (project_id, policy_id)
);

CREATE INDEX idx_project_sla_policies_project ON project_sla_policies(project_id);
CREATE INDEX idx_project_sla_policies_policy ON project_sla_policies(policy_id);

-- Add SLA fields to tickets
ALTER TABLE tickets ADD COLUMN sla_policy_id UUID REFERENCES sla_policies(id);
ALTER TABLE tickets ADD COLUMN sla_first_response_due TIMESTAMP;
ALTER TABLE tickets ADD COLUMN sla_resolution_due TIMESTAMP;
ALTER TABLE tickets ADD COLUMN sla_next_response_due TIMESTAMP;
ALTER TABLE tickets ADD COLUMN sla_first_response_breached BOOLEAN DEFAULT FALSE;
ALTER TABLE tickets ADD COLUMN sla_resolution_breached BOOLEAN DEFAULT FALSE;
ALTER TABLE tickets ADD COLUMN sla_paused_at TIMESTAMP;
ALTER TABLE tickets ADD COLUMN sla_paused_minutes INTEGER DEFAULT 0;

-- Indexes for SLA tracking on tickets
CREATE INDEX idx_tickets_sla_policy ON tickets(sla_policy_id);
CREATE INDEX idx_tickets_sla_first_response_due ON tickets(sla_first_response_due);
CREATE INDEX idx_tickets_sla_resolution_due ON tickets(sla_resolution_due);
CREATE INDEX idx_tickets_sla_breached ON tickets(sla_first_response_breached, sla_resolution_breached);
