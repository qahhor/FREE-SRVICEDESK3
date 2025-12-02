-- Create widget_sessions table
CREATE TABLE widget_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    session_token VARCHAR(255) UNIQUE NOT NULL,
    visitor_name VARCHAR(255),
    visitor_email VARCHAR(255),
    visitor_metadata JSONB,
    ticket_id UUID REFERENCES tickets(id),
    project_id UUID REFERENCES projects(id) NOT NULL,
    status VARCHAR(50) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'CLOSED')),
    ip_address VARCHAR(45),
    user_agent TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    closed_at TIMESTAMP
);

-- Create widget_messages table
CREATE TABLE widget_messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    session_id UUID REFERENCES widget_sessions(id) NOT NULL,
    sender_type VARCHAR(20) NOT NULL CHECK (sender_type IN ('VISITOR', 'AGENT', 'SYSTEM')),
    sender_id UUID,
    content TEXT NOT NULL,
    message_type VARCHAR(50) DEFAULT 'TEXT' CHECK (message_type IN ('TEXT', 'FILE', 'IMAGE')),
    attachment_id UUID REFERENCES attachments(id),
    read_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes
CREATE INDEX idx_widget_sessions_token ON widget_sessions(session_token);
CREATE INDEX idx_widget_sessions_ticket ON widget_sessions(ticket_id);
CREATE INDEX idx_widget_sessions_project ON widget_sessions(project_id);
CREATE INDEX idx_widget_sessions_status ON widget_sessions(status);
CREATE INDEX idx_widget_sessions_created ON widget_sessions(created_at DESC);
CREATE INDEX idx_widget_messages_session ON widget_messages(session_id);
CREATE INDEX idx_widget_messages_created ON widget_messages(created_at);

-- Add comments
COMMENT ON TABLE widget_sessions IS 'Chat sessions from web widget';
COMMENT ON COLUMN widget_sessions.session_token IS 'Unique token for widget authentication';
COMMENT ON COLUMN widget_sessions.visitor_metadata IS 'Additional visitor info (browser, referrer, etc.)';
COMMENT ON COLUMN widget_sessions.status IS 'Session status: ACTIVE, CLOSED';

COMMENT ON TABLE widget_messages IS 'Messages in widget chat sessions';
COMMENT ON COLUMN widget_messages.sender_type IS 'Message sender: VISITOR, AGENT, SYSTEM';
COMMENT ON COLUMN widget_messages.message_type IS 'Message type: TEXT, FILE, IMAGE';
