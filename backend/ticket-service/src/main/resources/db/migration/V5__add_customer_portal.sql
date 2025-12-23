-- Customer Portal Tables Migration
-- V5__add_customer_portal.sql

-- Email verification tokens
CREATE TABLE email_verifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id) NOT NULL,
    token VARCHAR(255) UNIQUE NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    verified_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Password reset tokens
CREATE TABLE password_resets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id) NOT NULL,
    token VARCHAR(255) UNIQUE NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    used_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Knowledge Base Categories
CREATE TABLE kb_categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    slug VARCHAR(255) UNIQUE NOT NULL,
    icon VARCHAR(50),
    parent_id UUID REFERENCES kb_categories(id),
    sort_order INTEGER DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    last_modified_by VARCHAR(255),
    deleted BOOLEAN NOT NULL DEFAULT false
);

-- Knowledge Base Articles
CREATE TABLE kb_articles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(500) NOT NULL,
    slug VARCHAR(500) UNIQUE NOT NULL,
    content TEXT NOT NULL,
    excerpt VARCHAR(1000),
    category_id UUID REFERENCES kb_categories(id),
    author_id UUID REFERENCES users(id),
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    view_count INTEGER NOT NULL DEFAULT 0,
    helpful_count INTEGER NOT NULL DEFAULT 0,
    not_helpful_count INTEGER NOT NULL DEFAULT 0,
    tags VARCHAR(500),
    published_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    last_modified_by VARCHAR(255),
    deleted BOOLEAN NOT NULL DEFAULT false
);

-- Article feedback
CREATE TABLE article_feedback (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    article_id UUID NOT NULL REFERENCES kb_articles(id),
    user_id UUID REFERENCES users(id),
    session_id VARCHAR(255),
    helpful BOOLEAN NOT NULL,
    comment TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_email_verifications_token ON email_verifications(token);
CREATE INDEX idx_email_verifications_user ON email_verifications(user_id);
CREATE INDEX idx_password_resets_token ON password_resets(token);
CREATE INDEX idx_password_resets_user ON password_resets(user_id);
CREATE INDEX idx_kb_categories_slug ON kb_categories(slug);
CREATE INDEX idx_kb_categories_parent ON kb_categories(parent_id);
CREATE INDEX idx_kb_articles_slug ON kb_articles(slug);
CREATE INDEX idx_kb_articles_category ON kb_articles(category_id);
CREATE INDEX idx_kb_articles_status ON kb_articles(status);
CREATE INDEX idx_kb_articles_published ON kb_articles(published_at);
CREATE INDEX idx_article_feedback_article ON article_feedback(article_id);

-- Insert sample KB categories
INSERT INTO kb_categories (id, name, description, slug, icon, sort_order, active, created_at, updated_at, deleted)
VALUES 
    (gen_random_uuid(), 'Getting Started', 'Learn the basics of using our service desk', 'getting-started', 'rocket_launch', 1, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
    (gen_random_uuid(), 'Account & Billing', 'Manage your account and billing information', 'account-billing', 'account_balance', 2, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
    (gen_random_uuid(), 'Troubleshooting', 'Solutions to common problems', 'troubleshooting', 'build', 3, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
    (gen_random_uuid(), 'FAQs', 'Frequently asked questions', 'faqs', 'help', 4, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false);

-- Insert sample KB articles
INSERT INTO kb_articles (id, title, slug, content, excerpt, category_id, status, view_count, published_at, created_at, updated_at, deleted)
SELECT 
    gen_random_uuid(),
    'How to Submit a Support Ticket',
    'how-to-submit-ticket',
    '# How to Submit a Support Ticket

Submitting a support ticket is easy! Follow these steps:

## Step 1: Log in to your account
First, make sure you are logged in to your customer portal account.

## Step 2: Click "Submit Ticket"
Navigate to the Submit Ticket page from the main menu.

## Step 3: Fill out the form
- **Subject**: Provide a brief summary of your issue
- **Description**: Include as much detail as possible
- **Priority**: Select the appropriate priority level
- **Category**: Choose the category that best fits your issue

## Step 4: Attach files (optional)
You can drag and drop files or click to browse.

## Step 5: Submit
Click the Submit button and you will receive a confirmation with your ticket number.

**Tip:** The more detail you provide, the faster we can help you!',
    'Learn how to submit a support ticket step by step',
    (SELECT id FROM kb_categories WHERE slug = 'getting-started'),
    'PUBLISHED',
    150,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    false;

INSERT INTO kb_articles (id, title, slug, content, excerpt, category_id, status, view_count, published_at, created_at, updated_at, deleted)
SELECT 
    gen_random_uuid(),
    'Track Your Ticket Status',
    'track-ticket-status',
    '# Track Your Ticket Status

You can track your ticket status in two ways:

## Method 1: Using the Track Ticket Page
1. Go to the Track Ticket page
2. Enter your email address
3. Enter your ticket number (e.g., DESK-123)
4. Click Track

## Method 2: Log in to Your Account
If you have an account, simply log in and go to "My Tickets" to see all your tickets and their current status.

## Ticket Statuses Explained

- **New**: Your ticket has been received
- **Open**: An agent is reviewing your ticket
- **In Progress**: Work is being done on your issue
- **Pending**: We are waiting for additional information
- **Resolved**: Your issue has been resolved
- **Closed**: The ticket is closed

You will receive email notifications when your ticket status changes.',
    'Learn how to track the status of your support tickets',
    (SELECT id FROM kb_categories WHERE slug = 'getting-started'),
    'PUBLISHED',
    120,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    false;
