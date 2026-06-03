CREATE TABLE IF NOT EXISTS shop_verifications (
    id              BIGSERIAL PRIMARY KEY,
    shop_id         BIGINT NOT NULL UNIQUE REFERENCES accounts(id) ON DELETE CASCADE,
    shop_name       VARCHAR(255) NOT NULL,
    category        VARCHAR(100) NOT NULL,
    address         TEXT NOT NULL,
    latitude        DOUBLE PRECISION,
    longitude       DOUBLE PRECISION,
    cccd_front_url  TEXT NOT NULL,
    cccd_back_url   TEXT NOT NULL,
    shop_photo_urls TEXT NOT NULL,
    status          VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    reject_reason   TEXT,
    created_at      TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_shop_verifications_shop_id ON shop_verifications(shop_id);
CREATE INDEX IF NOT EXISTS idx_shop_verifications_status ON shop_verifications(status);

DO $$ BEGIN
    CREATE TRIGGER update_shop_verifications_updated_at
    BEFORE UPDATE ON shop_verifications
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;
