-- Bổ sung cột cho tính năng Auto-Bid vào bảng products
ALTER TABLE products
    ADD COLUMN IF NOT EXISTS auto_bid_holder_id INT DEFAULT NULL,
    ADD COLUMN IF NOT EXISTS auto_bid_max DOUBLE DEFAULT 0.0,
    ADD COLUMN IF NOT EXISTS auto_bid_step DOUBLE DEFAULT 0.0;

-- Thêm ràng buộc khóa ngoại nếu cần
-- ALTER TABLE products ADD CONSTRAINT fk_auto_bid_holder FOREIGN KEY (auto_bid_holder_id) REFERENCES users(id);
