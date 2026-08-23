CREATE TABLE IF NOT EXISTS domain_mappings (
    id VARCHAR(50) PRIMARY KEY,
    domain VARCHAR(255) UNIQUE NOT NULL,
    target_page_id VARCHAR(50) NOT NULL,
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    deleted_at BIGINT,
    FOREIGN KEY (target_page_id) REFERENCES pages(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS messages (
    id VARCHAR(50) PRIMARY KEY,
    ref_id VARCHAR(50) NOT NULL,
    sender_nick VARCHAR(100) NOT NULL,
    content TEXT NOT NULL,
    is_from_merchant BOOLEAN DEFAULT FALSE,
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    deleted_at BIGINT
);

CREATE INDEX idx_messages_ref_id ON messages(ref_id);
