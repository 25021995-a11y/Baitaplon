package com.bidcycle.dao;

import com.bidcycle.model.Admin;
import com.bidcycle.model.Product;
import com.bidcycle.model.User;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * DAO: Toàn bộ thao tác quản trị toàn hệ thống.
 * Quản lý: tài khoản, vật phẩm, phí giao dịch, báo cáo.
 */
public class AdminDAO {

    private AdminDAO() {}

    // ────────────────────────────────────────────────────────────
    // ✓ QUẢN LÝ TÀI KHOẢN — Approval & Banning
    // ────────────────────────────────────────────────────────────

    /**
     * Duyệt tài khoản người dùng (kích hoạt).
     */
    public static boolean approveAccount(int userId) {
        String sql = "UPDATE users SET is_active = TRUE WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    /**
     * Cấm tài khoản (vô hiệu hóa).
     */
    public static boolean banUser(int userId, String reason) {
        String sql = "UPDATE users SET is_active = FALSE WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            int result = pstmt.executeUpdate();

            // Ghi lại lý do cấm
            if (result > 0) {
                recordBanAction(userId, reason);
            }
            return result > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    /**
     * Ghi lại hành động cấm tài khoản trong log.
     */
    private static void recordBanAction(int userId, String reason) {
        String sql = "INSERT INTO admin_logs (user_id, action, reason, timestamp) VALUES (?, 'BAN_USER', ?, NOW())";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, reason != null ? reason : "No reason provided");
            pstmt.executeUpdate();
        } catch (SQLException e) {
            // Bảng admin_logs có thể không tồn tại, không là lỗi chí mạng
        }
    }

    /**
     * Lấy danh sách tất cả người dùng.
     */
    public static List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY created_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                User user = mapResultSetToUser(rs);
                users.add(user);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return users;
    }

    /**
     * Lấy danh sách người dùng chưa được duyệt.
     */
    public static List<User> getInactiveUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE is_active = FALSE ORDER BY created_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                User user = mapResultSetToUser(rs);
                users.add(user);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return users;
    }

    /**
     * Cập nhật quyền hạn cho người dùng.
     */
    public static boolean updateUserRole(int userId, String role) {
        String sql = "UPDATE users SET role = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, role);
            pstmt.setInt(2, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    // ────────────────────────────────────────────────────────────
    // ✓ KIỂM DUYỆT VẬT PHẨM — Item Review
    // ────────────────────────────────────────────────────────────

    /**
     * Duyệt vật phẩm (cho phép xuất hiện trên trang chủ).
     */
    public static boolean approveItem(int productId) {
        String sql = "UPDATE products SET is_approved = TRUE WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, productId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    /**
     * Từ chối vật phẩm (ẩn khỏi trang chủ).
     */
    public static boolean rejectItem(int productId, String reason) {
        String sql = "UPDATE products SET is_approved = FALSE WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, productId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    /**
     * Buộc dừng phiên đấu giá (kết thúc trước thời hạn).
     */
    public static boolean forceStopAuction(int productId) {
        String sql = "UPDATE products SET is_finished = TRUE, end_time = NOW() WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, productId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    /**
     * Lấy danh sách vật phẩm chưa được duyệt.
     */
    public static List<Map<String, Object>> getPendingItems() {
        List<Map<String, Object>> items = new ArrayList<>();
        String sql = "SELECT p.*, u.username as owner_name FROM products p " +
                    "LEFT JOIN users u ON p.seller_id = u.id " +
                    "WHERE (p.is_approved IS NULL OR p.is_approved = FALSE) " +
                    "AND p.is_finished = FALSE " +
                    "ORDER BY p.start_time DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, Object> item = new HashMap<>();
                item.put("productId", rs.getInt("id"));
                item.put("name", rs.getString("name"));
                item.put("ownerName", rs.getString("owner_name"));
                item.put("ownerName", rs.getString("owner_name"));
                item.put("startPrice", rs.getDouble("start_price"));
                item.put("category", rs.getString("category"));
                item.put("description", rs.getString("description"));
                item.put("isApproved", rs.getBoolean("is_approved"));
                items.add(item);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return items;
    }

    // ────────────────────────────────────────────────────────────
    // ✓ CẤU HÌNH & THỐ NG KÊ — Configuration & Reports
    // ────────────────────────────────────────────────────────────

    public static void ensureSystemConfigTableExists() {
        String sqlConfig = "CREATE TABLE IF NOT EXISTS system_config (" +
                     "param_key VARCHAR(50) PRIMARY KEY, " +
                     "param_value DOUBLE NOT NULL" +
                     ")";
        String sqlLogs = "CREATE TABLE IF NOT EXISTS admin_logs (" +
                         "id INT AUTO_INCREMENT PRIMARY KEY, " +
                         "user_id INT, " +
                         "action VARCHAR(50), " +
                         "reason TEXT, " +
                         "timestamp DATETIME" +
                         ")";
        String sqlUsers = "CREATE TABLE IF NOT EXISTS users (" +
                          "id INT AUTO_INCREMENT PRIMARY KEY, " +
                          "username VARCHAR(50) UNIQUE NOT NULL, " +
                          "password VARCHAR(100) NOT NULL, " +
                          "is_active BOOLEAN DEFAULT TRUE, " +
                          "role VARCHAR(20) DEFAULT 'BIDDER', " +
                          "money DOUBLE DEFAULT 0.0, " +
                          "vir_money DOUBLE DEFAULT 0.0, " +
                          "email VARCHAR(255) DEFAULT '', " +
                          "gender VARCHAR(10) DEFAULT 'Khác', " +
                          "created_date DATE DEFAULT (CURDATE())" +
                          ")";
        String sqlProducts = "CREATE TABLE IF NOT EXISTS products (" +
                             "id INT AUTO_INCREMENT PRIMARY KEY, " +
                             "name VARCHAR(255) NOT NULL, " +
                             "start_price DOUBLE NOT NULL, " +
                             "cur_price DOUBLE NOT NULL, " +
                             "start_time DATETIME, " +
                             "end_time DATETIME, " +
                             "seller_id INT, " +
                             "highest_bidder_id INT, " +
                             "category VARCHAR(100) DEFAULT 'Khác', " +
                             "description TEXT, " +
                             "image_path VARCHAR(500), " +
                             "is_approved BOOLEAN DEFAULT FALSE, " +
                             "is_finished BOOLEAN DEFAULT FALSE, " +
                             "reject_reason TEXT, " +
                             "FOREIGN KEY (seller_id) REFERENCES users(id), " +
                             "FOREIGN KEY (highest_bidder_id) REFERENCES users(id)" +
                             ")";
        String sqlAutoBids = "CREATE TABLE IF NOT EXISTS auto_bids (" +
                             "id INT AUTO_INCREMENT PRIMARY KEY, " +
                             "product_id INT NOT NULL, " +
                             "user_id INT NOT NULL, " +
                             "max_bid DOUBLE NOT NULL, " +
                             "increment DOUBLE NOT NULL, " +
                             "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                             "UNIQUE KEY unique_product_user (product_id, user_id), " +
                             "FOREIGN KEY (product_id) REFERENCES products(id), " +
                             "FOREIGN KEY (user_id) REFERENCES users(id)" +
                             ")";
        String sqlBidHistory = "CREATE TABLE IF NOT EXISTS bid_history (" +
                               "id INT AUTO_INCREMENT PRIMARY KEY, " +
                               "product_id INT NOT NULL, " +
                               "price DOUBLE NOT NULL, " +
                               "bid_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                               "FOREIGN KEY (product_id) REFERENCES products(id)" +
                               ")";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Tạo các bảng theo thứ tự để tránh lỗi FK
            stmt.executeUpdate(sqlConfig);
            stmt.executeUpdate(sqlLogs);
            stmt.executeUpdate(sqlUsers);
            stmt.executeUpdate(sqlProducts);
            stmt.executeUpdate(sqlAutoBids);
            stmt.executeUpdate(sqlBidHistory);

            // Cập nhật cấu trúc bảng users nếu cần (Migration)
            try {
                stmt.executeUpdate("ALTER TABLE users ADD COLUMN IF NOT EXISTS email VARCHAR(255) DEFAULT ''");
                stmt.executeUpdate("ALTER TABLE users ADD COLUMN IF NOT EXISTS gender VARCHAR(10) DEFAULT 'Khác'");
                stmt.executeUpdate("ALTER TABLE users ADD COLUMN IF NOT EXISTS created_date DATE DEFAULT (CURDATE())");
                stmt.executeUpdate("ALTER TABLE users ADD COLUMN IF NOT EXISTS vir_money DOUBLE DEFAULT 0.0");
                // Cập nhật created_date nếu null cho các record cũ
                stmt.executeUpdate("UPDATE users SET created_date = CURDATE() WHERE created_date IS NULL");
            } catch (SQLException ignore) {
                // Một số phiên bản MySQL cũ có thể không hỗ trợ ADD COLUMN IF NOT EXISTS
                // Chúng ta sẽ thử thêm từng cột một cách thủ công để an toàn hơn
                String[] cols = {
                    "ALTER TABLE users ADD email VARCHAR(255) DEFAULT ''",
                    "ALTER TABLE users ADD gender VARCHAR(10) DEFAULT 'Khác'",
                    "ALTER TABLE users ADD created_date DATE DEFAULT (CURDATE())",
                    "ALTER TABLE users ADD vir_money DOUBLE DEFAULT 0.0"
                };
                for (String colSql : cols) {
                    try { stmt.executeUpdate(colSql); } catch (SQLException e2) { /* Column already exists */ }
                }
                try { stmt.executeUpdate("UPDATE users SET created_date = CURDATE() WHERE created_date IS NULL"); } catch (SQLException e3) {}
            }

            // Khởi tạo phí mặc định nếu chưa có
            String initSql = "INSERT IGNORE INTO system_config (param_key, param_value) VALUES ('TRANSACTION_FEE', 0.02)";
            stmt.executeUpdate(initSql);

            // Cập nhật cấu trúc bảng products (Migration)
            String[] productCols = {
                "ALTER TABLE products ADD category VARCHAR(100) DEFAULT 'Khác'",
                "ALTER TABLE products ADD description TEXT",
                "ALTER TABLE products ADD image_path VARCHAR(500)",
                "ALTER TABLE products ADD is_approved BOOLEAN DEFAULT FALSE",
                "ALTER TABLE products ADD reject_reason TEXT"
            };
            for (String colSql : productCols) {
                try { stmt.executeUpdate(colSql); } catch (SQLException e2) { /* Column already exists or error */ }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Cài đặt phí giao dịch hệ thống (%).
     */
    public static boolean setTransactionFee(double feePercent) {
        String sql = "INSERT INTO system_config (param_key, param_value) VALUES ('TRANSACTION_FEE', ?) " +
                    "ON DUPLICATE KEY UPDATE param_value = VALUES(param_value)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, feePercent);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    /**
     * Lấy phí giao dịch hiện tại.
     */
    public static double getTransactionFee() {
        String sql = "SELECT param_value FROM system_config WHERE param_key = 'TRANSACTION_FEE'";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            try (ResultSet rs = stmt.executeQuery(sql)) {
                if (rs.next()) return rs.getDouble("param_value");
            }
        } catch (SQLException e) {
            // Table might not exist yet if called before initialization
            System.err.println("[WARN] system_config table not found, using default fee.");
        }
        return 0.02; // Default 2%
    }

    /**
     * Sinh báo cáo thống kê toàn hệ thống.
     */
    public static Map<String, Object> generateReport() {
        Map<String, Object> report = new HashMap<>();

        try (Connection conn = DatabaseConnection.getConnection()) {
            // Tổng số user
            report.put("totalUsers", countQuery(conn, "SELECT COUNT(*) FROM users"));

            // Số user hoạt động
            report.put("activeUsers", countQuery(conn, "SELECT COUNT(*) FROM users WHERE is_active = TRUE"));

            // Số user bị cấm
            report.put("bannedUsers", countQuery(conn, "SELECT COUNT(*) FROM users WHERE is_active = FALSE"));

            // Tổng số vật phẩm
            report.put("totalProducts", countQuery(conn, "SELECT COUNT(*) FROM products"));

            // Số vật phẩm đã kết thúc
            report.put("finishedProducts", countQuery(conn, "SELECT COUNT(*) FROM products WHERE is_finished = TRUE"));

            // Số vật phẩm đang diễn ra
            report.put("ongoingProducts", countQuery(conn, "SELECT COUNT(*) FROM products WHERE is_finished = FALSE"));

            // Tổng giá trị giao dịch
            report.put("totalTransactionValue", sumQuery(conn,
                "SELECT COALESCE(SUM(cur_price), 0) FROM products WHERE is_finished = TRUE"));

            // Tổng tiền nạp
            report.put("totalDeposits", sumQuery(conn,
                "SELECT COALESCE(SUM(money), 0) FROM users"));

            // Doanh thu phí
            double totalTransValue = (Double) report.get("totalTransactionValue");
            double feePercent = getTransactionFee();
            report.put("feeRevenue", totalTransValue * feePercent / 100);

            // Top sellers
            report.put("topSellers", getTopSellers(conn, 5));

            // Top bidders
            report.put("topBidders", getTopBidders(conn, 5));

        } catch (SQLException e) { e.printStackTrace(); }

        return report;
    }

    private static long countQuery(Connection conn, String sql) throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getLong(1);
        }
        return 0;
    }

    private static double sumQuery(Connection conn, String sql) throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getDouble(1);
        }
        return 0.0;
    }

    private static List<Map<String, Object>> getTopSellers(Connection conn, int limit) throws SQLException {
        List<Map<String, Object>> sellers = new ArrayList<>();
        String sql = "SELECT u.username, COUNT(p.id) as sold_count, SUM(p.cur_price) as total_revenue " +
                    "FROM users u LEFT JOIN products p ON u.id = p.seller_id " +
                    "WHERE p.is_finished = TRUE GROUP BY u.id ORDER BY total_revenue DESC LIMIT ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> seller = new HashMap<>();
                    seller.put("username", rs.getString("username"));
                    seller.put("soldCount", rs.getInt("sold_count"));
                    seller.put("totalRevenue", rs.getDouble("total_revenue"));
                    sellers.add(seller);
                }
            }
        }
        return sellers;
    }

    private static List<Map<String, Object>> getTopBidders(Connection conn, int limit) throws SQLException {
        List<Map<String, Object>> bidders = new ArrayList<>();
        String sql = "SELECT u.username, COUNT(p.id) as won_count, SUM(p.cur_price) as total_spent " +
                    "FROM users u LEFT JOIN products p ON u.id = p.highest_bidder_id " +
                    "WHERE p.is_finished = TRUE GROUP BY u.id ORDER BY total_spent DESC LIMIT ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> bidder = new HashMap<>();
                    bidder.put("username", rs.getString("username"));
                    bidder.put("wonCount", rs.getInt("won_count"));
                    bidder.put("totalSpent", rs.getDouble("total_spent"));
                    bidders.add(bidder);
                }
            }
        }
        return bidders;
    }



    // ────────────────────────────────────────────────────────────
    // Helper Methods
    // ────────────────────────────────────────────────────────────

    private static User mapResultSetToUser(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String username = rs.getString("username");
        String password = rs.getString("password");
        boolean isActive = rs.getBoolean("is_active");
        
        String email = "";
        try { email = rs.getString("email"); } catch (SQLException e) { /* Missing */ }
        
        String gender = "Khác";
        try { gender = rs.getString("gender"); } catch (SQLException e) { /* Missing */ }
        
        LocalDate created = LocalDate.now();
        try {
            java.sql.Date d = rs.getDate("created_date");
            if (d != null) created = d.toLocalDate();
        } catch (SQLException e) { /* Missing */ }

        String role = "BIDDER";
        try { role = rs.getString("role"); } catch (Exception ignored) {}

        User user;
        if ("ADMIN".equalsIgnoreCase(role) || "ADMINISTRATOR".equalsIgnoreCase(role)) {
            user = new Admin(id, username, password, isActive, email, gender, created);
        } else {
            user = new User(id, username, password, isActive, email, gender, created);
        }
        
        try { user.setMoney(rs.getDouble("money")); } catch (SQLException e) { /* Missing */ }
        try { user.setVirMoney(rs.getDouble("vir_money")); } catch (SQLException e) { /* Missing */ }
        
        return user;
    }
}



