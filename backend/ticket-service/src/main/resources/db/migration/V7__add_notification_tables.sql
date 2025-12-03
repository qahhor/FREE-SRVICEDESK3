-- Notification tables for Service Desk Platform
-- V7: Notification service schema

-- Notifications table
CREATE TABLE notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id) NOT NULL,
    type VARCHAR(50) NOT NULL,  -- EMAIL, IN_APP, PUSH
    category VARCHAR(50) NOT NULL,  -- TICKET, COMMENT, SLA, SYSTEM
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    data JSONB,  -- Additional structured data
    reference_type VARCHAR(50),  -- TICKET, COMMENT, etc.
    reference_id UUID,
    read_at TIMESTAMP,
    sent_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Notification Templates
CREATE TABLE notification_templates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) UNIQUE NOT NULL,
    event_type VARCHAR(100) NOT NULL,  -- TICKET_CREATED, TICKET_ASSIGNED, etc.
    channel VARCHAR(50) NOT NULL,  -- EMAIL, IN_APP, PUSH
    subject VARCHAR(255),  -- For email
    body_template TEXT NOT NULL,  -- Thymeleaf/Mustache template
    is_active BOOLEAN DEFAULT TRUE,
    language VARCHAR(10) DEFAULT 'en',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- User Notification Preferences
CREATE TABLE notification_preferences (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id) UNIQUE NOT NULL,
    email_enabled BOOLEAN DEFAULT TRUE,
    in_app_enabled BOOLEAN DEFAULT TRUE,
    push_enabled BOOLEAN DEFAULT FALSE,
    
    -- Granular settings (JSONB)
    email_settings JSONB DEFAULT '{
        "ticket_created": true,
        "ticket_assigned": true,
        "ticket_commented": true,
        "ticket_resolved": true,
        "sla_warning": true,
        "sla_breach": true
    }',
    
    in_app_settings JSONB DEFAULT '{
        "ticket_created": true,
        "ticket_assigned": true,
        "ticket_commented": true,
        "ticket_resolved": true,
        "sla_warning": true,
        "sla_breach": true
    }',
    
    -- Quiet hours
    quiet_hours_enabled BOOLEAN DEFAULT FALSE,
    quiet_hours_start TIME,
    quiet_hours_end TIME,
    timezone VARCHAR(100) DEFAULT 'UTC',
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Push Subscriptions
CREATE TABLE push_subscriptions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id) NOT NULL,
    endpoint TEXT NOT NULL,
    p256dh_key VARCHAR(255) NOT NULL,
    auth_key VARCHAR(255) NOT NULL,
    user_agent TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_used_at TIMESTAMP,
    UNIQUE(user_id, endpoint)
);

-- Indexes for notifications
CREATE INDEX idx_notifications_user_id ON notifications(user_id);
CREATE INDEX idx_notifications_read_at ON notifications(read_at);
CREATE INDEX idx_notifications_created_at ON notifications(created_at);
CREATE INDEX idx_notifications_type ON notifications(type);
CREATE INDEX idx_notifications_category ON notifications(category);
CREATE INDEX idx_notifications_reference ON notifications(reference_type, reference_id);

-- Index for notification templates
CREATE INDEX idx_notification_templates_event ON notification_templates(event_type, channel);
CREATE INDEX idx_notification_templates_active ON notification_templates(is_active);

-- Index for push subscriptions
CREATE INDEX idx_push_subscriptions_user_id ON push_subscriptions(user_id);

-- Insert default notification templates
INSERT INTO notification_templates (name, event_type, channel, subject, body_template, is_active) VALUES
('ticket-created-email', 'TICKET_CREATED', 'EMAIL', 'New Ticket: {{ticketNumber}}', 'Hello {{userName}}, A new ticket has been created: {{ticketSubject}}. Priority: {{ticketPriority}}. View ticket: {{ticketUrl}}', true),
('ticket-created-inapp', 'TICKET_CREATED', 'IN_APP', 'New Ticket', 'New ticket {{ticketNumber}} has been created', true),
('ticket-assigned-email', 'TICKET_ASSIGNED', 'EMAIL', 'Ticket Assigned: {{ticketNumber}}', 'Hello {{userName}}, Ticket {{ticketNumber}} has been assigned to you. Subject: {{ticketSubject}}. View ticket: {{ticketUrl}}', true),
('ticket-assigned-inapp', 'TICKET_ASSIGNED', 'IN_APP', 'Ticket Assigned', 'Ticket {{ticketNumber}} has been assigned to you', true),
('ticket-commented-email', 'TICKET_COMMENTED', 'EMAIL', 'New Comment: {{ticketNumber}}', 'Hello {{userName}}, A new comment has been added to ticket {{ticketNumber}}. Comment by: {{commentAuthor}}. View ticket: {{ticketUrl}}', true),
('ticket-commented-inapp', 'TICKET_COMMENTED', 'IN_APP', 'New Comment', 'New comment on ticket {{ticketNumber}}', true),
('ticket-resolved-email', 'TICKET_RESOLVED', 'EMAIL', 'Ticket Resolved: {{ticketNumber}}', 'Hello {{userName}}, Ticket {{ticketNumber}} has been resolved. Subject: {{ticketSubject}}. View ticket: {{ticketUrl}}', true),
('ticket-resolved-inapp', 'TICKET_RESOLVED', 'IN_APP', 'Ticket Resolved', 'Ticket {{ticketNumber}} has been resolved', true),
('sla-warning-email', 'SLA_WARNING', 'EMAIL', 'SLA Warning: {{ticketNumber}}', 'Hello {{userName}}, Ticket {{ticketNumber}} is approaching SLA breach. Time remaining: {{timeRemaining}}. View ticket: {{ticketUrl}}', true),
('sla-warning-inapp', 'SLA_WARNING', 'IN_APP', 'SLA Warning', 'Ticket {{ticketNumber}} is approaching SLA breach', true),
('sla-breach-email', 'SLA_BREACHED', 'EMAIL', 'SLA Breached: {{ticketNumber}}', 'Hello {{userName}}, Ticket {{ticketNumber}} has breached SLA. Subject: {{ticketSubject}}. View ticket: {{ticketUrl}}', true),
('sla-breach-inapp', 'SLA_BREACHED', 'IN_APP', 'SLA Breached', 'Ticket {{ticketNumber}} has breached SLA', true);
