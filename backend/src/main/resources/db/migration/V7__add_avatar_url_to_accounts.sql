-- Add avatar_url column to accounts table
ALTER TABLE accounts ADD COLUMN IF NOT EXISTS avatar_url VARCHAR(2048);
