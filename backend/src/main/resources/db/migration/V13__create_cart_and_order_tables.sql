DO $$ BEGIN
    CREATE TYPE order_status AS ENUM (
        'PENDING',
        'ACCEPTED',
        'PICKING_UP',
        'DELIVERING',
        'COMPLETED',
        'CANCELLED',
        'REJECTED',
        'EXPIRED'
    );
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE payment_method AS ENUM ('COD', 'MOMO', 'ZALOPAY');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE payment_status AS ENUM ('UNPAID', 'PAID', 'FAILED', 'REFUNDED');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

CREATE TABLE IF NOT EXISTS cart_items (
    cart_item_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id  BIGINT NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
    post_id      UUID   NOT NULL REFERENCES posts(post_id) ON DELETE CASCADE,
    quantity     INT    NOT NULL CHECK (quantity > 0),
    created_at   TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_cart_items_customer_post UNIQUE (customer_id, post_id)
);

CREATE INDEX IF NOT EXISTS idx_cart_items_customer_id ON cart_items(customer_id);
CREATE INDEX IF NOT EXISTS idx_cart_items_post_id ON cart_items(post_id);

DO $$ BEGIN
    CREATE TRIGGER update_cart_items_updated_at
    BEFORE UPDATE ON cart_items
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

CREATE TABLE IF NOT EXISTS orders (
    order_id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id      BIGINT NOT NULL REFERENCES accounts(id),
    shop_id          BIGINT NOT NULL REFERENCES accounts(id),
    voucher_id       BIGINT REFERENCES vouchers(id),
    total_amount     NUMERIC(12, 2) NOT NULL,
    discount_amount  NUMERIC(12, 2) NOT NULL DEFAULT 0,
    final_amount     NUMERIC(12, 2) NOT NULL,
    order_status     order_status NOT NULL DEFAULT 'PENDING',
    payment_method   payment_method NOT NULL,
    payment_status   payment_status NOT NULL DEFAULT 'UNPAID',
    delivery_address TEXT NOT NULL,
    note             TEXT,
    cancel_reason    TEXT,
    created_at       TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_orders_customer_id ON orders(customer_id);
CREATE INDEX IF NOT EXISTS idx_orders_shop_id ON orders(shop_id);
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(order_status);
CREATE INDEX IF NOT EXISTS idx_orders_created_at ON orders(created_at DESC);

DO $$ BEGIN
    CREATE TRIGGER update_orders_updated_at
    BEFORE UPDATE ON orders
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

CREATE TABLE IF NOT EXISTS order_items (
    order_item_id      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id           UUID NOT NULL REFERENCES orders(order_id) ON DELETE CASCADE,
    post_id            UUID NOT NULL REFERENCES posts(post_id),
    quantity           INT NOT NULL CHECK (quantity > 0),
    price_at_purchase  NUMERIC(12, 2) NOT NULL,
    dish_name_snapshot VARCHAR(255) NOT NULL,
    image_url_snapshot VARCHAR(500)
);

CREATE INDEX IF NOT EXISTS idx_order_items_order_id ON order_items(order_id);
CREATE INDEX IF NOT EXISTS idx_order_items_post_id ON order_items(post_id);
