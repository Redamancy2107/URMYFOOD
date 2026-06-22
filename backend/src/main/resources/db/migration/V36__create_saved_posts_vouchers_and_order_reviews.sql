CREATE TABLE IF NOT EXISTS saved_posts (
    customer_id BIGINT NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
    post_id UUID NOT NULL REFERENCES posts(post_id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_saved_posts PRIMARY KEY (customer_id, post_id)
);

CREATE INDEX IF NOT EXISTS idx_saved_posts_customer_created_at
    ON saved_posts(customer_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_saved_posts_post_id
    ON saved_posts(post_id);

CREATE TABLE IF NOT EXISTS saved_vouchers (
    customer_id BIGINT NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
    voucher_id BIGINT NOT NULL REFERENCES vouchers(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_saved_vouchers PRIMARY KEY (customer_id, voucher_id)
);

CREATE INDEX IF NOT EXISTS idx_saved_vouchers_customer_created_at
    ON saved_vouchers(customer_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_saved_vouchers_voucher_id
    ON saved_vouchers(voucher_id);

CREATE TABLE IF NOT EXISTS order_reviews (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL REFERENCES orders(order_id) ON DELETE CASCADE,
    customer_id BIGINT NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
    shop_id BIGINT NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
    rating INTEGER NOT NULL,
    comment TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_order_reviews_order UNIQUE (order_id),
    CONSTRAINT chk_order_reviews_rating CHECK (rating BETWEEN 1 AND 5)
);

CREATE INDEX IF NOT EXISTS idx_order_reviews_customer_id ON order_reviews(customer_id);
CREATE INDEX IF NOT EXISTS idx_order_reviews_shop_id ON order_reviews(shop_id);

DO $$ BEGIN
    CREATE TRIGGER update_order_reviews_updated_at
    BEFORE UPDATE ON order_reviews
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;
