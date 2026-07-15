ALTER TABLE native_session_code
    ADD COLUMN purpose VARCHAR(32) NOT NULL DEFAULT 'LOGIN_EXCHANGE',
    ADD COLUMN code_challenge VARCHAR(43) NULL;

CREATE INDEX idx_native_session_code_purpose ON native_session_code (purpose);
