ALTER TABLE orders
    ALTER COLUMN order_status TYPE VARCHAR(30) USING order_status::text,
    ALTER COLUMN order_status SET DEFAULT 'PENDING';

ALTER TABLE orders
    ALTER COLUMN payment_method TYPE VARCHAR(30) USING payment_method::text;

ALTER TABLE orders
    ALTER COLUMN payment_status TYPE VARCHAR(30) USING payment_status::text,
    ALTER COLUMN payment_status SET DEFAULT 'UNPAID';
