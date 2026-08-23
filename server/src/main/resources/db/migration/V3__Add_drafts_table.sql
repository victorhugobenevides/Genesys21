CREATE TABLE IF NOT EXISTS drafts (
    id VARCHAR(50) PRIMARY KEY,
    owner_id VARCHAR(100) NOT NULL,
    page_id VARCHAR(50) NOT NULL,
    content TEXT NOT NULL,
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    deleted_at BIGINT,
    FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_drafts_owner_id ON drafts(owner_id);
