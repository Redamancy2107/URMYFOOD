ALTER TABLE orders
    ADD COLUMN IF NOT EXISTS payos_checkout_url TEXT;

ALTER TABLE orders
    ADD COLUMN IF NOT EXISTS payos_qr_code TEXT;
