CREATE TABLE IF NOT EXISTS shop_follows (
    customer_id BIGINT NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
    shop_id     BIGINT NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
    created_at  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_shop_follows PRIMARY KEY (customer_id, shop_id),
    CONSTRAINT chk_shop_follows_not_self CHECK (customer_id <> shop_id)
);

CREATE INDEX IF NOT EXISTS idx_shop_follows_customer_id ON shop_follows(customer_id);
CREATE INDEX IF NOT EXISTS idx_shop_follows_shop_id ON shop_follows(shop_id);
