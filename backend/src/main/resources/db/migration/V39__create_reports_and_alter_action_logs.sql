-- Create reports table
CREATE TABLE reports (
    report_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    post_id UUID NOT NULL REFERENCES posts(post_id) ON DELETE CASCADE,
    reporter_id BIGINT NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
    reason TEXT,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    resolved_at TIMESTAMP WITH TIME ZONE,
    resolved_by BIGINT REFERENCES accounts(id) ON DELETE SET NULL
);

-- Alter account_action_logs table
ALTER TABLE account_action_logs ALTER COLUMN target_account_id TYPE VARCHAR(255);
ALTER TABLE account_action_logs RENAME COLUMN target_account_id TO target_id_str;
ALTER TABLE account_action_logs ADD COLUMN target_type VARCHAR(30);

-- Update existing logs to have target_type = 'ACCOUNT'
UPDATE account_action_logs SET target_type = 'ACCOUNT' WHERE target_type IS NULL;
