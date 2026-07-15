ALTER TABLE refresh_token
    ADD COLUMN session_type VARCHAR(16) NOT NULL DEFAULT 'WEBVIEW';

CREATE INDEX idx_refresh_token_member_session_type
    ON refresh_token (member_id, session_type);
