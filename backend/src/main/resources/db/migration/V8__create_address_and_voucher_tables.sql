-- =============================================
-- Định nghĩa lại hàm update_updated_at_column nếu chưa có
-- =============================================
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- =============================================
-- Bảng addresses: Sổ địa chỉ giao hàng
-- =============================================
CREATE TABLE IF NOT EXISTS addresses (
    id          BIGSERIAL PRIMARY KEY,
    account_id  BIGINT       NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
    label       VARCHAR(50)  NOT NULL DEFAULT 'Nhà riêng',
    name        VARCHAR(255) NOT NULL,
    phone       VARCHAR(20)  NOT NULL,
    detail      TEXT         NOT NULL,
    is_default  BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_addresses_account_id ON addresses(account_id);

CREATE TRIGGER update_addresses_updated_at
BEFORE UPDATE ON addresses
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- =============================================
-- Bảng vouchers: Mã khuyến mãi
-- =============================================
CREATE TABLE IF NOT EXISTS vouchers (
    id           BIGSERIAL PRIMARY KEY,
    code         VARCHAR(50)  NOT NULL UNIQUE,
    title        VARCHAR(255) NOT NULL,
    description  TEXT,
    discount_value NUMERIC(12, 0) NOT NULL DEFAULT 0,
    min_order_value NUMERIC(12, 0) NOT NULL DEFAULT 0,
    expiry_date  DATE         NOT NULL,
    is_active    BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TRIGGER update_vouchers_updated_at
BEFORE UPDATE ON vouchers
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- =============================================
-- Dữ liệu mẫu vouchers
-- =============================================
INSERT INTO vouchers (code, title, description, discount_value, min_order_value, expiry_date, is_active) VALUES
('FREESHIP50', 'Miễn phí vận chuyển', 'Giảm 50.000đ phí vận chuyển cho đơn hàng từ 100.000đ', 50000, 100000, '2026-06-30', TRUE),
('FOOD30', 'Giảm 30% đơn hàng', 'Giảm 30% tổng đơn hàng, tối đa 100.000đ', 100000, 200000, '2026-06-15', TRUE),
('NEWUSER', 'Ưu đãi khách mới', 'Giảm 25.000đ cho đơn hàng đầu tiên', 25000, 50000, '2026-07-31', TRUE),
('COMBO20', 'Combo tiết kiệm', 'Giảm 20.000đ khi đặt combo từ 2 món trở lên', 20000, 80000, '2026-06-20', TRUE);
