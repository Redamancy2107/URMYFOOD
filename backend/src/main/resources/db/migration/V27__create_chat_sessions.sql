CREATE TABLE IF NOT EXISTS chat_sessions (
    id         BIGSERIAL PRIMARY KEY,
    shop_id    BIGINT NOT NULL REFERENCES accounts(id),
    user_id    BIGINT NOT NULL REFERENCES accounts(id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_chat_sessions_shop_user UNIQUE (shop_id, user_id)
);

CREATE INDEX IF NOT EXISTS idx_chat_sessions_shop_id ON chat_sessions(shop_id);
CREATE INDEX IF NOT EXISTS idx_chat_sessions_user_id ON chat_sessions(user_id);
