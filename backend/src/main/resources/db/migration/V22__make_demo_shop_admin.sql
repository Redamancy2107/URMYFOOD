-- Promote the demo shop account to admin so it can access admin endpoints.
UPDATE accounts
SET role = 'ADMIN'
WHERE email = 'shop@urmyfood.com';
