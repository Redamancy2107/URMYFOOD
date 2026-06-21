ALTER TABLE orders ADD COLUMN payos_order_code BIGINT;
CREATE INDEX idx_orders_payos_order_code ON orders(payos_order_code);
