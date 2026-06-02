package com.bidcycle.dao;
import com.bidcycle.model.Product;
import com.bidcycle.model.User;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.sql.*;

public class ProductDAO {

    // 1. HÀM LƯU SẢN PHẨM (Dành cho Seller)
    // Nhận sellerId (int) thay vì seller_name để khớp với FK trong DB
    public static boolean insertProduct(String name, double price, int durationMinutes, int sellerId, String category, String description, String imagePath) {
        String sql = "INSERT INTO products (name, start_price, cur_price, start_time, end_time, seller_id, category, description, image_path, is_approved) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, FALSE)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            LocalDateTime startTime = LocalDateTime.now();
            LocalDateTime endTime   = startTime.plusMinutes(durationMinutes);

            pstmt.setString(1, name);
            pstmt.setDouble(2, price);
            pstmt.setDouble(3, price);
            pstmt.setTimestamp(4, Timestamp.valueOf(startTime));
            pstmt.setTimestamp(5, Timestamp.valueOf(endTime));
            pstmt.setInt(6, sellerId);
            pstmt.setString(7, category);
            pstmt.setString(8, description);
            pstmt.setString(9, imagePath);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 2. HÀM LẤY TẤT CẢ SẢN PHẨM TỪ DATABASE
    public static List<Product> getAllProducts() {
        List<Product> productList = new ArrayList<>();
        String sql = "SELECT * FROM products";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int    id    = rs.getInt("id");
                String name  = rs.getString("name");
                double startPrice = rs.getDouble("start_price");
                double curPrice   = rs.getDouble("cur_price");
                boolean isFinished = rs.getBoolean("is_finished");
                LocalDateTime startTime = rs.getTimestamp("start_time").toLocalDateTime();
                LocalDateTime endTime = rs.getTimestamp("end_time").toLocalDateTime();
                String category = rs.getString("category");
                String description = rs.getString("description");
                String imagePath = rs.getString("image_path");

                int  sellerId = rs.getInt("seller_id");
                User owner    = UserDAO.getUserById(sellerId);

                Product p = new Product(id, name, startPrice, curPrice, startTime, endTime, owner);
                p.setCategory(category);
                p.setDescription(description);
                p.setImagePath(imagePath);

                int highestBidderId = rs.getInt("highest_bidder_id");
                if (!rs.wasNull()) {
                    User highestBidder = UserDAO.getUserById(highestBidderId);
                    p.setHighestBidder(highestBidder);
                    p.setLockedAmountPrev(curPrice); 
                }

                p.setFinished(isFinished);
                
                // Tải danh sách Auto-Bids cho sản phẩm này
                p.setAutoBids(getAutoBidsForProduct(id));

                productList.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return productList;
    }

    public static List<Product.AutoBidConfig> getAutoBidsForProduct(int productId) {
        List<Product.AutoBidConfig> configs = new ArrayList<>();
        String sql = "SELECT * FROM auto_bids WHERE product_id = ? ORDER BY created_at ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, productId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    User user = UserDAO.getUserById(rs.getInt("user_id"));
                    double maxBid = rs.getDouble("max_bid");
                    double increment = rs.getDouble("increment");
                    LocalDateTime createdAt = rs.getTimestamp("created_at").toLocalDateTime();
                    configs.add(new Product.AutoBidConfig(user, maxBid, increment, createdAt));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return configs;
    }

    public static boolean updateHighestBid(Product p) {
        // Cập nhật giá, người dẫn đầu và thời gian kết thúc (phòng trường hợp gia hạn)
        String sqlProduct = "UPDATE products SET cur_price = ?, highest_bidder_id = ?, end_time = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Cập nhật bảng products
                try (PreparedStatement pstmt = conn.prepareStatement(sqlProduct)) {
                    pstmt.setDouble(1, p.getPrice());
                    if (p.getHighestBidder() != null) {
                        pstmt.setInt(2, p.getHighestBidder().getUserId());
                    } else {
                        pstmt.setNull(2, Types.INTEGER);
                    }
                    pstmt.setTimestamp(3, Timestamp.valueOf(p.getEndTime()));
                    pstmt.setInt(4, p.getProductId());
                    pstmt.executeUpdate();
                }

                // 2. Cập nhật bảng auto_bids (xóa cũ thêm mới để đơn giản, hoặc dùng UPSERT)
                // Ở đây ta dùng UPSERT logic cho từng config trong p.getAutoBids()
                String sqlUpsertAuto = "INSERT INTO auto_bids (product_id, user_id, max_bid, increment, created_at) " +
                                     "VALUES (?, ?, ?, ?, ?) " +
                                     "ON DUPLICATE KEY UPDATE max_bid = VALUES(max_bid), increment = VALUES(increment)";
                
                try (PreparedStatement pstmt = conn.prepareStatement(sqlUpsertAuto)) {
                    for (Product.AutoBidConfig config : p.getAutoBids()) {
                        pstmt.setInt(1, p.getProductId());
                        pstmt.setInt(2, config.getUser().getUserId());
                        pstmt.setDouble(3, config.getMaxBid());
                        pstmt.setDouble(4, config.getIncrement());
                        pstmt.setTimestamp(5, Timestamp.valueOf(config.getCreatedAt()));
                        pstmt.addBatch();
                    }
                    pstmt.executeBatch();
                }

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateProductFinished(int productId, boolean isFinished) {
        String sql = "UPDATE products SET is_finished = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setBoolean(1, isFinished);
            pstmt.setInt(2, productId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ── Bid History ──────────────────────────────────────────

    public static void recordBidHistory(int productId, double price) {
        String sql = "INSERT INTO bid_history (product_id, price) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, productId);
            pstmt.setDouble(2, price);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<double[]> getBidHistory(int productId) {
        List<double[]> history = new ArrayList<>();
        String sql = "SELECT price, UNIX_TIMESTAMP(bid_time) as ts FROM bid_history WHERE product_id = ? ORDER BY bid_time ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, productId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    history.add(new double[]{rs.getDouble("ts") * 1000, rs.getDouble("price")});
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return history;
    }
}
