CREATE TABLE account_action_logs (
    id BIGSERIAL PRIMARY KEY,
    target_account_id BIGINT NOT NULL,
    action_type VARCHAR(20) NOT NULL,
    reason TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);
