-- Seed 3 real shop accounts
-- Password: Urmyfood@1 (BCrypt rounds=10)
INSERT INTO accounts (full_name, email, phone, password_hash, role, avatar_url)
VALUES
('Cơm Tấm Cali - Nguyễn Huệ', 'cali_nguyenhue@urmyfood.com', '0901112222',
 '$2a$10$DJrtmLPnBeNpJWigjYvJVOFD7SncY584GwdbWV7kf9rLMS/vDdXTW', 'CUSTOMER',
 'https://images.unsplash.com/photo-1569050467447-ce54b3bbc37d?w=200'),
('Tiệm Trà Sữa Gong Cha - Hồ Tùng Mậu', 'gongcha_ht@urmyfood.com', '0903334444',
 '$2a$10$DJrtmLPnBeNpJWigjYvJVOFD7SncY584GwdbWV7kf9rLMS/vDdXTW', 'CUSTOMER',
 'https://images.unsplash.com/photo-1541658016709-82535e94bc69?w=200'),
('Phở Hùng - Lê Thánh Tôn', 'phohung_ltt@urmyfood.com', '0905556666',
 '$2a$10$DJrtmLPnBeNpJWigjYvJVOFD7SncY584GwdbWV7kf9rLMS/vDdXTW', 'CUSTOMER',
 'https://images.unsplash.com/photo-1582878826629-29b7ad1cdc43?w=200')
ON CONFLICT (email) DO NOTHING;

-- Seed delicious dishes for Cơm Tấm Cali - Nguyễn Huệ
INSERT INTO posts (dish_name, price, original_price, max_quantity, remaining_quantity,
                   end_time, is_flash_sale, status, content, image_url, author_id)
SELECT
    dish_name, price, original_price, max_quantity, remaining_quantity,
    end_time, is_flash_sale, status, content, image_url, acc.id
FROM (VALUES
    ('Cơm Tấm Sườn Bì Chả Đặc Biệt',
     65000::numeric, 80000::numeric, 40, 40,
     NOW() + INTERVAL '4 hours', true, 'ACTIVE',
     'Cơm tấm sườn nướng mật ong thơm lừng kết hợp với bì heo giòn dai, chả trứng béo ngậy và mỡ hành siêu thơm.',
     'https://images.unsplash.com/photo-1569050467447-ce54b3bbc37d?w=800'),

    ('Cơm Tấm Đùi Gà Nướng Mật Ong',
     55000::numeric, 55000::numeric, 25, 25,
     NULL, false, 'ACTIVE',
     'Cơm tấm mềm dẻo, ăn kèm đùi gà góc tư nướng sốt mật ong đậm vị, nước mắm ngọt chua siêu đưa cơm.',
     'https://images.unsplash.com/photo-1547592180-85f173990554?w=800'),

    ('Cơm Tấm Ba Chỉ Nướng Sốt Teriyaki',
     60000::numeric, 75000::numeric, 30, 30,
     NOW() + INTERVAL '3 hours', true, 'ACTIVE',
     'Sự kết hợp hoàn hảo giữa cơm tấm truyền thống và thịt ba chỉ nướng giòn rụm tẩm sốt Teriyaki kiểu Nhật độc đáo.',
     'https://images.unsplash.com/photo-1565299585323-38d6b0865b47?w=800')
) AS data(dish_name, price, original_price, max_quantity, remaining_quantity,
           end_time, is_flash_sale, status, content, image_url)
CROSS JOIN (SELECT id FROM accounts WHERE email = 'cali_nguyenhue@urmyfood.com') AS acc
WHERE NOT EXISTS (SELECT 1 FROM posts WHERE author_id = acc.id);

-- Seed delicious drinks for Tiệm Trà Sữa Gong Cha - Hồ Tùng Mậu
INSERT INTO posts (dish_name, price, original_price, max_quantity, remaining_quantity,
                   end_time, is_flash_sale, status, content, image_url, author_id)
SELECT
    dish_name, price, original_price, max_quantity, remaining_quantity,
    end_time, is_flash_sale, status, content, image_url, acc.id
FROM (VALUES
    ('Trà Sữa Trân Châu Hoàng Gia',
     42000::numeric, 55000::numeric, 80, 80,
     NOW() + INTERVAL '6 hours', true, 'ACTIVE',
     'Trà sữa ô long truyền thống thơm bùi kết hợp với gấp đôi trân châu đen hoàng gia dai giòn sần sật.',
     'https://images.unsplash.com/photo-1541658016709-82535e94bc69?w=800'),

    ('Hồng Trà Sữa Sương Sáo',
     38000::numeric, 38000::numeric, 50, 50,
     NULL, false, 'ACTIVE',
     'Vị hồng trà thanh mát đậm đà kết hợp với sương sáo mềm mịn thanh nhiệt, cực kỳ phù hợp cho những ngày nắng nóng.',
     'https://images.unsplash.com/photo-1507133750040-4a8f57021571?w=800'),

    ('Sữa Tươi Trân Châu Đường Đen',
     48000::numeric, 60000::numeric, 60, 60,
     NOW() + INTERVAL '5 hours', true, 'ACTIVE',
     'Sữa tươi thanh trùng Đà Lạt Milk béo ngậy quyện cùng sốt đường đen tự nấu đậm đặc và trân châu đường đen ấm nóng.',
     'https://images.unsplash.com/photo-1541658016709-82535e94bc69?w=800')
) AS data(dish_name, price, original_price, max_quantity, remaining_quantity,
           end_time, is_flash_sale, status, content, image_url)
CROSS JOIN (SELECT id FROM accounts WHERE email = 'gongcha_ht@urmyfood.com') AS acc
WHERE NOT EXISTS (SELECT 1 FROM posts WHERE author_id = acc.id);

-- Seed delicious dishes for Phở Hùng - Lê Thánh Tôn
INSERT INTO posts (dish_name, price, original_price, max_quantity, remaining_quantity,
                   end_time, is_flash_sale, status, content, image_url, author_id)
SELECT
    dish_name, price, original_price, max_quantity, remaining_quantity,
    end_time, is_flash_sale, status, content, image_url, acc.id
FROM (VALUES
    ('Phở Tái Nạm Gầu Bò Úc',
     70000::numeric, 90000::numeric, 35, 35,
     NOW() + INTERVAL '8 hours', true, 'ACTIVE',
     'Phở bò truyền thống với nước dùng ninh từ xương ống 24h thơm mùi quế hồi, thịt bò gầu nạm Úc thái mỏng chín mềm.',
     'https://images.unsplash.com/photo-1582878826629-29b7ad1cdc43?w=800'),

    ('Phở Gà Ta Xé Lá Chanh',
     60000::numeric, 60000::numeric, 40, 40,
     NULL, false, 'ACTIVE',
     'Phở gà với nước dùng thanh ngọt, thịt gà ta dai chắc thịt xé phay giòn bì kết hợp cùng lá chanh thái sợi thơm nức.',
     'https://images.unsplash.com/photo-1626804475297-41608ea09aeb?w=800'),

    ('Phở Bò Viên Thập Cẩm',
     65000::numeric, 80000::numeric, 30, 30,
     NOW() + INTERVAL '7 hours', true, 'ACTIVE',
     'Tô phở nóng hổi tràn đầy bò viên khổng lồ dai ngon sần sật, gân bò giòn rụm và thịt bò tái tươi rói.',
     'https://images.unsplash.com/photo-1582878826629-29b7ad1cdc43?w=800')
) AS data(dish_name, price, original_price, max_quantity, remaining_quantity,
           end_time, is_flash_sale, status, content, image_url)
CROSS JOIN (SELECT id FROM accounts WHERE email = 'phohung_ltt@urmyfood.com') AS acc
WHERE NOT EXISTS (SELECT 1 FROM posts WHERE author_id = acc.id);
