-- Đảm bảo cột payos_order_code tồn tại trước khi tạo unique index bên dưới
-- (gộp từ migration add_payos_order_code để tránh trùng version V27)
ALTER TABLE orders
    ADD COLUMN IF NOT EXISTS payos_order_code BIGINT;

UPDATE orders
SET payment_method = 'VIETQR'
WHERE payment_method IN ('MOMO', 'ZALOPAY');

ALTER TABLE orders
    DROP CONSTRAINT IF EXISTS chk_orders_payment_method;

ALTER TABLE orders
    ADD CONSTRAINT chk_orders_payment_method
    CHECK (payment_method IN ('COD', 'VIETQR'));

DROP INDEX IF EXISTS idx_orders_payos_order_code;

CREATE UNIQUE INDEX IF NOT EXISTS uk_orders_payos_order_code
    ON orders(payos_order_code)
    WHERE payos_order_code IS NOT NULL;
