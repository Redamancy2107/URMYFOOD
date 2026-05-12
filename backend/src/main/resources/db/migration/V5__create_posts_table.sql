DO $$ BEGIN
    CREATE TYPE post_status AS ENUM ('ACTIVE', 'SOLD_OUT', 'EXPIRED', 'INACTIVE');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

CREATE TABLE IF NOT EXISTS posts (
    post_id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    dish_name          VARCHAR(255) NOT NULL,
    price              DECIMAL(12, 2) NOT NULL DEFAULT 0,
    original_price     DECIMAL(12, 2) NOT NULL DEFAULT 0,
    max_quantity       INT NOT NULL DEFAULT 0,
    remaining_quantity INT NOT NULL DEFAULT 0,
    end_time           TIMESTAMP WITH TIME ZONE,
    is_flash_sale      BOOLEAN NOT NULL DEFAULT FALSE,
    status             post_status NOT NULL DEFAULT 'ACTIVE',
    content            TEXT,
    image_url          VARCHAR(500),
    author_id          BIGINT NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
    created_at         TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_posts_author_id ON posts(author_id);
CREATE INDEX IF NOT EXISTS idx_posts_created_at ON posts(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_posts_status ON posts(status);
