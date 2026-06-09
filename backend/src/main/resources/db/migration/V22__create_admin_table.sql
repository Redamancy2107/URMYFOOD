CREATE TABLE IF NOT EXISTS admin (
    id BIGSERIAL PRIMARY KEY,
    account_id BIGINT NOT NULL UNIQUE,
    position VARCHAR(255),
    short_bio TEXT,
    is_2fa_enabled BOOLEAN DEFAULT false,
    CONSTRAINT fk_admin_account FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE
);
