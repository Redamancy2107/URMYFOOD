-- Update seeded shop accounts to have the 'SHOP' role so they can log into the shop app.
UPDATE accounts 
SET role = 'SHOP' 
WHERE email IN (
    'shop@urmyfood.com', 
    'cali_nguyenhue@urmyfood.com', 
    'gongcha_ht@urmyfood.com', 
    'phohung_ltt@urmyfood.com'
);
