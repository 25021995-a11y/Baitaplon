package com.bidcycle.util;

import com.bidcycle.dao.DatabaseConnection;
import com.bidcycle.dao.ProductDAO;
import com.bidcycle.model.Product;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Watcher để phát hiện các thay đổi từ Database (dành cho môi trường đa tiến trình).
 * Nó định kỳ kiểm tra bảng products và bid_history để kích hoạt AuctionEventManager cục bộ.
 */
public class DatabaseWatcher {
    private static DatabaseWatcher instance;
    private Timeline watcherTimeline;
    private final Map<Integer, Double> lastKnownPrices = new HashMap<>();

    private DatabaseWatcher() {}

    public static synchronized DatabaseWatcher getInstance() {
        if (instance == null) {
            instance = new DatabaseWatcher();
        }
        return instance;
    }

    /**
     * Bắt đầu theo dõi Database.
     */
    public void start() {
        if (watcherTimeline != null) {
            watcherTimeline.stop();
        }

        // Kiểm tra mỗi 2 giây
        watcherTimeline = new Timeline(new KeyFrame(Duration.seconds(2), e -> checkForUpdates()));
        watcherTimeline.setCycleCount(Animation.INDEFINITE);
        watcherTimeline.play();
    }

    public void stop() {
        if (watcherTimeline != null) {
            watcherTimeline.stop();
        }
    }

    /**
     * Ghi nhận giá mới được đặt từ CHÍNH ứng dụng này 
     * để Watcher không thông báo trùng lặp.
     */
    public void recordLocalUpdate(int productId, double price) {
        lastKnownPrices.put(productId, price);
    }

    private void checkForUpdates() {
        // 1. Kiểm tra sản phẩm mới hoặc giá thay đổi
        String sql = "SELECT id, cur_price FROM products WHERE is_finished = 0";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                int productId = rs.getInt("id");
                double currentPrice = rs.getDouble("cur_price");

                Double lastPrice = lastKnownPrices.get(productId);
                
                // Nếu là sản phẩm mới đăng (chưa có trong Map) hoặc giá đã thay đổi
                if (lastPrice == null || currentPrice != lastPrice) {
                    lastKnownPrices.put(productId, currentPrice);
                    
                    Product updatedProduct = ProductDAO.getProductById(productId);
                    if (updatedProduct != null) {
                        // Notify sẽ giúp BidderHomeController biết để thêm vào list hoặc cập nhật giá
                        AuctionEventManager.getInstance().notifyBid(updatedProduct);
                    }
                }
            }
        } catch (SQLException ex) {
            // Có thể bỏ qua lỗi kết nối tạm thời
        }
    }
}
