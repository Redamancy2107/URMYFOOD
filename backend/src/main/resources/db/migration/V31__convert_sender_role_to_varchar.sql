-- Convert sender_role column from custom PostgreSQL enum to VARCHAR
-- This matches the pattern used in V14 and V15 for other enum columns
ALTER TABLE chat_messages
    ALTER COLUMN sender_role TYPE VARCHAR(20) USING sender_role::text;

-- Drop the custom enum type since it's no longer needed
DROP TYPE IF EXISTS sender_role;
