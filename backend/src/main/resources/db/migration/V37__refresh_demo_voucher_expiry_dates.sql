UPDATE vouchers
SET expiry_date = DATE '2026-12-31'
WHERE code = 'FREESHIP50'
  AND expiry_date < DATE '2026-12-31';

UPDATE vouchers
SET expiry_date = DATE '2026-12-15'
WHERE code = 'FOOD30'
  AND expiry_date < DATE '2026-12-15';

UPDATE vouchers
SET expiry_date = DATE '2027-01-31'
WHERE code = 'NEWUSER'
  AND expiry_date < DATE '2027-01-31';

UPDATE vouchers
SET expiry_date = DATE '2026-12-20'
WHERE code = 'COMBO20'
  AND expiry_date < DATE '2026-12-20';
