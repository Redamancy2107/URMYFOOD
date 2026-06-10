-- Seed demo orders for the shop profile statistics screen.
-- The note marker keeps this migration idempotent for databases where it is rerun manually.
INSERT INTO accounts (full_name, email, phone, password_hash, role, avatar_url)
VALUES (
    'Sinh viên demo thống kê',
    'statistics_customer@urmyfood.com',
    '0907778888',
    '$2a$10$DJrtmLPnBeNpJWigjYvJVOFD7SncY584GwdbWV7kf9rLMS/vDdXTW',
    'CUSTOMER',
    'https://images.unsplash.com/photo-1517841905240-472988babdf9?w=200'
)
ON CONFLICT (email) DO NOTHING;

WITH demo_customer AS (
    SELECT id FROM accounts WHERE email = 'statistics_customer@urmyfood.com'
),
demo_shops AS (
    SELECT id, email
    FROM accounts
    WHERE email IN (
        'cali_nguyenhue@urmyfood.com',
        'gongcha_ht@urmyfood.com',
        'phohung_ltt@urmyfood.com'
    )
      AND role = 'SHOP'
),
seed_orders AS (
    SELECT
        shop.email AS shop_email,
        data.total_amount,
        data.discount_amount,
        data.final_amount,
        data.order_status,
        data.payment_method,
        data.payment_status,
        data.delivery_address,
        data.note,
        data.cancel_reason,
        data.created_at
    FROM demo_shops shop
    CROSS JOIN (VALUES
        (125000::numeric,  5000::numeric, 120000::numeric, 'COMPLETED', 'COD',     'PAID',     'KTX Khu A, ĐHQG TP.HCM', 'STAT_SEED_DAY_MORNING',   NULL,                  CURRENT_DATE + TIME '08:30'),
        ( 90000::numeric,     0::numeric,  90000::numeric, 'COMPLETED', 'MOMO',    'PAID',     'KTX Khu B, ĐHQG TP.HCM', 'STAT_SEED_DAY_NOON',      NULL,                  CURRENT_DATE + TIME '12:15'),
        ( 68000::numeric,     0::numeric,  68000::numeric, 'COMPLETED', 'ZALOPAY', 'PAID',     'Nhà văn hóa Sinh viên',  'STAT_SEED_DAY_AFTERNOON', NULL,                  CURRENT_DATE + TIME '16:20'),
        (150000::numeric, 10000::numeric, 140000::numeric, 'COMPLETED', 'COD',     'PAID',     'Đường Hàn Thuyên',       'STAT_SEED_DAY_EVENING',   NULL,                  CURRENT_DATE + TIME '19:45'),
        ( 55000::numeric,     0::numeric,  55000::numeric, 'CANCELLED', 'COD',     'UNPAID',   'KTX Khu A, ĐHQG TP.HCM', 'STAT_SEED_DAY_CANCELLED', 'Khách đổi ý',          CURRENT_DATE + TIME '20:10'),

        ( 76000::numeric,     0::numeric,  76000::numeric, 'COMPLETED', 'COD',     'PAID',     'KTX Khu B, ĐHQG TP.HCM', 'STAT_SEED_MONTH_W1',      NULL,                  date_trunc('month', CURRENT_DATE) + INTERVAL '2 days 11 hours'),
        (112000::numeric, 12000::numeric, 100000::numeric, 'COMPLETED', 'MOMO',    'PAID',     'Trường ĐH KHTN',         'STAT_SEED_MONTH_W2',      NULL,                  date_trunc('month', CURRENT_DATE) + INTERVAL '9 days 12 hours'),
        ( 95000::numeric,  5000::numeric,  90000::numeric, 'COMPLETED', 'ZALOPAY', 'PAID',     'Trường ĐH CNTT',         'STAT_SEED_MONTH_W3',      NULL,                  date_trunc('month', CURRENT_DATE) + INTERVAL '16 days 18 hours'),
        ( 64000::numeric,     0::numeric,  64000::numeric, 'REJECTED',  'COD',     'UNPAID',   'KTX Khu A, ĐHQG TP.HCM', 'STAT_SEED_MONTH_REJECTED','Quán hết món',        date_trunc('month', CURRENT_DATE) + INTERVAL '21 days 13 hours'),
        (130000::numeric,     0::numeric, 130000::numeric, 'COMPLETED', 'MOMO',    'PAID',     'Làng Đại học',           'STAT_SEED_MONTH_LAST',    NULL,                  date_trunc('month', CURRENT_DATE) + INTERVAL '27 days 19 hours'),

        ( 88000::numeric,     0::numeric,  88000::numeric, 'COMPLETED', 'COD',     'PAID',     'KTX Khu A, ĐHQG TP.HCM', 'STAT_SEED_YEAR_JAN',      NULL,                  date_trunc('year', CURRENT_DATE) + INTERVAL '14 days 12 hours'),
        (102000::numeric,  2000::numeric, 100000::numeric, 'COMPLETED', 'MOMO',    'PAID',     'Đường Tô Vĩnh Diện',     'STAT_SEED_YEAR_MAR',      NULL,                  date_trunc('year', CURRENT_DATE) + INTERVAL '2 months 8 days 18 hours'),
        (115000::numeric,  5000::numeric, 110000::numeric, 'COMPLETED', 'ZALOPAY', 'PAID',     'Trường ĐH Quốc tế',      'STAT_SEED_YEAR_JUN',      NULL,                  date_trunc('year', CURRENT_DATE) + INTERVAL '5 months 4 days 11 hours'),
        ( 72000::numeric,     0::numeric,  72000::numeric, 'EXPIRED',   'COD',     'UNPAID',   'KTX Khu B, ĐHQG TP.HCM', 'STAT_SEED_YEAR_EXPIRED',  'Quán không xác nhận', date_trunc('year', CURRENT_DATE) + INTERVAL '8 months 18 days 10 hours'),
        (145000::numeric, 15000::numeric, 130000::numeric, 'COMPLETED', 'MOMO',    'PAID',     'Nhà văn hóa Sinh viên',  'STAT_SEED_YEAR_DEC',      NULL,                  date_trunc('year', CURRENT_DATE) + INTERVAL '11 months 7 days 20 hours'),

        ( 98000::numeric,     0::numeric,  98000::numeric, 'COMPLETED', 'COD',     'PAID',     'KTX Khu A, ĐHQG TP.HCM', 'STAT_SEED_LAST_YEAR_Q1',  NULL,                  date_trunc('year', CURRENT_DATE) - INTERVAL '1 year' + INTERVAL '1 month 9 days 12 hours'),
        (121000::numeric,  1000::numeric, 120000::numeric, 'COMPLETED', 'MOMO',    'PAID',     'Trường ĐH KHTN',         'STAT_SEED_LAST_YEAR_Q3',  NULL,                  date_trunc('year', CURRENT_DATE) - INTERVAL '1 year' + INTERVAL '7 months 18 days 18 hours')
    ) AS data(
        total_amount,
        discount_amount,
        final_amount,
        order_status,
        payment_method,
        payment_status,
        delivery_address,
        note,
        cancel_reason,
        created_at
    )
)
INSERT INTO orders (
    customer_id,
    shop_id,
    total_amount,
    discount_amount,
    final_amount,
    order_status,
    payment_method,
    payment_status,
    delivery_address,
    note,
    cancel_reason,
    created_at,
    updated_at
)
SELECT
    customer.id,
    shop.id,
    seed.total_amount,
    seed.discount_amount,
    seed.final_amount,
    seed.order_status::order_status,
    seed.payment_method::payment_method,
    seed.payment_status::payment_status,
    seed.delivery_address,
    '[statistics-seed] ' || seed.note,
    seed.cancel_reason,
    seed.created_at,
    seed.created_at
FROM seed_orders seed
JOIN demo_shops shop ON shop.email = seed.shop_email
CROSS JOIN demo_customer customer
WHERE NOT EXISTS (
    SELECT 1
    FROM orders existing
    WHERE existing.shop_id = shop.id
      AND existing.note = '[statistics-seed] ' || seed.note
);
