-- Seed posts using the first account if it exists
INSERT INTO posts (dish_name, price, original_price, max_quantity, remaining_quantity,
                   end_time, is_flash_sale, status, content, image_url, author_id)
SELECT
    dish_name, price, original_price, max_quantity, remaining_quantity,
    end_time, is_flash_sale, status::post_status, content, image_url, author_id
FROM (VALUES
    ('Phở Bò Đặc Biệt', 75000, 95000, 50, 50,
     NOW() + INTERVAL '6 hours', true, 'ACTIVE',
     'Phở bò đặc biệt với nước dùng hầm 12 tiếng, thịt tái chín mềm tan.',
     'https://images.unsplash.com/photo-1582878826629-29b7ad1cdc43?w=800'),

    ('Bún Bò Huế', 65000, 65000, 30, 30,
     NULL, false, 'ACTIVE',
     'Bún bò Huế chuẩn vị với chả cua, giò heo và rau sống tươi ngon.',
     'https://images.unsplash.com/photo-1626804475297-41608ea09aeb?w=800'),

    ('Cơm Tấm Sườn Nướng', 55000, 70000, 100, 87,
     NOW() + INTERVAL '3 hours', true, 'ACTIVE',
     'Cơm tấm sườn nướng thơm lừng, kèm chả trứng và bì.',
     'https://images.unsplash.com/photo-1569050467447-ce54b3bbc37d?w=800'),

    ('Bánh Mì Thịt Nướng', 30000, 30000, 200, 162,
     NULL, false, 'ACTIVE',
     'Bánh mì giòn rụm, thịt nướng thơm, dưa cải và rau thơm tươi.',
     'https://images.unsplash.com/photo-1559054663-e8d23213f55c?w=800'),

    ('Bò Lúc Lắc', 120000, 150000, 20, 8,
     NOW() + INTERVAL '2 hours', true, 'ACTIVE',
     'Bò lúc lắc phi lê mềm, xào với tiêu xanh, ăn kèm cơm trắng.',
     'https://images.unsplash.com/photo-1547592180-85f173990554?w=800'),

    ('Gỏi Cuốn Tôm Thịt', 45000, 45000, 60, 0,
     NULL, false, 'SOLD_OUT',
     'Gỏi cuốn tươi với tôm, thịt heo, bún và rau sống, chấm tương đậu phộng.',
     'https://images.unsplash.com/photo-1563245372-f21724e3856d?w=800'),

    ('Lẩu Thái Hải Sản', 250000, 300000, 10, 10,
     NOW() + INTERVAL '12 hours', true, 'ACTIVE',
     'Lẩu Thái chua cay đậm vị, đầy đủ tôm, mực, nghêu và nấm các loại.',
     'https://images.unsplash.com/photo-1569050467447-ce54b3bbc37d?w=800'),

    ('Bánh Xèo Miền Tây', 40000, 40000, 80, 45,
     NULL, false, 'ACTIVE',
     'Bánh xèo giòn vàng nhân tôm thịt, ăn kèm rau sống và nước chấm đặc biệt.',
     'https://images.unsplash.com/photo-1565299585323-38d6b0865b47?w=800')
) AS data(dish_name, price, original_price, max_quantity, remaining_quantity,
          end_time, is_flash_sale, status, content, image_url)
CROSS JOIN (SELECT id AS author_id FROM accounts LIMIT 1) AS acc
WHERE EXISTS (SELECT 1 FROM accounts LIMIT 1);
