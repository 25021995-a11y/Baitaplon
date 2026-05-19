-- Migration v2: Gộp SELLER và BIDDER thành USER duy nhất
-- Chạy script này 1 lần để cập nhật DB

-- 1. Thêm các cột nếu chưa có
ALTER TABLE Users
    ADD COLUMN IF NOT EXISTS email VARCHAR(255) DEFAULT '',
    ADD COLUMN IF NOT EXISTS gender VARCHAR(10) DEFAULT 'Khác',
    ADD COLUMN IF NOT EXISTS created_date DATE DEFAULT (CURDATE()),
    ADD COLUMN IF NOT EXISTS vir_money DOUBLE DEFAULT 0.0;

-- 2. Đồng bộ vir_money: với user cũ là Seller thì vir_money = money (chưa bị lock)
UPDATE Users SET vir_money = money WHERE vir_money IS NULL;

-- 3. Cập nhật tất cả role về 'USER' (không còn phân biệt SELLER/BIDDER)
UPDATE Users SET role = 'USER' WHERE role IN ('SELLER', 'BIDDER');

-- 4. Cập nhật created_date nếu null
UPDATE Users SET created_date = CURDATE() WHERE created_date IS NULL;

-- Xác nhận
SELECT username, role, money, vir_money FROM Users;
