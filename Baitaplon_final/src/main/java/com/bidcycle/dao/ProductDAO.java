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
    public static boolean insertProduct(String name, double price, int durationMinutes, int sellerId) {
        String sql = "INSERT INTO products (name, cur_price, start_price, start_time, end_time, seller_id) VALUES (?, ?, ?, ?, ?, ?)";
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

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            // Fallback nếu chưa có cột start_price
            return insertProductBasic(name, price, durationMinutes, sellerId);
        }
    }

    private static boolean insertProductBasic(String name, double price, int durationMinutes, int sellerId) {
        String sql = "INSERT INTO products (name, cur_price, start_time, end_time, seller_id) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            LocalDateTime startTime = LocalDateTime.now();
            LocalDateTime endTime   = startTime.plusMinutes(durationMinutes);

            pstmt.setString(1, name);
            pstmt.setDouble(2, price);
            pstmt.setTimestamp(3, Timestamp.valueOf(startTime));
            pstmt.setTimestamp(4, Timestamp.valueOf(endTime));
            pstmt.setInt(5, sellerId);

            return pstmt.executeUpdate() > 0;
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
                double curPrice = rs.getDouble("cur_price");
                double startPrice = 0;
                try {
                    startPrice = rs.getDouble("start_price");
                    if (rs.wasNull()) startPrice = curPrice;
                } catch (Exception e) {
                    startPrice = curPrice;
                }

                boolean isFinished = rs.getBoolean("is_finished");
                // Tính durationInMinutes từ start_time và end_time
                LocalDateTime startTime = rs.getTimestamp("start_time").toLocalDateTime();
                LocalDateTime endTime = rs.getTimestamp("end_time").toLocalDateTime();

                // Lấy owner (seller) theo seller_id
                int  sellerId = rs.getInt("seller_id");
                User owner    = UserDAO.getUserById(sellerId);

                // description, image_path, category không có trong schema hiện tại
                String description = "";
                String imagePath   = "";
                String category    = "Khác";

                Product p = new Product(id, name, startPrice, curPrice, startTime, endTime, owner);

                // Đồng bộ highest_bidder nếu có
                int highestBidderId = rs.getInt("highest_bidder_id");
                if (!rs.wasNull()) {
                    User highestBidder = UserDAO.getUserById(highestBidderId);
                    p.setHighestBidder(highestBidder);
                }

                // Đồng bộ trạng thái kết thúc

                p.setFinished(isFinished);

                productList.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return productList;
    }

    public static void updateProductBid(int productId, double newPrice, int bidderId) {
        String sql = "UPDATE products SET cur_price = ?, highest_bidder_id = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, newPrice);
            pstmt.setInt(2, bidderId);
            pstmt.setInt(3, productId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
