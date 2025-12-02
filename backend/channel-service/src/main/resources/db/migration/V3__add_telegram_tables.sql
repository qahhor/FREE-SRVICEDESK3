-- Telegram Bot Integration Tables
-- V3: Add tables for Telegram chat and message storage

-- Table for storing Telegram chat mappings
CREATE TABLE IF NOT EXISTS telegram_chats (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    chat_id BIGINT UNIQUE NOT NULL,
    username VARCHAR(255),
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    user_id UUID REFERENCES users(id),
    is_blocked BOOLEAN DEFAULT FALSE,
    language_code VARCHAR(10),
    current_state VARCHAR(50) DEFAULT 'IDLE',
    pending_ticket_subject VARCHAR(500),
    pending_ticket_priority VARCHAR(20),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    last_modified_by VARCHAR(255),
    version BIGINT DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

-- Table for storing Telegram message history
CREATE TABLE IF NOT EXISTS telegram_messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    chat_id BIGINT NOT NULL,
    message_id BIGINT NOT NULL,
    direction VARCHAR(20) NOT NULL, -- INCOMING, OUTGOING
    message_type VARCHAR(50) NOT NULL, -- TEXT, PHOTO, DOCUMENT, VOICE, VIDEO, AUDIO, STICKER, CONTACT, LOCATION, COMMAND
    content TEXT,
    file_id VARCHAR(255),
    file_name VARCHAR(255),
    file_size BIGINT,
    mime_type VARCHAR(100),
    ticket_id UUID REFERENCES tickets(id),
    processed BOOLEAN DEFAULT FALSE,
    error_message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    last_modified_by VARCHAR(255),
    version BIGINT DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

-- Indexes for telegram_chats
CREATE INDEX idx_telegram_chats_chat_id ON telegram_chats(chat_id);
CREATE INDEX idx_telegram_chats_user_id ON telegram_chats(user_id) WHERE user_id IS NOT NULL;
CREATE INDEX idx_telegram_chats_username ON telegram_chats(username) WHERE username IS NOT NULL;

-- Indexes for telegram_messages
CREATE INDEX idx_telegram_messages_chat_id ON telegram_messages(chat_id);
CREATE INDEX idx_telegram_messages_ticket_id ON telegram_messages(ticket_id) WHERE ticket_id IS NOT NULL;
CREATE INDEX idx_telegram_messages_created_at ON telegram_messages(created_at DESC);
CREATE INDEX idx_telegram_messages_direction ON telegram_messages(direction);
CREATE INDEX idx_telegram_messages_processed ON telegram_messages(processed) WHERE processed = FALSE;

-- Comments
COMMENT ON TABLE telegram_chats IS 'Stores Telegram chat information and user mappings';
COMMENT ON COLUMN telegram_chats.chat_id IS 'Telegram chat ID (unique identifier from Telegram)';
COMMENT ON COLUMN telegram_chats.user_id IS 'Reference to linked Service Desk user account';
COMMENT ON COLUMN telegram_chats.current_state IS 'Current conversation state for multi-step operations';
COMMENT ON COLUMN telegram_chats.is_blocked IS 'Whether the chat is blocked from sending messages';

COMMENT ON TABLE telegram_messages IS 'Stores Telegram message history for audit and ticket tracking';
COMMENT ON COLUMN telegram_messages.direction IS 'Message direction: INCOMING (user to bot) or OUTGOING (bot to user)';
COMMENT ON COLUMN telegram_messages.message_type IS 'Type of message content: TEXT, PHOTO, DOCUMENT, etc.';
COMMENT ON COLUMN telegram_messages.file_id IS 'Telegram file_id for media files';
COMMENT ON COLUMN telegram_messages.ticket_id IS 'Reference to linked ticket if message is part of ticket conversation';
COMMENT ON COLUMN telegram_messages.processed IS 'Whether the message has been processed (ticket created/comment added)';
