CREATE TABLE IF NOT EXISTS shop_profiles (
    id              BIGSERIAL PRIMARY KEY,
    shop_id         BIGINT NOT NULL UNIQUE REFERENCES accounts(id) ON DELETE CASCADE,
    shop_name       VARCHAR(255) NOT NULL,
    logo_url        TEXT,
    cover_url       TEXT,
    category        VARCHAR(100) NOT NULL,
    address         TEXT NOT NULL,
    description     TEXT,
    opening_hours   VARCHAR(100),
    is_open         BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_shop_profiles_shop_id ON shop_profiles(shop_id);

DO $$ BEGIN
    CREATE TRIGGER update_shop_profiles_updated_at
    BEFORE UPDATE ON shop_profiles
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;
