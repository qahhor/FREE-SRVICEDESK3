-- Create attachments table
CREATE TABLE IF NOT EXISTS attachments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ticket_id UUID REFERENCES tickets(id) ON DELETE CASCADE,
    comment_id UUID REFERENCES comments(id) ON DELETE CASCADE,
    uploaded_by UUID NOT NULL REFERENCES users(id),
    original_filename VARCHAR(255) NOT NULL,
    stored_filename VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    file_size BIGINT NOT NULL,
    storage_type VARCHAR(50) DEFAULT 'LOCAL',
    s3_bucket VARCHAR(255),
    s3_key VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,
    deleted BOOLEAN DEFAULT FALSE,
    CONSTRAINT attachment_reference_check CHECK (
        (ticket_id IS NOT NULL OR comment_id IS NOT NULL) AND
        NOT (ticket_id IS NOT NULL AND comment_id IS NOT NULL)
    )
);

-- Create indexes
CREATE INDEX idx_attachment_ticket ON attachments(ticket_id) WHERE deleted = FALSE;
CREATE INDEX idx_attachment_comment ON attachments(comment_id) WHERE deleted = FALSE;
CREATE INDEX idx_attachment_created ON attachments(created_at DESC);
CREATE INDEX idx_attachment_uploader ON attachments(uploaded_by);

-- Add comments
COMMENT ON TABLE attachments IS 'File attachments for tickets and comments';
COMMENT ON COLUMN attachments.ticket_id IS 'Reference to parent ticket (if attached to ticket directly)';
COMMENT ON COLUMN attachments.comment_id IS 'Reference to parent comment (if attached to comment)';
COMMENT ON COLUMN attachments.original_filename IS 'Original filename uploaded by user';
COMMENT ON COLUMN attachments.stored_filename IS 'Filename as stored on server (usually UUID)';
COMMENT ON COLUMN attachments.file_path IS 'Full path to file on server or S3 key';
COMMENT ON COLUMN attachments.content_type IS 'MIME type of the file';
COMMENT ON COLUMN attachments.file_size IS 'File size in bytes';
COMMENT ON COLUMN attachments.storage_type IS 'Storage backend: LOCAL, S3, MINIO';
